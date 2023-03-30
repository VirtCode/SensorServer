package ch.virt.sensorserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the transmissions of a single device
 */
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
    private final List<String> names = new ArrayList<>();
    private final List<Byte> types = new ArrayList<>();

    private final ResultIndex index;

    /**
     * Creates a result writer
     * @param index index to use
     */
    public ResultWriter(ResultIndex index) {
        this.index = index;
    }

    /**
     * Registers a column for the given device
     * @param name name of the column
     * @param type type of the column specified by the byte indicating the type
     */
    public void addColumn(String name, Byte type) {

        names.add(name);
        types.add(type);

        // Recompute size
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

    /**
     * Starts a transmission
     * @return id of the started transmission
     * @throws IOException failed to write to index or to open file for data storage
     */
    public String startTransmission() throws IOException {
        currentId = index.requestId();

        index.registerTransmission(currentId, device, FORMAT, String.join(" ", names));

        File target = new File(index.getFolder(), String.format(FILENAME, currentId));
        file = new FileWriter(target);
        file.write(String.join(DELIMITER, names) + "\n"); // write header

        transmitting = true;

        return currentId;
    }

    /**
     * Ends the current transmission
     * @return id of the ended transmission
     * @throws IOException failed to close the data file
     */
    public String endTransmission() throws IOException {
        if (!transmitting) return "";

        file.flush();
        file.close();

        transmitting = false;

        return currentId;
    }

    /**
     * @return current device name
     */
    public String getDevice() {
        return device;
    }

    /**
     * @param device change the current device name
     */
    public void setDevice(String device) {
        this.device = device;
    }

    /**
     * Reads a data packet *body* from a given stream
     * @param stream stream to read data packet body from
     * @throws IOException failed to read data from stream
     */
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
