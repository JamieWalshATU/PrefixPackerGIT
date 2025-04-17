package ie.atu.sw;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
    /* Contains UI Methods and File Handling methods,
     * Keeps Runner class clean and readable
     */
public class MethodHelper {
    
    // Progress bar and visual indicator methods
    public static void printProgress(int index, int total) {
        if (index > total)
            return; // Out of range
        int size = 50; // Must be less than console width
        char done = '█'; // Change to whatever you like.
        char todo = '░'; // Change to whatever you like.
        System.out.print(ConsoleColour.YELLOW);
        int complete = (100 * index) / total;
        int completeLen = size * complete / 100;

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            sb.append((i < completeLen) ? done : todo);
        }

        System.out.print("\r" + sb + "] " + complete + "%");

        if (index == total)
            System.out.println("\n");
        System.out.print(ConsoleColour.WHITE);
    }
    
    public static void dirScanProgress() {
        System.out.println("Scanning Directory...");
        System.out.print(ConsoleColour.YELLOW);
        int size = 100;
        for (int i = 0; i < size; i++) {
            printProgress(i + 1, size);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.print("\n");
        System.out.print(ConsoleColour.WHITE);
    }
    
    // File operation helper methods
    public static HashMap<String, String> getFileNames(String dir) {
        HashMap<String, String> fileMap = new HashMap<>();
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            int dirIndex = 0;
            List<String> fileNames = stream
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> !name.equals("settings.properties")) // Filter out settings.properties
                .collect(Collectors.toList());
                
            for (String fileName : fileNames) {
                dirIndex++;
                fileMap.put(String.valueOf(dirIndex), fileName);
            }
        } catch (IOException e) {
            printError("Error reading directory: " + e.getMessage());
        }
        return fileMap;
    }
    
    public static String selectFile(String dir, String promptMessage, Scanner scanner) {
        HashMap<String, String> fileMap = getFileNames(dir);
        printHeader("\nFiles in directory:");
        fileMap.forEach((key, value) -> {
            System.out.print(ConsoleColour.CYAN);
            System.out.print(key + " - ");
            System.out.print(ConsoleColour.WHITE);
            System.out.println(value);
        });
        printInfo(promptMessage);
        String choice = scanner.nextLine();
        if (fileMap.containsKey(choice)) {
            return fileMap.get(choice);
        } else if (choice.isEmpty() && promptMessage.contains("default")) {
            // Allow empty input when a default is available
            return null;
        } else {
            printError("Invalid selection. Please try again.");
            return null;
        }
    }
    
    public static String handleDefaultFile(String defaultFilePath) {
        File file = new File(defaultFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                printSuccess("Default file created: " + defaultFilePath);
            } catch (IOException e) {
                printError("Error creating default file: " + e.getMessage());
            }
        }
        return defaultFilePath;
    }
    
    // Color-coded output methods
    public static void printSuccess(String message) {
        System.out.print(ConsoleColour.GREEN);
        System.out.println(message);
        System.out.print(ConsoleColour.WHITE);
    }
    
    public static void printWarning(String message) {
        System.out.print(ConsoleColour.YELLOW);
        System.out.println(message);
        System.out.print(ConsoleColour.WHITE);
    }
    
    public static void printError(String message) {
        System.out.print(ConsoleColour.RED);
        System.out.println(message);
        System.out.print(ConsoleColour.WHITE);
    }
    
    public static void printInfo(String message) {
        System.out.print(ConsoleColour.BLUE);
        System.out.println(message);
        System.out.print(ConsoleColour.WHITE);
    }
    
    public static void printHeader(String message) {
        System.out.print(ConsoleColour.PURPLE);
        System.out.println(message);
        System.out.print(ConsoleColour.WHITE);
    }
}