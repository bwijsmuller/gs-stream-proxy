package nl.wijsmullerbros.gs;

import java.io.Serializable;
import java.util.UUID;

import nl.wijsmullerbros.gs.inputstream.RemotingInputStream;
import nl.wijsmullerbros.gs.outputstream.RemotingOutputStream;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.j_spaces.core.IJSpace;

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
        IJSpace space = new UrlSpaceConfigurer(remoteSpaceUri).space();
        GigaSpace gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
        return new RemotingOutputStream(channelId, gigaSpace);
    }
    
    /**
     * 
     * @return
     */
    public RemotingInputStream createRemotingInputStream() {
        IJSpace space = new UrlSpaceConfigurer(remoteSpaceUri).space();
        GigaSpace gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();
        return new RemotingInputStream(channelId, gigaSpace);
    }

}
