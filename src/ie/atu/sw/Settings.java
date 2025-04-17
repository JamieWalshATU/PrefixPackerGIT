package ie.atu.sw;

import java.io.*;
import java.util.Properties;

public class Settings {
    private static final String SETTINGS_FILE = "settings.properties";
    private final Properties properties = new Properties();

    public boolean filePersist;
    public boolean autoEncodeDecode;
    public String inFile;
    public String outFile;
    public String mapFile;

    public void load() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                properties.load(input);
                filePersist = Boolean.parseBoolean(properties.getProperty("filePersist", "true"));
                autoEncodeDecode = Boolean.parseBoolean(properties.getProperty("autoEncodeDecode", "false"));
                inFile = properties.getProperty("inFile", "");
                outFile = properties.getProperty("outFile", "");
                mapFile = properties.getProperty("mapFile", "");
            } catch (IOException e) {
                System.err.println("Could not read settings: " + e.getMessage());
            }
        } else {
            filePersist = true;
            autoEncodeDecode = false;
            inFile = "";
            outFile = "";
            mapFile = "";
        }
    }

    public void save() {
        properties.setProperty("filePersist", String.valueOf(filePersist));
        properties.setProperty("autoEncodeDecode", String.valueOf(autoEncodeDecode));
        properties.setProperty("inFile", inFile != null ? inFile : "");
        properties.setProperty("outFile", outFile != null ? outFile : "");
        properties.setProperty("mapFile", mapFile != null ? mapFile : "");

        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(output, "Config");
        } catch (IOException e) {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }

    // Methods to update file paths and save them automatically
    public void setInFile(String inFile) {
        this.inFile = inFile;
        save();
    }

    public void setOutFile(String outFile) {
        this.outFile = outFile;
        save();
    }

    public void setMapFile(String mapFile) {
        this.mapFile = mapFile;
        save();
    }
}
