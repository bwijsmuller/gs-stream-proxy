package nl.wijsmullerbros.gs;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import nl.wijsmullerbros.gs.inputstream.ChunkWritingListenerContainer;
import nl.wijsmullerbros.gs.outputstream.ChunkReadingListenerContainer;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.j_spaces.core.client.SpaceURL;

/**
 * @author bwijsmuller
 *
 */
public class StreamProxyFactory {

    /**
     * Creates a new {@code StreamProxyFactory}.
     */
    public StreamProxyFactory() {
        //default
    }

    /**
     * 
     * @param inputStream 
     * @return
     */
    public StreamProxy createRegisteredProxy(InputStream inputStream) {
        return createRegisteredProxy(null, inputStream);
    }
    
    /**
     * 
     * @param outputStream 
     * @return
     */
    public StreamProxy createRegisteredProxy(OutputStream outputStream) {
        return createRegisteredProxy(null, outputStream);
    }
    
    /**
     * @param gigaSpace
     * @param inputStream 
     * @return
     */
    public StreamProxy createRegisteredProxy(GigaSpace gigaSpace, InputStream inputStream) {
        UUID channelId = UUID.randomUUID();
        if (gigaSpace == null) {
            gigaSpace = createLocalSpace(channelId);
        }
        ChunkWritingListenerContainer container = new ChunkWritingListenerContainer(gigaSpace, channelId);
        container.setInputStream(inputStream);
        container.registerContainer();
        return createProxy(gigaSpace, channelId);
    }
    
    /**
     * @param channelId
     * @return
     */
    private GigaSpace createLocalSpace(UUID channelId) {
        //TODO: read groups from environment through constructor
        return new GigaSpaceConfigurer(new UrlSpaceConfigurer("/./streamProxy"+channelId.toString()+"?groups=testGroup")).gigaSpace();
    }

    /**
     * @param gigaSpace
     * @param outputStream 
     * @return
     */
    public StreamProxy createRegisteredProxy(GigaSpace gigaSpace, OutputStream outputStream) {
        UUID channelId = UUID.randomUUID();
        if (gigaSpace == null) {
            gigaSpace = createLocalSpace(channelId);
        }
        ChunkReadingListenerContainer container = new ChunkReadingListenerContainer(gigaSpace, channelId);
        container.setOutputStream(outputStream);
        container.registerContainer();
        return createProxy(gigaSpace, channelId);
    }

    /**
     * @param gigaSpace 
     * @param channelId
     * @param inputStream 
     * @param url
     * @return
     */
    private StreamProxy createProxy(GigaSpace gigaSpace, UUID channelId) {
        final SpaceURL spaceURL = gigaSpace.getSpace().getURL();
        final String spaceName = spaceURL.getSpaceName();
        final String[] lookupGroups = spaceURL.getLookupGroups();
        final String host = spaceURL.getHost();
        final String url = "jini://"+host+"/*/"+spaceName+"?groups="+lookupGroups[0];
        System.out.println("Resolved remote url from server space: "+url);
        
        StreamProxy streamProxy = new StreamProxy(channelId, url);
        return streamProxy;
    }

}
