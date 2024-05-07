public class Page {
    private final int id;
    private Integer physicalAddress;
    private Boolean inRealMemory = false;
    private final int pId;
    private boolean referenceBit = false; // SC
    private int loadedTime; // LRU

    public Page(Integer pId, Integer id) {
        this.id = id;
        this.pId = pId;
        this.loadedTime = 0;
    }

    public int getId() {
        return id;
    }

    public Boolean getInRealMemory() {
        return inRealMemory;
    }

    public Integer getPhysicalAddress() {
        return physicalAddress;
    }

    public int getLoadedTime() {
        return loadedTime;
    }

    public void setPhysicalAddress(Integer physicalAddress) {
        this.physicalAddress = physicalAddress;
    }

    public void setInRealMemory(Boolean inRealMemory) {
        this.inRealMemory = inRealMemory;
    }

    public void setLoadedTime(int loadedTime) {
        this.loadedTime += loadedTime;
    }

    public int getPId() {
        return pId;
    }

    public boolean getReferenceBit() {
        return referenceBit;
    }

    public void setReferenceBit(boolean referenceBit) {
        this.referenceBit = referenceBit;
    }

    @Override
    public String toString() {
        return "Page{" +
                "id=" + id +
                ", physicalAddress=" + physicalAddress +
                ", inRealMemory=" + inRealMemory +
                ", pId=" + pId +
                '}';
    }
}
