import java.util.*;

public class MMU {

    private static final Integer MAX_RAM_KB = 15; // Max space for physical memory
    private static final Integer KB = 1000; // 1KB = 1000 bytes

    private static int remainingRAM = MAX_RAM_KB; // Remaining space in the physical memory
    private static final List<Page> virtualMemory = new ArrayList<>(); // Virtual memory
    private static final Page[] realMemory = new Page[MAX_RAM_KB]; // Real memory  (physical memory/RAM)
    private static final Map<Integer, List<Page>> symbolTable = new HashMap<>(); // Symbol table (Memory map)

    private static final Stack<Page> mruPageStack = new Stack<>(); // Stack to store the most recently used pages
    private static Integer fragmentation = 0;  // Amount of fragmentation in the memory
    private static int ptrCounter = 0;  // Pointer counter for id
    private static int paginationAlgorithm; // Number of the pagination algorithm chosen by the user
    private static int pageFaults; // Number of page faults
    private static List<String> instructions;

    public static Integer getFragmentation() {
        return fragmentation;
    }

    public static int getPageFaults() {
        return pageFaults;
    }

    /*
     * Create a new process in the memory
     * @param pid The process ID
     * @param size The size of the process in bytes
     * @return The pointer in the real memory where the process is stored
     */
    public static Integer new_(Integer pid, Integer size) {
        // Calculate the number of pages needed to store the process
        int remainingPages = calculatePagesNeeded(size);
        // Check if there is enough space in the real memory to store the process
        if (remainingRAM >= remainingPages) {
            storeNewPages(pid, remainingPages);
        } else {
            // If there is not enough space, then free the memory using the chosen pagination algorithm
            paginationAlgorithm(remainingPages);
            storeNewPages(pid, remainingPages);
        }

        return ptrCounter - 1;
    }

    /*
     * Use the memory assigned to the given pointer
     * @param ptr The pointer to the memory assigned to the process
     * @throws NotInRealMemoryException If the pointer is not in the real memory
     */
    public static void use(Integer ptr) throws Exception {
        if (!symbolTable.containsKey(ptr)) {
            throw new Exception("The pointer " + ptr + " is not in the symbol table");
        }

        List<Page> ptrPages = symbolTable.get(ptr); // Get the pages assigned to the pointer
        List<Page> pagesToMove = new ArrayList<>(); // List to store the pages that need to be moved to the real memory
        Set<Page> realMemorySet = new HashSet<>(Arrays.asList(realMemory)); // Set to store the pages in the real memory

        // Iterate over the pages assigned to the pointer
        for (Page searchedPage : ptrPages) {
            // Check if the page is in the real memory
            if (realMemorySet.contains(searchedPage)) {
                if (paginationAlgorithm == 2) { // If the pagination algorithm is SC
                    searchedPage.setReferenceBit(true);
                }
            } else {
                pageFaults++; // Increase the page faults
                pagesToMove.add(searchedPage); // Add the page to the list of pages to move to the real memory
            }
        }

        // Check if there are pages to move to the real memory
        if (!pagesToMove.isEmpty()) {
            paginationAlgorithm(pagesToMove.size()); // Free the memory using the chosen pagination algorithm
            storeOldPages(pagesToMove, ptr); // Store the pages in the real memory
            virtualMemory.removeAll(pagesToMove); // Remove the pages from the virtual memory
        }
        // If the pagination algorithm is MRU
        if (paginationAlgorithm == 3)
            mruPageStack.addAll(ptrPages); // Add the pages to the stack of most recently used pages

    }

    /*
     * Delete all the pages in the real memory that belong to the given pointer
     * @param ptr The pointer to the memory assigned to the process
     */
    public static void delete(Integer ptr) {
        // Check if the pointer is in the symbol table
        if (symbolTable.containsKey(ptr)) {
            // Iterate over the real memory to free the memory used by the pointer

            List<Page> pagesToRemove = symbolTable.get(ptr);

            for (Page page : pagesToRemove) {
                if (page.getInRealMemory()) {
                    for (int i = 0; i < realMemory.length; i++) {
                        if (realMemory[i] != null && realMemory[i] == page) {
                            if (paginationAlgorithm == 3)
                                mruPageStack.remove(page); // Remove the page from the stack of most recently used pages
                            realMemory[i] = null;
                            remainingRAM++;
                        }
                    }
                } else {
                    if (paginationAlgorithm == 3)
                        mruPageStack.remove(page); // Remove the page from the stack of most recently used pages
                    virtualMemory.remove(page);
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
                if (!pointersToRemove.contains(realMemory[i].getPhysicalAddress())) {
                    pointersToRemove.add(realMemory[i].getPhysicalAddress());
                }
                if (paginationAlgorithm == 3)
                    mruPageStack.remove(realMemory[i]); // Remove the page from the stack of most recently used pages
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
        -------------------------------------
        PAGINATION ALGORITHMS
        -------------------------------------
    */

    /*
     * FIFO algorithm to free memory (First In First Out)
     * @param remainingPages The number of pages needed to store the process
     */
    public static void fifo(int remainingPages) {

        int iterator = 0;
        // Iterate over the real memory to free the memory needed to store the new pages
        while (remainingRAM < remainingPages) {
            // If the space is occupied, then move the page to the virtual memory to free the space
            if (realMemory[iterator] != null) {
                Page pageToMove = realMemory[iterator];
                pageToMove.setPhysicalAddress(null);
                pageToMove.setInRealMemory(false);
                virtualMemory.add(pageToMove);
                realMemory[iterator] = null; // Free the space in the real memory
                remainingRAM++; // Increase the remaining RAM
            }

            // Move the iterator to the next position. If it reaches the end, then start from the beginning
            iterator++;
            if (iterator == MAX_RAM_KB - 1) {
                iterator = 0;
            }
        }
    }

    /*
     * SC algorithm to free memory (Second Chance)
     * @param remainingPages The number of pages needed to store the process
     */
    public static void sc(int remainingPages) {
        int iterator = 0;

        // Iterate over the real memory to free the memory needed to store the new pages
        while (remainingRAM < remainingPages) {
            // Get the page in the current position
            Page page = realMemory[iterator];
            // If the space is occupied, then check the reference bit
            if (page != null) {
                if (page.getReferenceBit()) {
                    // If the reference bit is true, then set it to false
                    page.setReferenceBit(false);
                } else {
                    // If the reference bit is false, then move the page to the virtual memory to free the space
                    page.setPhysicalAddress(null);
                    page.setInRealMemory(false);
                    virtualMemory.add(page);
                    realMemory[iterator] = null;
                    remainingRAM++;
                }
            }
            // Move the iterator to the next position. If it reaches the end, then start from the beginning
            iterator++;
            if (iterator == MAX_RAM_KB - 1) {
                iterator = 0;
            }
        }
    }

    /*
     * MRU algorithm to free memory (Most Recently Used)
     * @param remainingPages The number of pages needed to store the process
     */
    public static void mru(int remainingPages) {

        // Iterate over the real memory to find the most recently used page
        while (remainingRAM < remainingPages) {
            Page mruPage = mruPageStack.pop();
            for (int i = 0; i < realMemory.length; i++) {
                // If the page is the most recently used, then move it to the virtual memory
                if (realMemory[i] != null && realMemory[i] == mruPage) {
                    mruPage.setPhysicalAddress(null);
                    mruPage.setInRealMemory(false);
                    virtualMemory.add(mruPage);
                    realMemory[i] = null;
                    remainingRAM++;  // Increase the remaining RAM
                    break;
                }
            }
        }
    }

    /*
     * Random algorithm to free memory (Random)
     * @param remainingPages The number of pages needed to store the process
     */
    public static void rnd(int remainingPages) {
        Random random = new Random();

        // Iterate over the real memory to free the memory needed to store the new pages
        while (remainingRAM < remainingPages) {
            // Generate a random index to choose a page to move to the virtual memory
            int randomIndex = random.nextInt(MAX_RAM_KB);
            // Get the page in the random index
            Page page = realMemory[randomIndex];
            // If the space is occupied, then move the page to the virtual memory to free the space
            if (page != null) {
                page.setPhysicalAddress(null);
                page.setInRealMemory(false);
                virtualMemory.add(page);
                realMemory[randomIndex] = null;
                remainingRAM++;
            }
        }
    }

    public static void optimal(int remainingPages) {
        while (remainingRAM < remainingPages) {
            int farthestAccess = -1;
            int pageToReplaceIndex = -1;

            // Iterate over the pages in the real memory
            for (int i = 0; i < realMemory.length; i++) {
                Page page = realMemory[i];
                if (page == null) continue;
                int nextPageAccess = findNextPageAccess(page, instructions);
                // If the page will not be accessed in the future or its next access is farther, update
                if (nextPageAccess == -1 || nextPageAccess > farthestAccess) {
                    farthestAccess = nextPageAccess;
                    pageToReplaceIndex = i;
                }
            }

            // If no pages are found in real memory, break
            // If no pages are found in real memory, break
            if (pageToReplaceIndex == -1) break;
            // Remove the page from real memory and increase remaining RAM
            Page pageToReplace = realMemory[pageToReplaceIndex];
            pageToReplace.setPhysicalAddress(null);
            pageToReplace.setInRealMemory(false);
            virtualMemory.add(pageToReplace);
            realMemory[pageToReplaceIndex] = null;
            remainingRAM++;
        }
    }

    /*
        -------------------------------------
        AUXILIARY METHODS
        -------------------------------------
    */

    /*
     * Store the new pages in the real memory
     * @param pid The process ID
     * @param remainingPages The number of pages needed to store the process
     */
    private static void storeNewPages(Integer pid, int remainingPages) {

        // Create a list to store the references to the new pages on the symbol table
        List<Page> pages = new ArrayList<>();

        // Iterate over the real memory to store the new pages in the empty spaces
        int ramIterator = 0;
        while (remainingPages > 0) {
            if (realMemory[ramIterator] == null) {
                // Create a new page and store it in the real memory
                Page page = new Page(pid);
                page.setInRealMemory(true);
                page.setPhysicalAddress(ptrCounter);
                realMemory[ramIterator] = page;
                pages.add(page); // Add the page to the list of pages to store in the symbol table
                remainingRAM--; // Decrease the remaining RAM
                pageFaults++; // Increase the page faults
                remainingPages--;
                if (paginationAlgorithm == 3) { // If the pagination algorithm is MRU
                    mruPageStack.push(page); // Add the page to the stack of most recently used pages
                }
            }

            // Move the iterator to the next position. If it reaches the end, then start from the beginning
            if (ramIterator == MAX_RAM_KB - 1) {
                ramIterator = 0;
            } else {
                ramIterator++;
            }
        }

        // Store the references to the pages in the symbol table
        symbolTable.put(ptrCounter, pages);
        ptrCounter++;
    }

    /*
     * Store the pages in the real memory
     * @param pages The pages that already exist in the real memory
     * @param ptr The pointer to the memory assigned to the process
     */
    private static void storeOldPages(List<Page> pages, int ptr) {
        int ramIterator = 0;

        // Iterate over the real memory to store the pages in the empty spaces
        while (remainingRAM > 0) {
            for (Page page : pages) {
                if (realMemory[ramIterator] == null) {
                    // Store the page in the real memory
                    page.setInRealMemory(true);
                    page.setPhysicalAddress(ptr);
                    realMemory[ramIterator] = page;
                    remainingRAM--; // Decrease the remaining RAM
                }

                // Move the iterator to the next position. If it reaches the end, then start from the beginning
                if (ramIterator == MAX_RAM_KB - 1) {
                    ramIterator = 0;
                } else {
                    ramIterator++;
                }
            }
        }
    }

    /*
     * Choose a pagination algorithm to free memory. If the algorithm has already been chosen, then execute it.
     * @param remainingPages The number of pages needed to store the process
     */
    public static void paginationAlgorithm(int remainingPages) {
        // Execute the chosen pagination algorithm
        switch (paginationAlgorithm) {
            case 1:
                fifo(remainingPages);
                break;
            case 2:
                sc(remainingPages);
                break;
            case 3:
                mru(remainingPages);
                break;
            case 4:
                rnd(remainingPages);
                break;
            case 5:
                optimal(remainingPages);
        }
    }


    public static void choosePaginationAlgorithm() {
        // If the pagination algorithm has not been chosen, then ask the user to choose one
        if (paginationAlgorithm == 0) {
            Scanner scanner = new Scanner(System.in);

            // Ask the user to choose a pagination algorithm until a valid option is chosen
            do {
                try {
                    System.out.println("Choose a pagination algorithm ( 1 - 4): ");
                    System.out.println("1. FIFO");
                    System.out.println("2. SC");
                    System.out.println("3. MRU");
                    System.out.println("4. RND");
                    paginationAlgorithm = scanner.nextInt();
                    if (paginationAlgorithm < 1 || paginationAlgorithm > 4) {
                        System.err.println("Invalid option, please choose a number between 1 and 4");
                    }
                } catch (InputMismatchException e) {
                    System.err.println("Invalid option, please choose a number between 1 and 4");
                    scanner.next();
                }
            } while (paginationAlgorithm < 1 || paginationAlgorithm > 4);
        }
    }

    /*
     * Find the next access to a page in the instructions list
     * @param page The page to find next access for
     * @param instructions The list of instructions
     * @return The index of next access or -1 if not found
     */
    private static int findNextPageAccess(Page page, List<String> instructions) {
        for (int i = 0; i < instructions.size(); i++) {
            String instruction = instructions.get(i);
            if (instruction.contains("use(" + page.getPhysicalAddress() + ")")) {
                return i;
            }
        }
        return -1;
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

        // If the pagination algorithm has not been chosen, then ask the user to choose one
        if (paginationAlgorithm == 0) {
            choosePaginationAlgorithm();
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
            if (command.equals("new")) {
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
                        use(ptr);
                        break;
                    default:
                        System.out.println("Invalid command: " + command);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void executeOptimal(List<String> userInstructions) {
        // Check if there are instructions to execute
        if (userInstructions == null) {
            System.out.println("No instructions to execute");
            return;
        }

        instructions = userInstructions;
        paginationAlgorithm = 5;
        execute(instructions);

        instructions = null;
        paginationAlgorithm = 0;
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
        System.out.println("\nVirtual memory: ");
        for (Page page : virtualMemory) {
            System.out.println(page);
        }
        System.out.println("=================================");
    }

    /*
     * Print the symbol table
     */
    public static void printSymbolTable() {
        System.out.println("=================================");
        System.out.println("\nSymbol table: ");
        for (Map.Entry<Integer, List<Page>> entry : symbolTable.entrySet()) {
            System.out.print("Ptr: " + entry.getKey() + ", Pages: ");
            for (Page page : entry.getValue()) {
                System.out.print(page + ", ");
            }
            System.out.println();
        }
        System.out.println("=================================");
    }

    public static void clean() {
        remainingRAM = MAX_RAM_KB;
        virtualMemory.clear();
        Arrays.fill(realMemory, null);
        symbolTable.clear();
        mruPageStack.clear();
        fragmentation = 0;
        ptrCounter = 0;
        paginationAlgorithm = 0;
        pageFaults = 0;
        instructions = null;
    }
}