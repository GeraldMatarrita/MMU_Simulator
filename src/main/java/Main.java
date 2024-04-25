public class Main {
    public static void main(String[] args) {
        MMU mmu = new MMU();


        mmu.new_(1, 4003);
        mmu.new_(2, 2000);
        mmu.delete(1);
        mmu.new_(3, 1000);
    }
}
