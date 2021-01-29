package tp1;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {
    private static Socket socket;

    public static void main(String[] args) throws Exception {
        InputParser inputParser = new InputParser();
        String serverAddress;
        int port;

        try {
            serverAddress = inputParser.getIP();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            port = inputParser.getPort();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        socket = new Socket(serverAddress, port);

        System.out.format("The server is running %s:%d%n", serverAddress, port);

        DataInputStream in = new DataInputStream(socket.getInputStream());

        String helloMessageFromServer = in.readUTF();
        System.out.println(helloMessageFromServer);

        socket.close();
    }

    public static class InputParser {
        private Scanner scanner;

        public InputParser() {
            scanner = new Scanner(System.in);
        }

        private int parseInt(String s, int minValue, int maxValue, String error) throws Exception {
            int val;
            try {
                val = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new Exception(error);
            }

            if (val < minValue || val > maxValue)
                throw new Exception(error);

            return val;
        }

        public String getIP() throws Exception {
            System.out.print("Server address: ");

            String ip = scanner.next();
            String[] bytes = ip.split("\\.", -1);
            String error = "IP address must be in the format '0-255.0-255.0-255.0-255'";

            if (bytes.length != 4)
                throw new Exception(error);

            for (String value : bytes) {
                parseInt(value, 0, 255, error);
            }

            return ip;
        }

        public int getPort() throws Exception {
            System.out.print("Server port: ");

            String error = "The port should be a number between 5000 and 5050";
            int port = parseInt(scanner.next(), 5000, 5050, error);

            return port;
        }
    }
}
