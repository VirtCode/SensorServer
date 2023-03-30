package org.example;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static final byte ID_LOGIN = 0x01;
    public static final byte ID_DATA_REGISTER = 0x02;
    public static final byte ID_TRANSMISSION_STATE = 0x03;
    public static final byte ID_DATA = 0x04;

    private final ResultIndex storage;

    private final ServerSocket server;

    public Server(String path, int port) throws IOException {
        System.out.printf("Using output folder at '%s'%n", new File(path).getCanonicalPath());
        storage = new ResultIndex(new File(path));

        System.out.printf("Opening server on port %d%n", port);
        server = new ServerSocket(port);
    }

    public void accept() throws IOException {
        System.out.println("Ready for clients...\n");
        while (!server.isClosed()) {
            Socket socket = server.accept();
            new Thread(() -> {
                try {
                    run(socket);
                } catch (IOException e) {
                    System.err.println("Exception occurred in connection thread: " + e);
                    e.printStackTrace();
                }
            }).start();
        }
    }

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
