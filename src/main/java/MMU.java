import java.util.*;

public class MMU {

    private static List<Page> virtualMemory;
    private static Page[] realMemory;
    private static final Integer MAX_RAM_KB = 6; // Max space for physical memory
    private static final Integer KB = 1000;
    private static int remainingRAM;
    private static Map<Integer, List<Integer>> symbolTable;

    private static Integer fragmentation = 0;

    private static int ptrCounter = 1;

    public MMU() {
        virtualMemory = new ArrayList<>();
        realMemory = new Page[MAX_RAM_KB];
        remainingRAM = MAX_RAM_KB;
        symbolTable = new HashMap<Integer, List<Integer>>();
    }

    public static List<Page> getVirtualMemory() {
        return virtualMemory;
    }

    public static void setVirtualMemory(List<Page> virtualMemory) {
        MMU.virtualMemory = virtualMemory;
    }

    public static Page[] getRealMemory() {
        return realMemory;
    }

    public static void setRealMemory(Page[] realMemory) {
        MMU.realMemory = realMemory;
    }

    public static Integer getRemainingRAM() {
        return remainingRAM;
    }

    public static void setRemainingRAM(Integer remainingRAM) {
        MMU.remainingRAM = remainingRAM;
    }

    public static Map<Integer, List<Integer>> getSymbolTable() {
        return symbolTable;
    }

    public static void setSymbolTable(Map<Integer, List<Integer>> symbolTable) {
        MMU.symbolTable = symbolTable;
    }

    /*
     * Create a new process in the memory
     * @param pid The process ID
     * @param size The size of the process in bytes
     * @return The pointer in the real memory where the process is stored
     */
    public Integer new_(Integer pid, Integer size) {

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

    public void use(Integer ptr) throws NotInRealMemoryException{
        // Check if the pointer is in the symbol table if not throw an exception
        if (!symbolTable.containsKey(ptr)) {
            throw new NotInRealMemoryException("The pointer is not in the real memory");
        }
    }

    /*
     * Delete all the pages in the real memory that belong to the given pointer
     * @param ptr The pointer to the memory assigned to the process
     */
    public void delete(Integer ptr) {
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
    public void kill(Integer pid) {

        List<Integer> ptrsToRemove = new ArrayList<>();
        // Iterate over the real memory to free the memory used by the process
        for (int i = 0; i < realMemory.length; i++) {
            if (realMemory[i] != null && Objects.equals(realMemory[i].getPId(), pid)) {
                // Add the pointer to the list of pointers to remove from the symbol table
                if(!ptrsToRemove.contains(realMemory[i].getPhysicalAddress())){
                    ptrsToRemove.add(realMemory[i].getPhysicalAddress());
                }
                realMemory[i] = null;
                remainingRAM++;
            }
        }

        // Remove the pointers with their pages from the symbol table
        for (Integer ptr : ptrsToRemove) {
            symbolTable.remove(ptr);
        }
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

    public static Integer getFragmentation() {
        return fragmentation;
    }
}
