package tp1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Function;

public class Server {
    private static ServerSocket listener;

    public static void main(String[] args) throws IOException {
        int clientNumber     = 0;
        String serverAddress = "localhost";
        int serverPort       = 5000;

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
        File cwd;

        public ClientHandler(Socket socket, int clientNumber) {
            this.socket       = socket;
            this.clientNumber = clientNumber;
            this.cwd          = new File(".");
            System.out.println("New connection with client#" + clientNumber + " at " + socket);
        }

        public void run() {
            boolean exit = false;
            while (!exit) {
                try {
                    DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
                    DataInputStream in   = new DataInputStream(this.socket.getInputStream());
                    String[] args        = in.readUTF().split("\\s+");
                    exit                 = args[0].equals("exit");

                    out.writeUTF(ls.apply(args));
                } catch (IOException e) {
                    System.out.println("Error handling client #" + clientNumber + ": " + e);
                    try {
                        socket.close();
                    } catch (IOException ioe) {
                        System.out.println("Couldn't close a scoket, what's gouing on?");
                    }
                    System.out.println("Connection with client #" + clientNumber + " closed");
                }
            }
            try {
                socket.close();
            } catch (IOException ioe) {
                System.out.println("Couldn't close a scoket, what's gouing on?");
            }
            System.out.println("Connection with client #" + clientNumber + " closed");
        }

        public Function<String[], String> ls = (args) -> {
            String files = "";
            for (String file : this.cwd.list()) {
                files += file + " ";
            }

            return files;
        };

        public Function<String[], String> mkdir = (args) -> {
            for (String dir : args) {
                File absoluteFile = new File(cwd.getAbsoluteFile() + dir);
                if (!absoluteFile.mkdir())
                    return "Couldn't make dir: " + dir;
            }

            return "";
        };
    }
}
