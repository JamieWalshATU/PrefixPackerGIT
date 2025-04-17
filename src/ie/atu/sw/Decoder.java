package ie.atu.sw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;

public class Decoder {
    private String input; // Path to input text file.
    private String mappingFile; // Path to mapping CSV.
    private String output; // Path to output file.

    public Decoder(String input, String mappingFile, String output) {
        this.input = input;
        this.mappingFile = mappingFile;
        this.output = output;
    }

    /*
     * Overall Time Complexity of method:
     * 
     * Best case: O(n) where n is the number of words to process
     * Worst case: O(n+m+k) where n is word count, m is mapping file size, k is
     * input file size
     */
    public void decode() throws IOException {
        HashMap<String, String> codeToWord = new HashMap<>();
        HashMap<String, String> codeToSuffix = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(mappingFile))) {
            String line;
            /*
             * Time Complexity here is O(m) as:
             * while ((line = br.readLine()) != null) {...}
             * Word processing like this cannot be done any *
             * faster, as the code needs to process (loop) for *
             * every word in the mapping file. This is the same for other sections *
             * below.
             */
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 2) {
                    String code = values[1].trim();
                    String word = values[0].trim();

                    if (word.startsWith("@@")) {
                        codeToSuffix.put(code, word.substring(2)); // Remove @@ prefix when storing
                    } else {
                        codeToWord.put(code, word);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading mapping file: " + e.getMessage(), e);
        }

        /*
         * Reads input file, also O(k) due to the nature of how
         * processing works here
         * while ((line = br.readLine()) != null) {..}
         */
        StringBuilder sampleTextBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(input))) {
            String line;
            while ((line = br.readLine()) != null) {
                sampleTextBuilder.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading input file: " + e.getMessage(), e);
        }

        // Process input
        String sampleText = sampleTextBuilder.toString().trim();
        // O(k) - scans each char once for replaceAll
        sampleText = sampleText.replaceAll("\\s+", "");
        if (sampleText.startsWith("[") && sampleText.endsWith("]")) {
            sampleText = sampleText.substring(1, sampleText.length() - 1);
        }
        // O(k) - split operation is linear in the input length
        String[] words = sampleText.split(",");
        StringBuilder decodedText = new StringBuilder();
        
        // Display total number of words being processed
        int totalWords = words.length;
        System.out.println("Decoding " + totalWords + " words...");
        
        // Processes each word, O(n)
        for (int i = 0; i < totalWords; i++) {
            String word = words[i];
            if (codeToWord.containsKey(word)) {
                decodedText.append(codeToWord.get(word)).append(" ");
            } else if (codeToSuffix.containsKey(word)) {
                // Handle suffixes
                int lastSpaceIndex = decodedText.toString().lastIndexOf(" "); // Convert to String to use lastIndexOf
                if (lastSpaceIndex != -1) {
                    decodedText.deleteCharAt(lastSpaceIndex);
                }
                decodedText.append(codeToSuffix.get(word)).append(" ");
            } else if (word.endsWith("x")) {
                // Handle hex decoding
                try {
                    String hexValue = word.substring(0, word.length() - 1); // Remove the 'x' at the end
                    String decodedHex = new String(HexFormat.of().parseHex(hexValue), StandardCharsets.UTF_8);
                    decodedText.append(decodedHex).append(" ");
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid hex string: " + word);
                }
            }
            
            // Update progress meter for each processed word
            MethodHelper.printProgress(i + 1, totalWords);
        }
        
        System.out.println(); 

        String decodedOutput = decodedText.toString().trim();

        try (PrintWriter pw = new PrintWriter(new FileWriter(output))) {
            // Writes each word to the file, O(n)
            String[] outputWords = decodedOutput.split(" ");
            String joined = String.join(" ", outputWords);
            pw.println(joined);
        } catch (IOException e) {
            throw new RuntimeException("Error writing output file: " + e.getMessage(), e);
        }
        System.out.println("Decoding complete. Output written to: " + output);
    }
}
