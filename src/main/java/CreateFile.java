import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class CreateFile {

    private static class IdRecord {
        int id;
        boolean isDeleted;

        IdRecord(int id) {
            this.id = id;
            this.isDeleted = false;
        }
        @Override
        public String toString() {
            return "["  + id + isDeleted +
                    ']';
        }
    }

    private static final int MIN_SIZE = 3000;
    private static final int MAX_SIZE = 10000;
    private static final Random random = new Random();
    private static final List<IdRecord> idList = new ArrayList<>();
    private static final String FILENAME = "instructions.txt";
    private static int idCounter = 1;

    public static void writeInstructionsToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME))) {
            int killCount = 0;
            int deleteCount = 0;
            int instructionCount = 10;

            for (int i = 0; i < instructionCount; i++) {

                double chance = random.nextDouble();
                String instruction;

                if (i >= 2) { // Realizar operaciones kill y delete después de las primeras n iteraciones
                    if (chance < 0.15 && killCount < instructionCount * 0.05) {
                        int idToKill = getRandomId();
                        if (idToKill != -1) {
                            instruction = "kill(" + idToKill + ")";
                            markAsDeleted(idToKill); // Marca el ID seleccionado como eliminado
                            killCount++;
                        } else {
                            continue; // Saltar si no hay IDs válidos para kill
                        }
                    } else if (chance < 0.35 && deleteCount < instructionCount * 0.30) {
                        int indexToDelete = getRandomIndice();
                        if (indexToDelete != -1) {
                            indexToDelete = indexToDelete == 0 ? indexToDelete + 1 : indexToDelete;
                            instruction = "delete(" + indexToDelete + ")";
                            idList.get(indexToDelete).isDeleted = true; // Marca el índice seleccionado como eliminado
                            deleteCount++;
                        } else {
                            continue; // Saltar si no hay índices válidos para delete
                        }
                    } else {
                        instruction = generateNewInstruction();
                    }
                } else {
                    instruction = generateNewInstruction();
                }

                System.out.println("Instrucción generada: " + instruction);
                System.out.println("Estado actual de idList: " + idList);
                System.out.println("\n\n");

                writer.write(instruction);
                writer.newLine();
            }
        }
    }

    private static String generateNewInstruction() {
        double chance = random.nextDouble();
        int id;
        if (chance < 0.7) {
            id = idCounter++; // Incrementar el contador de IDs si es menor que 0.7
        } else {
            id = Math.abs(getRandomReuseId()); // Reutilizar un ID existente si la probabilidad lo indica
        }
        int size = MIN_SIZE + random.nextInt(MAX_SIZE - MIN_SIZE + 1);
        idList.add(new IdRecord(id)); // Agregar nuevo IdRecord
        return "new(" + id + "," + size + ")";
    }

    private static int getRandomId() {
        List<Integer> validIds = idList.stream()
                .filter(idRecord -> !idRecord.isDeleted)
                .map(idRecord -> idRecord.id)
                .toList();
        return validIds.isEmpty() ? -1 : validIds.get(random.nextInt(validIds.size()));
    }

    private static void markAsDeleted(int id) {
        idList.stream()
                .filter(idRecord -> idRecord.id == id)
                .findAny()
                .ifPresent(idRecord -> idRecord.isDeleted = true);
    }

    private static int getRandomIndice() {
        List<Integer> validIndices = IntStream.range(0, idList.size())
                .filter(i -> !idList.get(i).isDeleted)
                .boxed()
                .toList();
        return validIndices.isEmpty() ? -1 : validIndices.get(random.nextInt(validIndices.size()));
    }

    private static int getRandomReuseId() {
        List<Integer> validIds = idList.stream()
                .filter(idRecord -> !idRecord.isDeleted)
                .map(idRecord -> idRecord.id)
                .toList();
        return validIds.isEmpty() ? -1 : validIds.get(random.nextInt(validIds.size()));
    }

}
