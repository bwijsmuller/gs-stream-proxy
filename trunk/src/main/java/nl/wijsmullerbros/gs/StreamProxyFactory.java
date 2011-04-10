package nl.wijsmullerbros.gs;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import nl.wijsmullerbros.gs.inputstream.ChunkWritingListenerContainer;
import nl.wijsmullerbros.gs.outputstream.ChunkReadingListenerContainer;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j_spaces.core.client.SpaceURL;

/**
 * @author bwijsmuller
 *
 */
public class StreamProxyFactory {

	Logger logger = LoggerFactory.getLogger(StreamProxyFactory.class);
	
    /**
     * Creates a new {@code StreamProxyFactory}.
     */
    public StreamProxyFactory() {
        //default
    }

    /**
     * Create a proxy to send to the client.
     * @param inputStream 
     * @return
     */
    public StreamProxy createRegisteredProxy(InputStream inputStream) {
        return createRegisteredProxy(null, inputStream);
    }
    
    /**
     * Create a proxy to send to the client.
     * @param outputStream 
     * @return
     */
    public StreamProxy createRegisteredProxy(OutputStream outputStream) {
        return createRegisteredProxy(null, outputStream);
    }
    
    /**
     * Create a proxy to send to the client.
     * @param configurer 
     * @param inputStream 
     * @return
     */
    public StreamProxy createRegisteredProxy(UrlSpaceConfigurer configurer, InputStream inputStream) {
        UUID channelId = UUID.randomUUID();
        if (configurer == null) {
            configurer = createLocalSpaceConfigurer(channelId);
        }
        ChunkWritingListenerContainer container = new ChunkWritingListenerContainer(configurer, channelId);
        container.setInputStream(inputStream);
        container.registerContainer();
        GigaSpace space = container.getSpace();
        
        return createProxy(space, channelId);
    }
    
    /**
     * Create a proxy to send to the client.
     * @param gigaSpace
     * @param outputStream 
     * @return
     */
    public StreamProxy createRegisteredProxy(UrlSpaceConfigurer configurer, OutputStream outputStream) {
        UUID channelId = UUID.randomUUID();
        if (configurer == null) {
            configurer = createLocalSpaceConfigurer(channelId);
        }
        ChunkReadingListenerContainer container = new ChunkReadingListenerContainer(configurer, channelId);
        container.setOutputStream(outputStream);
        container.registerContainer();
        GigaSpace gigaSpace = container.getSpace();
        
        return createProxy(gigaSpace, channelId);
    }
    
    //================== helper methods ======================//
    
    private UrlSpaceConfigurer createLocalSpaceConfigurer(UUID channelId) {
        //TODO: read groups from environment through constructor
        return new UrlSpaceConfigurer("/./streamProxy"+channelId.toString()+"?groups=testGroup");
    }

    private StreamProxy createProxy(GigaSpace space, UUID channelId) {
        final String url = extractRemoteSpaceUrl(space);
        StreamProxy streamProxy = new StreamProxy(channelId, url);
        return streamProxy;
    }

    private String extractRemoteSpaceUrl(GigaSpace space) {
        final SpaceURL spaceURL = space.getSpace().getURL();
        final String spaceName = spaceURL.getSpaceName();
        final String[] lookupGroups = spaceURL.getLookupGroups();
        final String host = spaceURL.getHost();
        final String url = "jini://"+host+"/*/"+spaceName+"?groups="+lookupGroups[0];
        
        logger.info("Resolved remote url from server space: {}", url);
        return url;
    }

}
