import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        try {
            CreateFile.writeInstructionsToFile();
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }

        List<String> instructions = new ArrayList<>();
//        try {
//            instructions = ReadFile.readLines("instructions.txt");
//        } catch (IOException e) {
//            System.err.println("An error occurred while reading the file: " + e.getMessage());
//        }
        instructions.add("new(0,10000)"); // ptr 0
        instructions.add("new(1,4500)"); // ptr 1
        instructions.add("new(2,5000)"); // ptr 2
        instructions.add("use(0)");
        instructions.add("use(0)");
        instructions.add("use(2)");
        instructions.add("use(0)");
        instructions.add("use(2)");
        instructions.add("use(1)");
        instructions.add("delete(2)");
        instructions.add("new(1,2000)"); // ptr 3

        MMU.executeOptimal(instructions);
        System.out.println("----------------------------------");
        System.out.println("Optimal Algorithm");
        System.out.println("----------------------------------");
        MMU.printRealMemory();
        MMU.printVirtualMemory();
        MMU.printSymbolTable();
        System.out.println("Page faults: " + MMU.getPageFaults());
        MMU.clean();
        System.out.println("----------------------------------");
        System.out.println("Other Algorithms");
        System.out.println("----------------------------------");
        MMU.execute(instructions);
        MMU.printRealMemory();
        MMU.printVirtualMemory();
        MMU.printSymbolTable();
        System.out.println("\nFragmentation: " + MMU.getFragmentation() + " KB");
        System.out.println("Page faults: " + MMU.getPageFaults());
    }
}
