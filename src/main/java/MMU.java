import java.util.*;

public class MMU {

    private static List<Page> virtualMemory;
    private static Page[] realMemory;
    private static final Integer MAX_RAM_KB = 10; // Max space for physical memory
    private static final Integer KB = 1000;
    private static int remainingRAM;
    private static Map<Integer, List<Integer>> symbolTable;

    private static Integer fragmentation = 0;

    private static int ptrCounter = 1;

    public MMU() {
        virtualMemory = new ArrayList<>();
        realMemory = new Page[MAX_RAM_KB];
        remainingRAM = MAX_RAM_KB;
        symbolTable = new HashMap<>();
    }

    public static List<Page> getVirtualMemory() {
        return virtualMemory;
    }

    public static Page[] getRealMemory() {
        return realMemory;
    }

    public static Map<Integer, List<Integer>> getSymbolTable() {
        return symbolTable;
    }

    public static Integer getFragmentation() {
        return fragmentation;
    }
    /*
     * Create a new process in the memory
     * @param pid The process ID
     * @param size The size of the process in bytes
     * @return The pointer in the real memory where the process is stored
     */

    public static Integer new_(Integer pid, Integer size) {

        int remainingPages = calculatePagesNeeded(size);
        List<Integer> pages = new ArrayList<>();
        // Check if the RAM is not full
        if (remainingRAM > 0) {
            // Number of pages to be stored in the RAM
            int ramIterator = 0; // Pointer to iterate over the RAM

            // While there is RAM available and there are pages to store
            while (remainingRAM != 0 && remainingPages > 0) {
                // Check if the current position in the RAM is empty
                if (realMemory[ramIterator] == null) {
                    // Create a new page and store it in the RAM
                    Page page = new Page(pid);
                    page.setInRealMemory(true);
                    page.setPhysicalAddress(ptrCounter);
                    realMemory[ramIterator] = page;
                    pages.add(page.getId());
                    remainingPages--;
                    remainingRAM--;
                }

                // Move the pointer to the next position in the RAM or reset it
                if (ramIterator == MAX_RAM_KB - 1) {
                    ramIterator = 0;
                } else {
                    ramIterator++;
                }
            }
        }

        // If there are remaining pages, store them in the virtual memory
        if (remainingPages > 0) {
            for (int i = 0; i < remainingPages; i++) {
                Page page = new Page(pid);
                virtualMemory.add(page);
                pages.add(page.getId());
            }
        }

        // Store the pages in the symbol table with the corresponding PID
        symbolTable.put(ptrCounter, pages);
        ptrCounter++;


        return ptrCounter;
    }

    /*
     * Use the memory assigned to the given pointer
     * @param ptr The pointer to the memory assigned to the process
     * @throws NotInRealMemoryException If the pointer is not in the real memory
     */

    public static void use(Integer ptr) throws Exception {
        // Check if the pointer is in the symbol table if not throw an exception
        if (symbolTable.containsKey(ptr)){
            List<Integer> pagesIds = symbolTable.get(ptr);
            boolean pageInRealMemory = false;
            for (Integer pageId : pagesIds) {
                for (Page page : realMemory) {
                    if (page != null && Objects.equals(page.getId(), pageId)) {
                        pageInRealMemory = true;
                        System.out.println("Using page " + pageId + " of process " + page.getPId());
                        break;
                    }
                }
                if (!pageInRealMemory) {
                    System.out.println("Page fault, page " + pageId + " is not in the real memory. Please use a pagination algorithm to load the page into the real memory.");
                    return;
                }
            }
        } else {
            throw new Exception("The pointer is not in the symbol table");
        }
    }
    /*
     * Delete all the pages in the real memory that belong to the given pointer
     * @param ptr The pointer to the memory assigned to the process
     */

    public static void delete(Integer ptr) {
        // Check if the pointer is in the symbol table
        if (symbolTable.containsKey(ptr)) {
            // Iterate over the real memory to free the memory used by the pointer

            for (int i = 0; i < realMemory.length; i++) {
                if (realMemory[i] != null && Objects.equals(realMemory[i].getPhysicalAddress(), ptr)) {
                    realMemory[i] = null;
                    remainingRAM++;
                }
            }

            // Remove the pointer from the symbol table
            symbolTable.remove(ptr);
        }
    }
    /*
     * Kill the process with the given PID
     * @param pid The process ID
     */

    public static void kill(Integer pid) {

        List<Integer> pointersToRemove = new ArrayList<>();
        // Iterate over the real memory to free the memory used by the process
        for (int i = 0; i < realMemory.length; i++) {
            if (realMemory[i] != null && Objects.equals(realMemory[i].getPId(), pid)) {
                // Add the pointer to the list of pointers to remove from the symbol table
                if(!pointersToRemove.contains(realMemory[i].getPhysicalAddress())){
                    pointersToRemove.add(realMemory[i].getPhysicalAddress());
                }
                realMemory[i] = null;
                remainingRAM++;
            }
        }

        // Remove the pointers with their pages from the symbol table
        for (Integer ptr : pointersToRemove) {
            symbolTable.remove(ptr);
        }

        // Remove the pages from the virtual memory
        virtualMemory.removeIf(page -> Objects.equals(page.getPId(), pid));
    }
    /*
     * Calculate the number of pages needed to store the process
     * @param size The size of the process in bytes
     * @return The number of pages needed to store the process
     */

    private static int calculatePagesNeeded(Integer size) {
        int result;
        // Check if the size is greater than 1KB
        if (size > KB) {
            // Calculate the number of pages needed
            result = size / KB;
            int residue = size % KB;
            if (residue > 0) {
                fragmentation += KB - residue;
            }
            if (residue > 0) {
                result++;
            }
        } else {
            // If the size is less than 1KB, then only one page is needed
            result = 1;
            fragmentation += KB - size;
        }
        return result;
    }

    /*
     * Execute the instructions in the given list
     * @param instructions The list of instructions to execute
     */
    public static void execute(List<String> instructions) {
        // Check if there are instructions to execute
        if (instructions == null) {
            System.out.println("No instructions to execute");
            return;
        }

        // Initialize the variables to store the command, process ID, size, command, pointer
        Integer pid = null;
        Integer size = null;
        String command;
        Integer ptr = null;

        // Iterate over the instructions to execute them
        for (String instruction : instructions) {
            // Split the instruction to get the command and the arguments
            String[] parts = instruction.split("\\(");
            command = parts[0];
            if (command.equals("new")){
                String[] args = parts[1].split(",");
                pid = Integer.parseInt(args[0]);
                size = Integer.parseInt(args[1].substring(0, args[1].length() - 1));
            } else if (command.equals("kill")) {
                pid = Integer.parseInt(parts[1].substring(0, parts[1].length() - 1));
            } else {
                ptr = Integer.parseInt(parts[1].substring(0, parts[1].length() - 1));
            }
            // Execute the command depending on the instruction
            try {
                switch (command) {
                    case "new":
                        new_(pid, size);
                        break;
                    case "delete":
                        delete(ptr);
                        break;
                    case "kill":
                        kill(pid);
                        break;
                    case "use":
                        use(pid);
                        break;
                    default:
                        System.out.println("Invalid command: " + command);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /*
     * Print the pages in the real memory
     */
    public static void printRealMemory() {
        System.out.println("=================================");
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
        System.out.println("=================================");
    }

    /*
     * Print the pages in the virtual memory
     */
    public static void printVirtualMemory() {
        System.out.println("=================================");
        List<Page> virtualMemory = MMU.getVirtualMemory();
        System.out.println("\nVirtual memory: ");
        for (Page page : virtualMemory) {
            System.out.println("Page ID: " + page.getId() + ", Process ID: " + page.getPId());
        }
        System.out.println("=================================");
    }

    /*
     * Print the symbol table
     */
    public static void printSymbolTable() {
        System.out.println("=================================");
        for (Map.Entry<Integer, List<Integer>> entry : symbolTable.entrySet()) {
            System.out.print("Ptr: " + entry.getKey() + ", Pages: ");
            for (Integer page : entry.getValue()) {
                System.out.print(page + ", ");
            }
            System.out.println();
        }
        System.out.println("=================================");
    }
}
