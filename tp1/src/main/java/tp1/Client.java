package tp1;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
        String serverAddress;
        int port;

        serverAddress = IpParser.getIP();
        port          = IpParser.getPort();

        socket = new Socket();
        socket.connect(new InetSocketAddress(serverAddress, port), TIMEOUT);

        System.out.format("The server is running %s:%d%n", serverAddress, port);
    }

}
