package tp1;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server {
    private static ServerSocket listener;

    public static void main(String[] args) throws IOException {
        int clientNumber = 0;

        try {
            bind();
        } catch (BindException e) {
            System.out.println("L'addresse n'a pas pu etre assigne");
            return;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            while (true) {
                new ClientHandler(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    private static void bind() throws Exception, BindException {
        String serverAddress;
        int serverPort;

        serverAddress = IpParser.getIP();
        serverPort    = IpParser.getPort();

        listener = new ServerSocket();
        listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);

        listener.bind(new InetSocketAddress(serverIP, serverPort), serverPort);

        System.out.format("The server is running %s:%d%n", serverAddress, serverPort);
    }

    public static class ClientHandler extends Thread {
        private Socket socket;
        private int clientNumber;

        public ClientHandler(Socket socket, int clientNumber) {
            this.socket       = socket;
            this.clientNumber = clientNumber;
            System.out.println("New connection with client#" + clientNumber + " at " + socket);
        }

        private void prompt(String cmd) {
            LocalDateTime nowDateTime        = LocalDateTime.now();
            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");
            String formattedDateTime         = nowDateTime.format(dateTimeFormat);

            System.out.println("[" + this.socket.getLocalAddress().getHostAddress() + ":"
                               + this.socket.getPort() + " - " + formattedDateTime + "]: " + cmd);
        }

        public void run() {
            boolean exit = false;
            Api api      = new Api(socket);
            while (!exit) {
                try {
                    String cmd    = SocketCommunication.getMessage(socket);
                    String[] args = cmd.split("\\s+");
                    exit          = args[0].equals("exit");

                    prompt(cmd);

                    String message = api.exec(args);
                    if (!args[0].equals("upload") && !args[0].equals("download")) {
                        SocketCommunication.sendMessage(socket, message);
                    }
                } catch (IOException e) {
                    exit = true;
                    System.out.println("Error handling client #" + clientNumber + ": " + e);
                }
            }
            try {
                socket.close();
            } catch (IOException ioe) {
                System.out.println("Couldn't close a scoket, what's gouing on?");
            }
            System.out.println("Connection with client #" + clientNumber + " closed");
        }
    }
}
