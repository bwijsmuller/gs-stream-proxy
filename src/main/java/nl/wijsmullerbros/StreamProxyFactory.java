package nl.wijsmullerbros;

import java.io.OutputStream;
import java.util.UUID;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.j_spaces.core.client.SpaceURL;

/**
 * @author bwijsmuller
 *
 */
public class StreamProxyFactory {

    private final OutputStream outputStream;

    /**
     * Creates a new {@code StreamProxyFactory}.
     * @param fileOutputStream
     */
    public StreamProxyFactory(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * 
     * @return
     */
    public StreamProxy createRegisteredProxy() {
        UUID channelId = UUID.randomUUID();
        //TODO: read groups from environment through constructor
        GigaSpace gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("/./streamProxy"+channelId+"?groups=testGroup")).gigaSpace();
        return createProxy(gigaSpace, channelId);
    }
    
    /**
     * @param gigaSpace
     * @return
     */
    public StreamProxy createRegisteredProxy(GigaSpace gigaSpace) {
        UUID channelId = UUID.randomUUID();
        return createProxy(gigaSpace, channelId);
    }

    /**
     * @param gigaSpace 
     * @param channelId
     * @param url
     * @return
     */
    private StreamProxy createProxy(GigaSpace gigaSpace, UUID channelId) {
        final SpaceURL spaceURL = gigaSpace.getSpace().getURL();
        final String spaceName = spaceURL.getSpaceName();
        final String[] lookupGroups = spaceURL.getLookupGroups();
        final String host = spaceURL.getHost();
        final String url = "jini://"+host+"/*/"+spaceName+"?groups="+lookupGroups[0];
        
        ChunkListenerContainer container = new ChunkListenerContainer(gigaSpace, channelId);
        container.setOutputStream(outputStream);
        container.registerPollingContainer();
        
        StreamProxy streamProxy = new StreamProxy(channelId, url);
        return streamProxy;
    }

}
