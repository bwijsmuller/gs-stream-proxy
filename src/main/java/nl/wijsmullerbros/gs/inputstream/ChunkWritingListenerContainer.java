/*
 * Copyright (c) Qiy Intellectual Property B.V. and licensors, 2007-2010. All rights reserved.
 */
package nl.wijsmullerbros.gs.inputstream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import nl.wijsmullerbros.gs.ChunkHolder;

import org.apache.commons.io.IOUtils;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.events.notify.SimpleNotifyContainerConfigurer;
import org.openspaces.events.notify.SimpleNotifyEventListenerContainer;
import org.springframework.transaction.TransactionStatus;

import com.j_spaces.core.client.SQLQuery;

/**
 * @author bwijsmuller
 *
 */
public class ChunkWritingListenerContainer {

    private static final int READ_BUFFERSIZE = 1024;
    protected static final int CHUNKS_TO_BUFFER = 10;

    private UUID channelId;
    private SimpleNotifyEventListenerContainer listenerContainer;
    private final GigaSpace gigaSpace;
    private BufferedInputStream inputStream;

    /**
     * Creates a new {@code ChunkWritingListenerContainer}.
     * @param gigaSpace
     * @param channelId
     */
    public ChunkWritingListenerContainer(GigaSpace gigaSpace, UUID channelId) {
        this.gigaSpace = gigaSpace;
        this.channelId = channelId;
    }

    /**
     * 
     */
    public void registerContainer() {
        final String queryString = "channelId = ? AND fillBufferChunk = true";
        SQLQuery<ChunkHolder> sqlQuery = new SQLQuery<ChunkHolder>(ChunkHolder.class, queryString);
        sqlQuery.setParameters(channelId.toString());
        
        SpaceDataEventListener<ChunkHolder> eventListener = new SpaceDataEventListener<ChunkHolder>() {
            long counter = 0;
            @Override
            public void onEvent(ChunkHolder data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
                System.out.println("Received fill buffer ("+CHUNKS_TO_BUFFER+") event chunk: "+data.getConcattedChunkId());
                try {
                    byte[] buffer = new byte[READ_BUFFERSIZE];
                    for (int i = 0; i < CHUNKS_TO_BUFFER; i++) {
                        int nrReadBytes = inputStream.read(buffer);
                        if (nrReadBytes == -1) {
                            System.out.println("End of stream read (closing stream), sending closed message.");
                            
                            //send close message
                            ChunkHolder chunk = new ChunkHolder(buffer.clone());
                            chunk.setChannelId(channelId.toString());
                            chunk.setChunkId(counter);
                            chunk.setFillBufferChunk(false);
                            chunk.setClosingChunk(true);
                            gigaSpace.write(chunk);
                            
                            //close stream
                            IOUtils.closeQuietly(inputStream);
                            
                            //TODO: separate messages and data chunks, and add cleanup state to remove space and
                            // container
                            break;
                        } else {
                            //write chunk of data
                            ChunkHolder chunk = new ChunkHolder(buffer.clone());
                            chunk.setChannelId(channelId.toString());
                            chunk.setChunkId(counter);
                            chunk.setFillBufferChunk(false);
                            chunk.setClosingChunk(false);
                            System.out.println("Writing chunk to space: "+chunk.getConcattedChunkId());
                            gigaSpace.write(chunk);
                        }
                        counter++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            private void cleanup(GigaSpace gigaSpace) {
                System.out.println("Found closing chunk, server stream will close...");
                
                ChunkWritingListenerContainer.this.listenerContainer.destroy();
                System.out.println("Destroyed listener container...");
            }
        };
        
        listenerContainer = new SimpleNotifyContainerConfigurer(gigaSpace)
            .template(sqlQuery)
            .eventListener(eventListener)
            .performTakeOnNotify(true)
            .notifyContainer();
    }

    /**
     * @param inputStream
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = new BufferedInputStream(inputStream);
    }
}
