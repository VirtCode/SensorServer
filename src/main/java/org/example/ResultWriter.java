package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ResultWriter {

    public static final String DELIMITER = ",";
    public static final String FORMAT = "dynamic";
    public static final String FILENAME = "data-%s.csv";

    public static final byte TYPE_BOOL = 0x01;
    public static final byte TYPE_I32 = 0x02;
    public static final byte TYPE_I64 = 0x03;
    public static final byte TYPE_F32 = 0x04;
    public static final byte TYPE_F64 = 0x05;

    private String device = "Unknown";

    private String currentId;
    private FileWriter file;
    boolean transmitting = false;

    private int size;
    private List<String> names = new ArrayList<>();
    private List<Byte> types = new ArrayList<>();

    private final ResultIndex index;

    public ResultWriter(ResultIndex index) {
        this.index = index;
    }

    public void addColumn(String name, Byte type) {

        names.add(name);
        types.add(type);

        int size = 0;

        for (Byte b : types) {
            switch (b) {
                case TYPE_BOOL:
                    size += 1;
                    break;
                case TYPE_I32:
                case TYPE_F32:
                    size += 4;
                    break;
                case TYPE_I64:
                case TYPE_F64:
                    size += 8;
                    break;
            }
        }

        this.size = size;
    }

    public String startTransmission() throws IOException {
        currentId = index.requestId();

        index.registerTransmission(currentId, device, FORMAT, String.join(" ", names));

        File target = new File(index.getFolder(), String.format(FILENAME, currentId));
        file = new FileWriter(target);
        file.write(String.join(DELIMITER, names) + "\n"); // write header

        transmitting = true;

        return currentId;
    }

    public String endTransmission() throws IOException {
        if (!transmitting) return "";

        file.flush();
        file.close();

        transmitting = false;

        return currentId;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void readData(StreamHandler stream) throws IOException {
        if (!transmitting) return;

        ByteBuffer buffer = stream.readBuffer(size);
        StringBuilder string = new StringBuilder();

        for (Byte type : types) {
            if (string.length() != 0) string.append(DELIMITER);

            switch (type) {

                case TYPE_BOOL:
                    boolean bool = buffer.get() != 0;
                    string.append(bool);
                    break;
                case TYPE_I32:
                    int i32 = buffer.getInt();
                    string.append(i32);
                    break;

                case TYPE_I64:
                    long i64 = buffer.getLong();
                    string.append(i64);
                    break;

                case TYPE_F32:
                    float f32 = buffer.getFloat();
                    string.append(f32);
                    break;

                case TYPE_F64:
                    double f64 = buffer.getDouble();
                    string.append(f64);
                    break;
            }
        }

        string.append('\n');

        file.write(string.toString());
        file.flush();
    }
}
