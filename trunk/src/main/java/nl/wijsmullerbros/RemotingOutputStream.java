package nl.wijsmullerbros;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.output.NullOutputStream;
import org.openspaces.core.GigaSpace;

/**
 * @author bwijsmuller
 *
 */
public class RemotingOutputStream extends BufferedOutputStream {

    private final UUID channelId;
    private final GigaSpace space;
    private int counter = 0;
    private int byteCounter = 0;

    /**
     * Creates a new {@code RemotingOutputStream}.
     * @param channelId 
     * @param space 
     */
    public RemotingOutputStream(UUID channelId, GigaSpace space) {
        super(new NullOutputStream());
        //TODO: maybe optionally offer constructor that also writes to local file (as failover backup)
        this.channelId = channelId;
        this.space = space;
        System.out.println("Created remoting stream for space: "+space.getSpace().getURL());
        System.out.println("Channel id: "+channelId);
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
        System.out.println("Written ["+byteCounter+"] total bytes to remote location, sending close event.");
        ChunkHolder chunkHolder = new ChunkHolder();
        chunkHolder.setChannelId(channelId.toString());
        chunkHolder.setChunkId(counter);
        chunkHolder.setClosingChunk(true);
        space.write(chunkHolder, 20000);
        super.close();
    }

}
