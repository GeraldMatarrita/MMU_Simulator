public class Page {
    private final Integer id;
    private Integer physicalAddress;
    private Boolean inRealMemory = false;
    private final Integer pId;
    private boolean referenceBit = false; // SC

    private static Integer idCounter = 0;

    public Page(Integer pId) {
        this.id = idCounter++;
        this.pId = pId;
    }

    public Integer getId() {
        return id;
    }

    public Boolean getInRealMemory() {
        return inRealMemory;
    }

    public Integer getPhysicalAddress() {
        return physicalAddress;
    }

    public void setPhysicalAddress(Integer physicalAddress) {
        this.physicalAddress = physicalAddress;
    }

    public void setInRealMemory(Boolean inRealMemory) {
        this.inRealMemory = inRealMemory;
    }

    public static void setIdCounter(Integer idCounter) {
        Page.idCounter = idCounter;
    }

    public Integer getPId() {
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
