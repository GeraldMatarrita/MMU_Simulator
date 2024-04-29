public class Page {
    private Integer id = 1;
    private Integer physicalAddress;
    private Boolean inRealMemory = false;
    private Integer pId;

    private static Integer idCounter = 0;

    public Page(Integer pId) {
        this.id = idCounter++;
        this.pId = pId;
    }

    public Integer getId() {
        return id;
    }

    public Integer getPhysicalAddress() {
        return physicalAddress;
    }

    public void setPhysicalAddress(Integer physicalAddress) {
        this.physicalAddress = physicalAddress;
    }

    public Boolean getInRealMemory() {
        return inRealMemory;
    }

    public void setInRealMemory(Boolean inRealMemory) {
        this.inRealMemory = inRealMemory;
    }

    public Integer getPId() {
        return pId;
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
