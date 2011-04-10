package nl.wijsmullerbros.gs.outputstream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import nl.wijsmullerbros.gs.ChunkHolder;
import nl.wijsmullerbros.gs.inputstream.RemotingInputStream;

import org.apache.commons.io.output.NullOutputStream;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j_spaces.core.IJSpace;

/**
 * Client side stream that can write to a server side stream by proxy
 * @author bwijsmuller
 */
public class RemotingOutputStream extends BufferedOutputStream {

	Logger logger = LoggerFactory.getLogger(RemotingInputStream.class);
	
    private final UUID channelId;
    private final GigaSpace space;
    private long counter = 0;
    private long byteCounter = 0;
    private final UrlSpaceConfigurer urlSpaceConfigurer;

    /**
     * Creates a new {@code RemotingOutputStream}.
     * @param channelId 
     * @param urlSpaceConfigurer 
     */
    public RemotingOutputStream(UUID channelId, UrlSpaceConfigurer urlSpaceConfigurer) {
        super(new NullOutputStream());
        
        //TODO: maybe optionally offer constructor that also writes to local file (as failover backup)
        this.channelId = channelId;
        this.urlSpaceConfigurer = urlSpaceConfigurer;
        
        IJSpace space = urlSpaceConfigurer.space();
        GigaSpace gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
        
        this.space = gigaSpace;
        logger.info("Created remoting stream for space: {}", space.getURL());
        logger.info("Channel id: {}", channelId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b) throws IOException {
        ChunkHolder chunkHolder = new ChunkHolder(b);
        chunkHolder.setChannelId(channelId.toString());
        chunkHolder.setChunkId(counter);
        space.write(chunkHolder, 5000);
        counter++;
        byteCounter += b.length;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        byte[] chunk = new byte[len];
        System.arraycopy(b, off, chunk, 0, len);
        write(chunk);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void write(int b) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
    	logger.info("Written [{}] total bytes to remote location, sending close event.", byteCounter);
        ChunkHolder chunkHolder = new ChunkHolder();
        chunkHolder.setChannelId(channelId.toString());
        chunkHolder.setChunkId(counter);
        chunkHolder.setClosingChunk(true);
        space.write(chunkHolder, 20000);
        
        try {
            urlSpaceConfigurer.destroy();
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
        super.close();
    }

}
