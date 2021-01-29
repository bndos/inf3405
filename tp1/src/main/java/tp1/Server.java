package tp1;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static ServerSocket listener;

    public static void main(String[] args) throws IOException {
        int clientNumber = 0;

        String serverAddress = "localhost";
        int serverPort = 5000;

        listener = new ServerSocket();
        listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);

        listener.bind(new InetSocketAddress(serverIP, serverPort), serverPort);

        System.out.format("The server is running %s:%d%n", serverAddress, serverPort);

        try {
            while (true) {
                new ClientHandler(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    public static class ClientHandler extends Thread {
        private Socket socket;
        private int clientNumber;

        public ClientHandler(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            System.out.println("New connection with client#" + clientNumber + " at " + socket);
        }

        public void run() {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF("Hello from server - you are client #" + clientNumber);
            } catch (IOException e) {
                System.out.println("Error handling client #" + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Couldn't close a scoket, what's gouing on?");
                }
                System.out.println("Connection with client #" + clientNumber + " closed");
            }
        }
    }
}
