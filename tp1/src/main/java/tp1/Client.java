package tp1;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    private static Socket socket;
    private static final int TIMEOUT = 2000;

    public static void main(String[] args) throws Exception {
        try {
            connect();
        } catch (SocketTimeoutException e) {
            System.out.println("Couldn't reach the server");
            return;
        } catch (ConnectException e) {
            System.out.println("Connexion refused");
            return;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        Shell shell;

        try {
            shell = new Shell(socket);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        shell.run();

        socket.close();
    }

    private static void connect() throws Exception, SocketTimeoutException, ConnectException {
        InputParser inputParser = new InputParser();
        String serverAddress;
        int port;

        // serverAddress = inputParser.getIP();
        // port          = inputParser.getPort();

        serverAddress = "127.0.0.1";
        port          = 5000;

        socket = new Socket();
        socket.connect(new InetSocketAddress(serverAddress, port), TIMEOUT);

        System.out.format("The server is running %s:%d%n", serverAddress, port);
    }

    public static class InputParser {
        private static final Pattern IPV4_PATTERN =
            Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
                            + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
                            + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
                            + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        private static final Pattern PORT_PATTERN = Pattern.compile("^(50[0-5][0-9])$");

        private Scanner scanner;

        public InputParser() {
            this.scanner = new Scanner(System.in);
        }

        public String getIP() throws Exception {
            System.out.print("Server address: ");

            String ip       = this.scanner.nextLine();
            String error    = "IP address must be in the format '0-255.0-255.0-255.0-255'";
            Matcher matcher = IPV4_PATTERN.matcher(ip);

            if (!matcher.matches())
                throw new Exception(error);

            return ip;
        }

        public int getPort() throws Exception {
            System.out.print("Server port: ");

            String error    = "The port should be a number between 5000 and 5050";
            String port     = this.scanner.nextLine();
            Matcher matcher = PORT_PATTERN.matcher(port);

            if (!matcher.matches())
                throw new Exception(error);

            return Integer.parseInt(port);
        }
    }
}
