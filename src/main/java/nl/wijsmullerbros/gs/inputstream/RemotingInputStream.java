package nl.wijsmullerbros.gs.inputstream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.UUID;

import nl.wijsmullerbros.gs.ChunkHolder;

import org.apache.commons.io.input.NullInputStream;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j_spaces.core.IJSpace;

/**
 * Client side stream that can read from a serverside stream by proxy.
 * @author bwijsmuller
 */
public class RemotingInputStream extends BufferedInputStream {

	Logger logger = LoggerFactory.getLogger(RemotingInputStream.class);
	
    private final GigaSpace gigaSpace;
    private long counter;
    private final UUID channelId;
    private final UrlSpaceConfigurer urlSpaceConfigurer;

    /**
     * Creates a new {@code RemotingInputStream}.
     * @param channelId 
     * @param urlSpaceConfigurer 
     */
    public RemotingInputStream(UUID channelId, UrlSpaceConfigurer urlSpaceConfigurer) {
        super(new NullInputStream(1));
        this.channelId = channelId;
        this.urlSpaceConfigurer = urlSpaceConfigurer;
        
        IJSpace space = urlSpaceConfigurer.space();
        GigaSpace gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
        
        this.gigaSpace = gigaSpace;
        counter = 0;
        fireFillBufferEvent();
    }
    
    private void fireFillBufferEvent() {
        System.out.println("Asking for buffer fill.");
        ChunkHolder event = new ChunkHolder();
        event.setChannelId(channelId.toString());
        event.setFillBufferChunk(true);
        event.setChunkId(-1l);
        gigaSpace.write(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean markSupported() {
        return false;
    }
    
    /**
     * Unsupported
     * <p>
     * {@inheritDoc}
     */
    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Unsupported
     * <p>
     * {@inheritDoc}
     */
    @Override
    public synchronized int available() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported
     * <p>
     * {@inheritDoc}
     */
    @Override
    public synchronized long skip(long n) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Unsupported
     * <p>
     * {@inheritDoc}
     */
    @Override
    public synchronized int read() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, -1);
    }
    
    /**
     * Length is ignored!
     * <p>
     * {@inheritDoc}
     */
    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int chosenOffset = off;
        
        ChunkHolder template = new ChunkHolder();
        template.setChannelId(channelId.toString());
        template.setChunkId(counter);
        template.setFillBufferChunk(false);
        logger.info("Asking for chunk: {}", template.getConcattedChunkId());
        
        ChunkHolder result = gigaSpace.take(template);
        if (result == null) {
            fireFillBufferEvent();
            //wait max 20 seconds
            result = gigaSpace.take(template, 20000);
        }
        
        if (result == null || Boolean.TRUE.equals(result.getClosingChunk())) {
        	logger.info("Read closing chunk, returning EOF.");
            return -1;
        } else {
            if (counter != result.getChunkId()) {
            	logger.info("Found chunk out of order: "+result.getConcattedChunkId());
            } else {
            	logger.info("Returning data for chunk: "+result.getConcattedChunkId());
            }
            
            byte[] data = result.getDataChunk();
            System.arraycopy(data, 0, b, chosenOffset, data.length);
            counter++;
            return data.length;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        //send cleanup signal to server
        ChunkHolder closeEvent = new ChunkHolder();
        closeEvent.setFillBufferChunk(false);
        closeEvent.setClosingChunk(true);
        gigaSpace.write(closeEvent);
        
        try {
            urlSpaceConfigurer.destroy();
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
        super.close();
    }
    
}
