package ch.virt.sensorserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class manages the server socket and accepts and reads clients
 */
public class Server {

    public static final byte ID_LOGIN = 0x01;
    public static final byte ID_DATA_REGISTER = 0x02;
    public static final byte ID_TRANSMISSION_STATE = 0x03;
    public static final byte ID_DATA = 0x04;

    private final ResultIndex storage;

    private final ServerSocket server;

    /**
     * Creates a server
     * @param path path to store data to
     * @param port port to open server on
     * @throws IOException failed to read or write to data path or failed to open port
     */
    public Server(String path, int port) throws IOException {
        System.out.printf("Using output folder at '%s'%n", new File(path).getCanonicalPath());
        storage = new ResultIndex(new File(path));

        System.out.printf("Opening server on port %d%n", port);
        server = new ServerSocket(port);
    }

    /**
     * Start accepting clients, this method will block indefinitely and will fork into different threads
     * @throws IOException failed to accept clients
     */
    public void accept() throws IOException {
        System.out.println("Ready for clients...\n");
        while (!server.isClosed()) {
            Socket socket = server.accept();
            new Thread(() -> {
                try {
                    run(socket);
                } catch (IOException e) {
                    System.err.println("Error: Exception occurred in connection thread: " + e);
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * This method takes care of one single connection, accepting and reading data from it
     * @param socket connection to read from
     * @throws IOException something failed
     */
    private void run(Socket socket) throws IOException {
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
