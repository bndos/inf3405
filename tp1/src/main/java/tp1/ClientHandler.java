package tp1;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.nio.file.*;
import java.text.SimpleDateFormat;  
import java.util.Date;  

/*
* Un thread qui se charge de traiter la demande de chaque client 
* sur un socket particulier
*/
public class ClientHandler extends Thread {
    private Socket socket;
    private int clientNumber;
    private Path currentDirectory;

    public ClientHandler(Socket socket, int clientNumber, Path currentDirectory) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        this.currentDirectory = currentDirectory;
        System.out.println("New connection with client#" + clientNumber + " at " + socket);
    }

    // Un thread se charge d'envoyer au client un message de bienvenue
    public void run() {
        try {
            // Creation d'un canal sortant pour envoyer des messages au client
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Envoie d<un message au client 
            out.writeUTF("Hello from server - you are client#" + clientNumber);

            while(commands());

        }
        catch (Exception e) {
            System.out.println("Error handling client#1" + clientNumber + ": " + e);
        }
        finally {
            try {
                // Fermeture de la connexion avec le client
                socket.close();
            }
            catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");
            }
            System.out.println("Connection with client# " + clientNumber + " closed");
        }
    }

    private static String getDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy@HH:mm:ss");  
        Date date = new Date(); 
        return(formatter.format(date)); 
    }

    private boolean commands() throws Exception {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream in = new DataInputStream(socket.getInputStream());
        String cmd = in.readUTF();
		String[] input = cmd.split(" ", 2);	

        String command = input[0];
        System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + " - " + getDateTime() + "]: " + cmd);

        switch (command) {
            case "ls":
                ls(out);
                break;
            case "cd":
                cd(out, input);
                break;
            case "mkdir":
                mkdir(out, input);
                break;
            case "download":
                break;
            case "upload":
                break;
            case "exit":
                return false;
            default:
                out.writeUTF("Something is wrong with the command..");
                break;
        }
        return true;
    }

    private void ls(DataOutputStream out) throws Exception {
        String[] directoryList = currentDirectory.toFile().list();

        // check if there are files
        if(directoryList == null) {
            out.writeUTF("No files found.");
            return;
        }

        // test for types here? (folder or files)
        for(int i = 0; i < directoryList.length ; i++) {
            out.writeUTF(directoryList[i]);
        }
        out.flush();
    }

    private void cd(DataOutputStream out, String[] input) throws Exception {
        String nextDirectory = currentDirectory.toString();
        
        if(input.length != 2) {
            out.writeUTF("No directory or action was listed");
            return;
        }

        String directory = input[1];
        if(directory == ".") {
            return;
        } else if(directory == "..") {
            Path parentDirectory = currentDirectory.getParent();
            if(parentDirectory != null) {
                nextDirectory = parentDirectory.toString();
            }
        } else {
            String[] currentDirectoryList = currentDirectory.toFile().list();

            for(int i=0; i< currentDirectoryList.length; i++) {
                if(currentDirectoryList[i] == nextDirectory) {
                    nextDirectory += "\\" + currentDirectoryList[i];
                } 
            }

            // if no next directory was found
			if(nextDirectory == currentDirectory.toString())
			{
				out.writeUTF("The directory " + directory + " does not exist.");
				return;
			}
			
			// that path is now the current path
			currentDirectory = Paths.get(nextDirectory);
			out.writeUTF("Current Directory : " + nextDirectory.toString());
        } 

    }

    private void mkdir(DataOutputStream out, String[] input) throws Exception{
        String directoryName = input[1];
		Path newFolderPath = Paths.get(currentDirectory.toString(), directoryName);
		if(Files.notExists(newFolderPath)) {
            Files.createDirectory(newFolderPath);
            out.writeUTF(currentDirectory + "was succesfully created");
		}
		else {
            out.writeUTF(currentDirectory + " already exist !");
		}
    }

    private void download() {
        // TODO
    }

    private void upload() {
        // TODO
    }

}