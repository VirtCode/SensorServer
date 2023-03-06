package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StreamHandler {

    private InputStream stream;
    private byte currentId;

    public StreamHandler(InputStream stream) {
        this.stream = stream;
    }

    public boolean readId() throws IOException {
        int id = stream.read();
        if (id == -1) return false;

        this.currentId = (byte) id;
        return true;
    }

    public byte getId() {
        return currentId;
    }

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

    public ByteBuffer readBuffer(int size) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.put(stream.readNBytes(buffer.capacity()));
        buffer.position(0);

        return buffer;
    }

    public byte readByte() throws IOException {
        return (byte) stream.read();
    }

    public boolean readBoolean() throws IOException {
        return readByte() != 0;
    }

}
