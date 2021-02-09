package tp1;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Shell {
    private Socket socket;
    private Scanner scanner;

    public Shell(Socket s) throws Exception, IOException {
        this.socket  = s;
        this.scanner = new Scanner(System.in);
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
                            exit = !SocketCommunication.exchangeMessages(socket, input);
                        break;
                    case LS:
                        if (validArgs(args, 1, 1))
                            exit = !SocketCommunication.exchangeMessages(socket, input);
                        break;
                    case MKDIR:
                        if (validArgs(args, 2, Integer.MAX_VALUE))
                            exit = !SocketCommunication.exchangeMessages(socket, input);
                        break;
                    case UPLOAD:
                        if (validArgs(args, 2, Integer.MAX_VALUE)) {
                            try {
                                exit = !SocketCommunication.sendMessage(socket, input);
                                SocketCommunication.sendFile(socket, args[1]);
                                System.out.println("File: " + args[1] + " uploaded successfully");
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case DOWNLOAD:
                        if (validArgs(args, 2, Integer.MAX_VALUE)) {
                            exit = !SocketCommunication.sendMessage(socket, input);
                            if (!exit) {
                                try {
                                    SocketCommunication.receiveFile(socket, args[1]);
                                    System.out.println("File: " + args[1]
                                                       + " downloaded successfully");
                                } catch (Exception e) {
                                    System.out.println("Could not download file: " + args[1]);
                                }
                            }
                        }
                        break;
                    case EXIT:
                        if (validArgs(args, 1, 1)) {
                            SocketCommunication.sendMessage(socket, input);
                            exit = true;
                        }
                        break;
                }

            } else {
                System.out.println("Unrecognize command: " + cmd);
            }
        }
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
