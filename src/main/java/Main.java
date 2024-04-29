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
        instructions.add("new(1,14200)");
        instructions.add("new(2,4500)");
        instructions.add("use(0)");
        instructions.add("use(1)");

        MMU.execute(instructions);
        MMU.printRealMemory();
        MMU.printVirtualMemory();
        MMU.printSymbolTable();
        System.out.println("\nFragmentation: " + MMU.getFragmentation() + " KB");
        System.out.println("Page faults: " + MMU.getPageFaults());
    }
}
