package ie.atu.sw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

public class Encoder {
    private String input; // Path to input text file.
    private String mappingFile; // Path to mapping CSV.
    private String output; // Path to output file.

    public Encoder(String input, String mappingFile, String output) {
        this.input = input;
        this.mappingFile = mappingFile;
        this.output = output;
    }

    /*
     * Overall Time Complexity of method:
     * 
     * Best case: O(n) where n is the number of words to process
     * Worst case: O(n*L+m+k) where n is word count, m is mapping file size,
     * k is input file size, and L is max word length
     */
    public void encode() throws IOException {
        // Load CSV data into HashMaps.
        HashMap<String, String> listWords = new HashMap<>();
        HashMap<String, String> suffixes = new HashMap<>();

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
                    String key = values[0].trim();
                    String value = values[1].trim();

                    if (key.startsWith("@@")) {
                        suffixes.put(key, value);
                    } else {
                        listWords.put(key, value);
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
                sampleTextBuilder.append(line).append(" ");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading input file: " + e.getMessage(), e);
        }
        String sampleText = sampleTextBuilder.toString().trim();

        // Process each word in the sample text.
        List<String> outputLines = new ArrayList<>();
        // O(k) - split operation is linear in the input length
        String[] words = sampleText.split(" ");
        int totalWords = words.length;

        // Processes each word, O(n*L) where L is the max word length
        for (int i = 0; i < totalWords; i++) {
            String word = words[i];
            String longestPrefix = null;
            // Inner loop: O(L) where L is the word length
            for (int j = word.length(); j > 0; j--) {
                String prefix = word.substring(0, j);
                // HashMap lookups are O(1)
                if (listWords.containsKey(prefix)) {
                    longestPrefix = prefix;
                    break;
                }
            }
            if (longestPrefix != null) {
                String remainder = word.substring(longestPrefix.length());
                if (!remainder.isEmpty()) {
                    String suffixKey = "@@" + remainder;
                    if (suffixes.containsKey(suffixKey)) {
                        // Create a line containing both the prefix and the corresponding suffix.
                        String prefixValue = listWords.get(longestPrefix);
                        String suffixValue = suffixes.get(suffixKey);
                        outputLines.add(prefixValue + "," + suffixValue);
                    } else {
                        // O(1) operations,

                        /* The encoder will take the longest known prefix from the unknown word,
                         *  The remainder is then parsed into UTF-8 Bytes 
                         * Then it is finally converted to hex and a 'x' prefix is added denoting it is a custom word ('x' is not a hex char so it won't be produced in the sequence)
                         * The combination of the UTF-8 Bytes and the hex representation ensures the char sequence will always be unique, this is essential for reprocessing the data as there won't be any collisions.
                          */
                        String prefixValue = listWords.get(longestPrefix);
                        outputLines.add(prefixValue);
                        String hexRemainder = HexFormat.of().formatHex(remainder.getBytes(StandardCharsets.UTF_8))
                                + "x";
                        outputLines.add(hexRemainder);
                    }
                } else {
                    String prefixValue = listWords.get(longestPrefix);
                    outputLines.add(prefixValue);
                }
            } else {
                // Encodes words not in the list into a raw UTF byte sequence as hex. "x" suffix marks custom encoding.

                String hex = HexFormat.of().formatHex(word.getBytes(StandardCharsets.UTF_8)) + "x"; 
                outputLines.add(hex);
            }
            // Update progress meter for each processed word.
            MethodHelper.printProgress(i + 1, totalWords);
        }
        System.out.println(); // Move to a new line after progress meter

        // Write the output lines to the output file.
        try (PrintWriter pw = new PrintWriter(new FileWriter(output))) {
            // O(n) - joining operation is linear in the number of output lines
            String joined = outputLines.stream()
                    .collect(Collectors.joining(","));
            pw.println("[" + joined + "]");
        } catch (IOException e) {
            throw new RuntimeException("Error writing output file: " + e.getMessage(), e);
        }
        System.out.println("Encoding complete. Output written to: " + output);
    }

    public void decode() throws IOException {
        HashMap<String, String> codeToWord = new HashMap<>();
        HashMap<String, String> codeToSuffix = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(mappingFile))) {
            String line;
            /*
             * Complexity Analysis: O(m)
             * Maps are created by iterating through each line of the CSV file.
             * Unlike the encoder, we're creating a reversed mapping (code -> word)
             * to maintain O(1) lookups when decoding, which prevents having to
             * search through every entry in the maps for each code we process.
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
         * File Reading: O(k) for input of size k
         * StringBuilder is used for efficiency vs String concatenation
         * which would create O(k^2) temporary objects in memory
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

        // String Processing: Multiple O(k) operations
        String sampleText = sampleTextBuilder.toString().trim();
        sampleText = sampleText.replaceAll("\\s+", ""); // Removes all whitespace

        // Bracket removal is O(1) since it's just substring operations
        if (sampleText.startsWith("[") && sampleText.endsWith("]")) {
            sampleText = sampleText.substring(1, sampleText.length() - 1);
        }

        // Splitting the comma-separated codes: O(k)
        String[] codes = sampleText.split(",");

        /*
         * Decoding Loop: O(n) where n is number of codes
         * Unlike the encoder's O(n*L) complexity with nested loops,
         * the decoder uses direct HashMap lookups which are O(1),
         * making the overall complexity just O(n)
         */
        StringBuilder decodedText = new StringBuilder();
        for (String code : codes) {
            // All operations below are O(1) per iteration
            if (codeToWord.containsKey(code)) {
                decodedText.append(codeToWord.get(code)).append(" ");
            } else if (codeToSuffix.containsKey(code)) {
                // Suffix handling with O(1) string operations
                int lastSpaceIndex = decodedText.toString().lastIndexOf(" ");
                if (lastSpaceIndex != -1) {
                    decodedText.deleteCharAt(lastSpaceIndex);
                }
                decodedText.append(codeToSuffix.get(code)).append(" ");
            } else if (code.endsWith("x")) {
                // Hex decoding with constant time operations per code
                try {
                    String hexValue = code.substring(0, code.length() - 1);
                    String decodedHex = new String(HexFormat.of().parseHex(hexValue), StandardCharsets.UTF_8);
                    decodedText.append(decodedHex).append(" ");
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid hex string: " + code);
                }
            }
        }

        // Output Writing: O(n) operations
        String decodedOutput = decodedText.toString().trim();

        try (PrintWriter pw = new PrintWriter(new FileWriter(output))) {
            // Final string processing: O(n) for splitting and joining
            String[] outputWords = decodedOutput.split(" ");
            String joined = String.join(" ", outputWords);
            pw.println(joined);
        } catch (IOException e) {
            throw new RuntimeException("Error writing output file: " + e.getMessage(), e);
        }

        System.out.println("Decoding complete. Output written to: " + output);
    }
}