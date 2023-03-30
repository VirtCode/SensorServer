package ch.virt.sensorserver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This abstracts a raw input stream and provides methods for easily accessing data
 */
public class StreamHandler {

    private final InputStream stream;
    private byte currentId;

    /**
     * Creates a stream handler
     * @param stream input stream to abstract
     */
    public StreamHandler(InputStream stream) {
        this.stream = stream;
    }

    /**
     * Blocks to read the next id and stores it
     * @return could successfully read the next id
     * @throws IOException failed to read anything from the stream
     * @see StreamHandler#getId()
     */
    public boolean readId() throws IOException {
        int id = stream.read();
        if (id == -1) return false;

        this.currentId = (byte) id;
        return true;
    }

    /**
     * @return returns the last read id
     * @see StreamHandler#readId()
     */
    public byte getId() {
        return currentId;
    }

    /**
     * Reads a null terminated string from the stream
     * @return read string
     * @throws IOException failed to read from stream
     */
    public String readString() throws IOException {
        List<Byte> string = new ArrayList<>();

        int next;
        while ((next = stream.read()) != 0 && next != -1) {
            string.add((byte) next);
        }

        byte[] bytes = new byte[string.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = string.get(i);
        }

        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /**
     * Reads a buffer from the stream
     * @param size size of the buffer to read
     * @return read buffer
     * @throws IOException failed to read anything from the stream
     */
    public ByteBuffer readBuffer(int size) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.put(stream.readNBytes(buffer.capacity()));
        buffer.position(0);

        return buffer;
    }

    /**
     * Reads a single byte from the stream
     * @return read byte
     * @throws IOException failed to access stream
     */
    public byte readByte() throws IOException {
        return (byte) stream.read();
    }

    /**
     * Reads a boolean from the stream
     * @return read boolean
     * @throws IOException failed to access stream
     */
    public boolean readBoolean() throws IOException {
        return readByte() != 0;
    }
}
