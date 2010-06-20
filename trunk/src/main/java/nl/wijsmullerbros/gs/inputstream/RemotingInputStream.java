/*
 * Copyright (c) Qiy Intellectual Property B.V. and licensors, 2007-2010. All rights reserved.
 */
package nl.wijsmullerbros.gs.inputstream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.UUID;

import nl.wijsmullerbros.gs.ChunkHolder;

import org.apache.commons.io.input.NullInputStream;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.space.UrlSpaceConfigurer;

/**
 * @author bwijsmuller
 *
 */
public class RemotingInputStream extends BufferedInputStream {

    private final GigaSpace gigaSpace;
    private long counter;
    private final UUID channelId;

    /**
     * Creates a new {@code RemotingInputStream}.
     * @param channelId 
     * @param gigaSpace 
     */
    public RemotingInputStream(UUID channelId, GigaSpace gigaSpace) {
        super(new NullInputStream(1));
        this.channelId = channelId;
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
        System.out.println("Asking for chunk: "+template.getConcattedChunkId());
        
        ChunkHolder result = gigaSpace.take(template);
        if (result == null) {
            fireFillBufferEvent();
            //wait max 20 seconds
            result = gigaSpace.take(template, 20000);
        }
        
        if (result == null || Boolean.TRUE.equals(result.getClosingChunk())) {
            System.out.println("Read closing chunk, returning EOF.");
            return -1;
        } else {
            if (counter != result.getChunkId()) {
                System.err.println("Found chunk out of order: "+result.getConcattedChunkId());
            } else {
                System.out.println("Returning data for chunk: "+result.getConcattedChunkId());
            }
            
            byte[] data = result.getDataChunk();
            System.arraycopy(data, 0, b, chosenOffset, data.length);
            counter++;
            return data.length;
        }
    }
    
}
