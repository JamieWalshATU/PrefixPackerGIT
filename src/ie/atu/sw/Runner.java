package ie.atu.sw;

import java.util.*;
import java.io.*;

public class Runner {

	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
		boolean exit = false;

		Settings settings = new Settings();
		settings.load();

		String mapFile = "";
		String inputFile = "";
		String outputFile = "";
		if (settings.filePersist) {
			mapFile = settings.mapFile;
			inputFile = settings.inFile;
			outputFile = settings.outFile;
		}

		while (!exit) {
			// Print header / menu
			System.out.println(ConsoleColour.WHITE);
			System.out.println("************************************************************");
			System.out.println("*     ATU - Dept. of Computer Science & Applied Physics    *");
			System.out.println("*                                                          *");
			System.out.println("*              Encoding Words with Suffixes                *");
			System.out.println("*                                                          *");
			System.out.println("************************************************************");
			System.out.println("(1) Specify Mapping File");
			System.out.println("(2) Specify Text File to Encode/Decode");
			System.out.println("(3) Specify Output File (default: ./out.txt)");
			System.out.println("(4) Configure Settings");
			System.out.println("(5) Encode Text File");
			System.out.println("(6) Decode Text File");
			System.out.println("(7) Quit");

			// Prompt for selection
			System.out.print(ConsoleColour.BLACK_BOLD_BRIGHT);
			System.out.print("Select Option [1-7]: ");
			System.out.println();
			System.out.print(ConsoleColour.WHITE);

			String choice = scanner.nextLine();

			switch (choice) {
				case "1":
					MethodHelper.printHeader("Option 1 selected: Specify Mapping File");
					MethodHelper.printInfo("Please note that the mapping file must be in the current directory");
					MethodHelper.dirScanProgress();
					String selectedMappingFile = MethodHelper.selectFile("./",
							"Please select a file by entering the corresponding number:", scanner);
					if (selectedMappingFile != null) {
						mapFile = selectedMappingFile;
						settings.setMapFile(mapFile);
						MethodHelper.printSuccess("Full Mapping file path: " + mapFile);
					}
					break;
				case "2":
					MethodHelper.printHeader("Option 2 selected: Specify Input File");
					MethodHelper.printInfo("Please note that the Input file must be in the current directory");
					MethodHelper.dirScanProgress();
					String selectedInputFile = MethodHelper.selectFile("./",
							"Please select a file by entering the corresponding number:", scanner);
					if (selectedInputFile != null) {
						inputFile = selectedInputFile;
						settings.setInFile(inputFile);
						MethodHelper.printSuccess("Full Input file path: " + inputFile);
					}
					break;
				case "3":
					MethodHelper.printHeader("Option 3 selected: Specify Output File (default: ./out.txt)");
					MethodHelper.printInfo(
							"If you wish to override the default, please select a file from the current directory:");
					MethodHelper.dirScanProgress();
					String selectedOutputFile = MethodHelper.selectFile("./",
							"Please select a file by entering the corresponding number or press Enter to use the default (./out.txt):",
							scanner);
					if (selectedOutputFile == null || selectedOutputFile.isEmpty()) {
						outputFile = MethodHelper.handleDefaultFile("./out.txt");
						settings.setOutFile(outputFile);
						MethodHelper.printInfo("Using default output file: " + outputFile);
					} else {
						outputFile = selectedOutputFile;
						File file = new File(outputFile);
						if (!file.exists()) {
							try {
								file.createNewFile();
								MethodHelper.printSuccess("Selected output file created: " + outputFile);
							} catch (IOException e) {
								MethodHelper.printError("Error creating selected output file: " + e.getMessage());
							}
						}
						settings.setOutFile(outputFile);
						MethodHelper.printSuccess("Full Output file path: " + outputFile);
					}
					break;
				case "4":
					MethodHelper.printHeader("Option 4 selected: Configure Settings");
					System.out.println("Current settings:");
					//https://stackoverflow.com/questions/30310147/how-to-print-an-string-variable-as-italicized-text
					System.out.println("\033[3mFile path persistence will save the last known directories of the selected files in settings.properties\033[0m");
					System.out.print("1. File path persistence: ");
					if (settings.filePersist) {
						MethodHelper.printSuccess("Enabled");
					} else {
						MethodHelper.printWarning("Disabled");
					}

					System.out.println("\033[3mAuto re-processing will automatically decode the output file after encoding, or vice versa.\033[0m");
					System.out.print("2. Auto re-processing: ");
					if (settings.autoEncodeDecode) {
						MethodHelper.printSuccess("Enabled");
					} else {
						MethodHelper.printWarning("Disabled");
					}

					MethodHelper.printInfo("\nToggle settings [1-2] or press Enter to go back:");
					String settingChoice = scanner.nextLine();

					switch (settingChoice) {
						case "1":
							settings.filePersist = !settings.filePersist;
							settings.save();
							System.out.print("File path persistence: ");
							if (settings.filePersist) {
								MethodHelper.printSuccess("Enabled");
							} else {
								MethodHelper.printWarning("Disabled");
							}
							break;
						case "2":
							settings.autoEncodeDecode = !settings.autoEncodeDecode;
							settings.save();
							System.out.print("Auto re-processing: ");
							if (settings.autoEncodeDecode) {
								MethodHelper.printSuccess("Enabled");
							} else {
								MethodHelper.printWarning("Disabled");
							}
							break;
						default:
							MethodHelper.printInfo("No changes made to settings.");
					}
					break;
				case "5":
					MethodHelper.printHeader("Option 5 selected: Encode Text File");
					if (inputFile.isEmpty() || mapFile.isEmpty() || outputFile.isEmpty()) {
						MethodHelper.printWarning(
								"Please specify Mapping, Input, and Output files via Options 1-3 before encoding.");
					} else {
						Encoder enc = new Encoder(inputFile, mapFile, outputFile);
						enc.encode();

						// Auto re-processing if enabled
						if (settings.autoEncodeDecode) {
							MethodHelper.printInfo("Auto re-processing enabled: Decoding the output file...");
							String autoDecFile = "autoDEC.txt";
							Decoder autoDec = new Decoder(outputFile, mapFile, autoDecFile);
							autoDec.decode();
							MethodHelper.printSuccess("Re-processed file created: " + autoDecFile);
						}
					}
					break;
				case "6":
					MethodHelper.printHeader("Option 6 selected: Decode Text File");
					if (inputFile.isEmpty() || mapFile.isEmpty() || outputFile.isEmpty()) {
						MethodHelper.printWarning(
								"Please specify Mapping, Input, and Output files via Options 1-3 before decoding.");
					} else {
						Decoder dec = new Decoder(inputFile, mapFile, outputFile);
						dec.decode();

						// Auto re-processing if enabled
						if (settings.autoEncodeDecode) {
							MethodHelper.printInfo("Auto re-processing enabled: Re-encoding the output file...");
							String autoEncFile = "autoENC.txt";
							Encoder autoEnc = new Encoder(outputFile, mapFile, autoEncFile);
							autoEnc.encode();
							MethodHelper.printSuccess("Re-processed file created: " + autoEncFile);
						}
					}
					break;
				case "7":
					MethodHelper.printInfo("Quitting...");
					exit = true;
					break;
				default:
					MethodHelper.printError("Invalid selection. Please try again.");
			}

			if (!exit) {
				System.out.println("\nPress Enter to return to the menu...");
				scanner.nextLine(); // Wait for user input before re-displaying menu.
			}
		}

		scanner.close();
	}
}