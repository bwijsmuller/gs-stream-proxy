package nl.wijsmullerbros.gs;

import java.io.Serializable;
import java.util.UUID;

import nl.wijsmullerbros.gs.inputstream.RemotingInputStream;
import nl.wijsmullerbros.gs.outputstream.RemotingOutputStream;

import org.openspaces.core.space.UrlSpaceConfigurer;

/**
 * @author bwijsmuller
 *
 */
public class StreamProxy implements Serializable {

    private static final long serialVersionUID = -7238809794566331294L;
    
    private final String remoteSpaceUri;
    private UUID channelId;
    
    /**
     * Creates a new {@code StreamProxy}.
     * @param channelId 
     * @param remoteSpaceUri 
     */
    public StreamProxy(UUID channelId, String remoteSpaceUri) {
        this.channelId = channelId;
        this.remoteSpaceUri = remoteSpaceUri;
    }
    
    /**
     * 
     * @return
     */
    public RemotingOutputStream createRemotingOutputStream() {
        UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer(remoteSpaceUri);
        return new RemotingOutputStream(channelId, urlSpaceConfigurer);
    }
    
    /**
     * 
     * @return
     */
    public RemotingInputStream createRemotingInputStream() {
        UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer(remoteSpaceUri);
        return new RemotingInputStream(channelId, urlSpaceConfigurer);
    }

}
