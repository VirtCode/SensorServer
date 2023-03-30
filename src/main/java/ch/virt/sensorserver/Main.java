package ch.virt.sensorserver;

import java.io.IOException;

/**
 * This class mainly handles cli input and launches the server
 */
public class Main {

    private static final String VERSION = "v0.2";

    private static final String OUTPUT = ".";
    private static final int PORT = 55555;

    private static final String HELP_TEXT =
            """
                    SensorServer data receiver

                    Usage: java -jar SensorServer.jar [OPTIONS]
                                
                    Options:
                    -v, --version           Print version info and exit
                    -o, --output <PATH>     Set output directory, default %s
                    -p, --port <PORT>       Set port number, default %d
                                
                    -h, --help              Display help information and exit
                    """.formatted(OUTPUT, PORT);

    private static final String VERSION_TEXT = "SensorServer %s".formatted(VERSION);

    /**
     * This is the main method
     * ...yes indeed
     */
    public static void main(String[] args) {

        String output = OUTPUT;
        int port = PORT;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--output", "-o" -> {
                    if (args.length == i + 1) {
                        System.out.println("Error: --output requires an argument");
                        return;
                    }
                    output = args[++i];
                }
                case "--port", "-p" -> {
                    if (args.length == i + 1) {
                        System.out.println("Error: --port requires an argument");
                        return;
                    }

                    String portString = args[++i];
                    try {
                        port = Integer.parseInt(portString);
                        if (port < 0 || port > 65535) throw new RuntimeException();
                    } catch (Exception e) {
                        System.out.println("Error: --port requires a valid integer between 0 and 65535");
                        return;
                    }
                }
                case "--help", "-h" -> {
                    System.out.println(HELP_TEXT);
                    return;
                }
                case "--version", "-v" -> {
                    System.out.println(VERSION_TEXT);
                    return;
                }
                default -> {
                    System.out.printf("Error: Unrecognized option '%s'%n", args[i]);
                    return;
                }
            }
        }

        System.out.printf("Starting SensorServer %s%n", VERSION);

        try {
            Server server = new Server(output, port);
            server.accept();
        } catch (IOException e) {
            System.out.println("Error: Server crashed because of '" + e + "'");
            e.printStackTrace();
        }
    }
}