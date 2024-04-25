import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        MMU mmu = new MMU();

        try {
            mmu.new_(1, 2003);
            mmu.new_(1, 1000);
            mmu.new_(3, 1000);
            mmu.delete(1);
            mmu.new_(4, 500);
            mmu.kill(1);
            mmu.new_(4, 4000);
            mmu.use(1);
        } catch (NotInRealMemoryException e) {
            System.out.println(e.getMessage());
        }

        Page[] realMemory = MMU.getRealMemory();
        System.out.println("Real memory: ");
        int iter = 0;
        for (Page page : realMemory) {
            if (page != null) {
                System.out.print(iter++ + " ");
                System.out.println(page);
            } else {
                System.out.println(iter++ + " null");
            }
        }

        List<Page> virtualMemory = MMU.getVirtualMemory();
        System.out.println("\nVirtual memory: ");
        for (Page page : virtualMemory) {
            System.out.println("Page ID: " + page.getId() + ", Process ID: " + page.getPId());
        }

        Map<Integer, List<Integer>> symbolTable = MMU.getSymbolTable();
        System.out.println("\nSymbol table: ");
        for (Map.Entry<Integer, List<Integer>> entry : symbolTable.entrySet()) {
            System.out.print("Ptr: " + entry.getKey() + ", Pages: ");
            for (Integer page : entry.getValue()) {
                System.out.print(page + ", ");
            }
            System.out.println();
        }
    }
}
