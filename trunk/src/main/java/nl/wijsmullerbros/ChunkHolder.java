package nl.wijsmullerbros;

import java.io.Serializable;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

/**
 * @author bwijsmuller
 *
 */
@SpaceClass(fifoSupport=FifoSupport.ALL)
public class ChunkHolder implements Serializable {

    private static final long serialVersionUID = 2482205196156990057L;
    
    private static final String SPLIT_CHAR = "#";
    private byte[] dataChunk;
    private Integer chunkId;
    private String channelId;
    private String concattedChunkId;
    private Boolean closingChunk;

    /**
     * Creates a new {@code ChunkHolder}.
     * @param dataChunk 
     */
    public ChunkHolder(byte[] dataChunk) {
        this.dataChunk = dataChunk.clone();
    }
    
    /**
     * Creates a new {@code ChunkHolder}.
     */
    public ChunkHolder() {
        //default for template matching
    }

    /**
     * @param channelId
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
        concatenate();
    }
    
    /**
     * 
     * @return
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Sets the chunkId
     * @param chunkId the chunkId to set
     */
    public void setChunkId(Integer chunkId) {
        this.chunkId = chunkId;
        concatenate();
    }
    
    /**
     * 
     * @return
     */
    public Integer getChunkId() {
        return this.chunkId;
    }
    
    /**
     * Gets the chunkId
     * @return Integer the chunkId
     */
    @SpaceId
    public String getConcattedChunkId() {
        return concattedChunkId;
    }
    
    /**
     * 
     * @param concattedChunkId
     */
    public void setConcattedChunkId(String concattedChunkId) {
        this.concattedChunkId = concattedChunkId;
        splitConcat(concattedChunkId);
    }
    
    /**
     * Gets the dataChunk
     * @return byte[] the dataChunk
     */
    public byte[] getDataChunk() {
        return dataChunk;
    }
    
    /**
     * Set the data
     * @param {@code byte[]} dataChunk
     */
    public void setDataChunk(byte[] dataChunk) {
        this.dataChunk = dataChunk;
    }
    
    /**
     * Sets the finalChunk
     * @param closingChunk the closingChunk to set
     */
    public void setClosingChunk(Boolean closingChunk) {
        this.closingChunk = closingChunk;
    }

    /**
     * Gets the finalChunk
     * @return Boolean the closingChunk
     */
    public Boolean getClosingChunk() {
        return closingChunk;
    }

    //================== helpers ======================//
    
    private void splitConcat(String concattedChunkId) {
        String[] split = concattedChunkId.split(SPLIT_CHAR);
        this.channelId = split[0];
        this.chunkId = Integer.valueOf(split[1]);
    }
    
    private void concatenate() {
        if (channelId != null && chunkId != null) {
            this.concattedChunkId = channelId+SPLIT_CHAR+chunkId;
        }
    }
    
}
