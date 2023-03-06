package org.example;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) throws IOException {
        new Main();
    }

    private ResultIndex storage;

    public Main() throws IOException {
        System.out.println("Starting Server");

        storage = new ResultIndex(new File(""));

        ServerSocket server = new ServerSocket(8003);
        while (!server.isClosed()) {
            Socket socket = server.accept();
            new Thread(() -> {
                try {
                    run(socket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    public static final byte ID_LOGIN = 0x01;
    public static final byte ID_DATA_REGISTER = 0x02;
    public static final byte ID_TRANSMISSION_STATE = 0x03;
    public static final byte ID_DATA = 0x04;

    public void run(Socket socket) throws IOException {
        StreamHandler stream = new StreamHandler(socket.getInputStream());

        ResultWriter result = new ResultWriter(storage);
        System.out.println(result.getDevice() + ": Connected");

        while (stream.readId()) {
            switch (stream.getId()) {

                case ID_LOGIN:

                    result.setDevice(stream.readString());
                    System.out.println(result.getDevice() + ": Logged In successfully");

                    break;

                case ID_DATA_REGISTER:

                    byte type = stream.readByte();
                    String name = stream.readString();
                    //System.out.println(result.getDevice() + ": registered column '" + name + "'");

                    result.addColumn(name, type);

                    break;

                case ID_TRANSMISSION_STATE:

                    if (stream.readBoolean()) {
                        String id = result.startTransmission();
                        System.out.println(result.getDevice() + ": Started transmission " + id);

                    } else {
                        String id = result.endTransmission();
                        System.out.println(result.getDevice() + ": Ended transmission   " + id);
                    }

                    break;

                case ID_DATA:

                    result.readData(stream);

                    break;

            }
        }

        System.out.println(result.getDevice() + ": Disconnected");

    }
}