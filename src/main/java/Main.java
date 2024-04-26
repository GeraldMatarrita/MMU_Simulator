import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

//        try {
//            CreateFile.writeInstructionsToFile();
//        } catch (IOException e) {
//            System.err.println("An error occurred while writing to the file: " + e.getMessage());
//        }

        List<String> instructions = null;
        try {
            instructions = ReadFile.readLines("instructions.txt");
        } catch (IOException e) {
            System.err.println("An error occurred while reading the file: " + e.getMessage());
        }

        MMU.execute(instructions);
        MMU.printRealMemory();
        MMU.printVirtualMemory();
        MMU.printSymbolTable();
        System.out.println("\nFragmentation: " + MMU.getFragmentation() + " KB");
    }
}
