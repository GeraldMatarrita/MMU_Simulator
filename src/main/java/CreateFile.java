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
            return "[" + id + isDeleted +
                    ']';
        }
    }

    private static final int CANTIDAD_INSTRUCCIONES = 10;
    private static final double PORCENTAJE_KILL = 0.05;
    private static final double PORCENTAJE_DELETE = 0.20;
    private static final double PORCENTAJE_USE = 0.30;
    private static final int MIN_SIZE = 1000;
    private static final int MAX_SIZE = 5000;
    private static final Random random = new Random();
    private static final List<IdRecord> idList = new ArrayList<>();
    private static final String FILENAME = "instructions.txt";
    private static int idCounter = 0;

    public static void writeInstructionsToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME))) {
            int killCount = 0;
            int deleteCount = 0;
            int useCount = 0;
            int instructionCount = CANTIDAD_INSTRUCCIONES;

            for (int i = 0; i < instructionCount; i++) {

                double chance = random.nextDouble();
                String instruction;

                if (i >= 2) { // Realizar operaciones kill y delete después de las primeras n iteraciones
                    if (chance < PORCENTAJE_KILL && killCount < instructionCount * PORCENTAJE_KILL) {
                        int idToKill = getRandomId();
                        if (idToKill != -1) {
                            instruction = "kill(" + idToKill + ")";
                            markAsDeleted(idToKill); // Marca el ID seleccionado como eliminado
                            killCount++;
                        } else {
                            instruction = generateNewInstruction();
                        }
                    } else if (chance < PORCENTAJE_KILL + PORCENTAJE_DELETE && deleteCount < instructionCount * PORCENTAJE_DELETE) {
                        int indexToDelete = getRandomIndice();
                        if (indexToDelete != -1) {
                            instruction = "delete(" + indexToDelete + ")";
                            idList.get(indexToDelete).isDeleted = true; // Marca el índice seleccionado como eliminado
                            deleteCount++;
                        } else {
                            instruction = generateNewInstruction();
                        }
                    } else if (chance < PORCENTAJE_KILL + PORCENTAJE_DELETE + PORCENTAJE_USE && useCount < instructionCount * PORCENTAJE_USE) {
                        int idUse = getRandomIndice();
                        if (idUse != -1) {
                            instruction = "use(" + idUse + ")";
                            useCount++;
                        } else {
                            instruction = generateNewInstruction();
                        }
                    } else {
                        instruction = generateNewInstruction();
                    }
                } else {
                    instruction = generateNewInstruction();
                }

//                System.out.println("Instrucción generada: " + instruction);
//                System.out.println("Estado actual de idList: " + idList.toString());
//                System.out.println("\n\n");

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
