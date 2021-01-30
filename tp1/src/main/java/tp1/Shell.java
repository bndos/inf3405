package tp1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Shell {
    private Socket socket;
    private Scanner scanner;
    private DataOutputStream out;
    private DataInputStream in;

    public Shell(Socket s) throws Exception, IOException {
        this.socket  = s;
        this.scanner = new Scanner(System.in);

        this.out = new DataOutputStream(this.socket.getOutputStream());
        this.in  = new DataInputStream(this.socket.getInputStream());
    }

    public void run() throws Exception, IOException {
        if (this.socket == null || !this.socket.isConnected())
            throw new Exception("The socket is invalid");

        mainLoop();
    }

    private void mainLoop() throws Exception, IOException {
        boolean exit = false;
        while (!exit) {
            String input  = prompt();
            String[] args = input.trim().split("\\s+");
            String cmd    = args[0];

            if (isValid(cmd)) {
                switch (Api.Command.getCmd(cmd)) {
                    case CD:
                        if (validArgs(args, 2, 2))
                            exit = !exchangeMessages(input);
                        break;
                    case LS:
                        if (validArgs(args, 1, 1))
                            exit = !exchangeMessages(input);
                        break;
                    case MKDIR:
                    case UPLOAD:
                    case DOWNLOAD:
                        if (validArgs(args, 2, Integer.MAX_VALUE))
                            exit = !exchangeMessages(input);
                        break;
                    case EXIT:
                        if (validArgs(args, 1, 1)) {
                            exchangeMessages(input);
                            exit = true;
                        }
                        break;
                }

            } else {
                System.out.println("Unrecognize command: " + cmd);
            }
        }
    }

    private boolean exchangeMessages(String message) {
        try {
            this.out.writeUTF(message);
            String response = this.in.readUTF();
            if (!response.isEmpty())
                System.out.println(response);
        } catch (IOException e) {
            System.out.println("Error sending message to server");
            return false;
        }
        return true;
    }

    private static boolean validArgs(String[] args, int minArg, int maxArgs) {
        if (args.length < minArg) {
            missingOp(args[0]);
            return false;
        }

        if (args.length > maxArgs) {
            tooManyArgs(args[0]);
            return false;
        }

        return true;
    }

    private static void tooManyArgs(String op) {
        System.out.println(op + ": Error, too many arguments");
    }

    private static void missingOp(String op) {
        System.out.println(op + ": Error, missing operand");
    }

    private static boolean isValid(String op) {
        return Api.Command.getCmd(op) != null;
    }

    private String prompt() {
        LocalDateTime nowDateTime        = LocalDateTime.now();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");
        String formattedDateTime         = nowDateTime.format(dateTimeFormat);

        System.out.print("[" + this.socket.getLocalAddress().getHostAddress() + ":"
                         + this.socket.getLocalPort() + " - " + formattedDateTime + "]: ");

        this.scanner = new Scanner(System.in);
        String input = this.scanner.nextLine();

        return input;
    }
}
