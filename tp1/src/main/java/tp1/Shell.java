package tp1;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Shell {
    private static final String[] VALID_OPS = {"cd", "ls", "mkdir", "upload", "download", "exit"};

    private Socket socket;
    private Scanner scanner;

    public Shell(Socket s) throws Exception, IOException {
        this.socket  = s;
        this.scanner = new Scanner(System.in);
    }

    public void run() throws Exception, IOException {
        if (this.socket == null || !this.socket.isConnected())
            throw new Exception("The socket is invalid");

        DataInputStream in = new DataInputStream(this.socket.getInputStream());

        String helloMessageFromServer = in.readUTF();
        System.out.println(helloMessageFromServer);

        mainLoop();
    }

    private void mainLoop() {
        boolean exit = false;
        while (!exit) {
            String cmd = prompt();
            if (cmd.equals("exit"))
                exit = true;
        }
    }

    private String prompt() {
        LocalDateTime nowDateTime        = LocalDateTime.now();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");
        String formattedDateTime         = nowDateTime.format(dateTimeFormat);

        System.out.print("[" + this.socket.getLocalAddress().getHostAddress() + ":"
                         + this.socket.getLocalPort() + " - " + formattedDateTime + "]");

        this.scanner = new Scanner(System.in);
        String cmd   = this.scanner.nextLine();

        return cmd;
    }
}
