package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResultIndex {

    public static final String DELIMITER = ",";
    public static final String INDEX = "index.csv";

    private final File folder;
    private final List<String> ids;
    private final FileWriter writer;

    public ResultIndex(File folder) throws IOException {
        this.folder = folder;

        File index = new File(folder, INDEX);
        if (index.exists()) {
            List<String> lines = Files.readAllLines(Path.of(index.toURI()));
            
            ids = new ArrayList<>();
            for (String line : lines) {
                if (line.startsWith("id")) continue; // ignore header

                String[] splits = line.split(DELIMITER);
                if (splits.length == 0) continue;

                ids.add(splits[0]);
            }
            
            writer = new FileWriter(index, true);
        } else {
            writer = new FileWriter(index);
            
            writer.write("id" + DELIMITER + "device" + DELIMITER + "start" + DELIMITER + "format" + DELIMITER + "info\n");
            writer.flush();
            
            ids = new ArrayList<>();
        }
    }

    public File getFolder() {
        return folder;
    }


    public String requestId() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            builder.append(chars.charAt((int) (Math.random() * chars.length())));
        }

        String id = builder.toString();
        if (ids.contains(id)) return requestId();
        
        ids.add(id);
        return id;
    }

    public void registerTransmission(String id, String device, String format, String info) throws IOException {

        this.writer.write(
                id + DELIMITER +
                    device + DELIMITER +
                    new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(new Date()) + DELIMITER +
                    format + DELIMITER +
                    info + "\n");
        this.writer.flush();
    }



}
