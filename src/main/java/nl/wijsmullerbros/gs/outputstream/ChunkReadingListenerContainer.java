package nl.wijsmullerbros.gs.outputstream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import nl.wijsmullerbros.gs.ChunkHolder;

import org.apache.commons.io.IOUtils;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.events.notify.SimpleNotifyContainerConfigurer;
import org.openspaces.events.notify.SimpleNotifyEventListenerContainer;
import org.springframework.transaction.TransactionStatus;

import com.j_spaces.core.client.SQLQuery;

/**
 * Listener container that reads chunks from the space,
 * and writes them to an arbitrary outputstream.
 * 
 * @author bwijsmuller
 */
public class ChunkReadingListenerContainer {

    private UUID channelId;
    private SimpleNotifyEventListenerContainer listenerContainer;
    private final GigaSpace gigaSpace;
    private OutputStream outputStream;
    private final UrlSpaceConfigurer configurer;

    /**
     * Creates a new {@code ChunkReadingListenerContainer}.
     * @param configurer
     * @param channelId
     */
    public ChunkReadingListenerContainer(UrlSpaceConfigurer configurer, UUID channelId) {
        this.configurer = configurer;
        this.gigaSpace = new GigaSpaceConfigurer(configurer).gigaSpace();
        this.channelId = channelId;
    }

    /**
     * 
     */
    public void registerContainer() {
        SQLQuery<ChunkHolder> sqlQuery = new SQLQuery<ChunkHolder>(ChunkHolder.class, "channelId = ?");
        sqlQuery.setParameters(channelId.toString());
        
        SpaceDataEventListener<ChunkHolder> eventListener = new SpaceDataEventListener<ChunkHolder>() {
            long counter = 0;
            @Override
            public void onEvent(ChunkHolder data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
                System.out.println("Received chunk: "+data.getConcattedChunkId());
                try {
                    if (Boolean.TRUE.equals(data.getClosingChunk())) {
                        cleanup();
                        return;
                    }
                    //if event data is not the next chunk, log error
                    if (data.getChunkId() > counter +1) {
                        System.err.println("Rewriting chunk out of order: "+data.getChunkId());
                    } else {
                        outputStream.write(data.getDataChunk());
                        counter++;
                    }
                    System.out.println("Expecting next chunk: "+counter);                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            private void cleanup() {
                try {
                    System.out.println("Found closing chunk, server stream will close...");
                    //close stream
                    IOUtils.closeQuietly(outputStream);
                    //destroy listener
                    ChunkReadingListenerContainer.this.listenerContainer.destroy();
                    System.out.println("Destroyed listener container...");
                } finally {
                    try {
                        configurer.destroy();
                        System.out.println("Destroyed space...");
                    } catch (Exception e) {
                        System.err.println("Cannot destroy space...");
                        e.printStackTrace();
                    }
                }
            }
        };
        
        listenerContainer = new SimpleNotifyContainerConfigurer(gigaSpace)
            .template(sqlQuery)
            .eventListener(eventListener)
            .fifo(true)
            .performTakeOnNotify(true)
            .notifyContainer();
    }

    /**
     * @param outputStream
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        
    }

    /**
     * @return
     */
    public GigaSpace getSpace() {
        return gigaSpace;
    }

}
