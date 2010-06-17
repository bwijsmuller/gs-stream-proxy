package nl.wijsmullerbros;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.events.notify.SimpleNotifyContainerConfigurer;
import org.openspaces.events.notify.SimpleNotifyEventListenerContainer;
import org.springframework.transaction.TransactionStatus;

import com.j_spaces.core.client.SQLQuery;

/**
 * @author bwijsmuller
 *
 */
public class ChunkListenerContainer {

    private UUID channelId;
    private SimpleNotifyEventListenerContainer listenerContainer;
    private final GigaSpace gigaSpace;
    private OutputStream outputStream;

    /**
     * Creates a new {@code ChunkListenerContainer}.
     * @param gigaSpace
     * @param channelId
     */
    public ChunkListenerContainer(GigaSpace gigaSpace, UUID channelId) {
        this.gigaSpace = gigaSpace;
        this.channelId = channelId;
    }

    /**
     * 
     */
    public void registerPollingContainer() {
        SQLQuery<ChunkHolder> sqlQuery = new SQLQuery<ChunkHolder>(ChunkHolder.class, "channelId = ?");
        sqlQuery.setParameters(channelId.toString());
        
        SpaceDataEventListener<ChunkHolder> eventListener = new SpaceDataEventListener<ChunkHolder>() {
            int counter = 0;
            @Override
            public void onEvent(ChunkHolder data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
                System.out.println("Received chunk: "+data.getConcattedChunkId());
                try {
                    if (Boolean.TRUE.equals(data.getClosingChunk())) {
                        System.out.println("Found closing chunk, server stream will close...");
                        //close stream
                        IOUtils.closeQuietly(outputStream);
                        //destroy listener
                        ChunkListenerContainer.this.listenerContainer.destroy();
                        System.out.println("Destroyed listener container.");
                    } else {
                        //if event data is not the next chunk, put in buffer
                        if (data.getChunkId() > counter +1) {
                            System.err.println("Rewriting chunk out of order: "+data.getChunkId());
                        } else {
                            outputStream.write(data.getDataChunk());
                            counter++;
                        }
                        System.out.println("Expecting next chunk: "+counter);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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

}
