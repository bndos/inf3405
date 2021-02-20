package tp1;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
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
                try {
                    if(fileExist(out, input[1])) {
                        download(out, input[1]);
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
                break;
            case "upload":
                try{
                     upload(in, out, input[1]);
                } catch (IOException e) {
                   e.printStackTrace();
                }
                break;
            case "exit":
                return false;
            default:
                out.writeUTF("Something is wrong with the command..");
                out.writeUTF("end");
                break;
        }
        return true;
    }

    private void ls(DataOutputStream out) throws Exception {
        String[] directoryList = currentDirectory.toFile().list();

        // check if there are files
        if(directoryList == null) {
            out.writeUTF("No files found.");
            out.writeUTF("end");
            return;
        }

        for(int i = 0; i < directoryList.length ; i++) {
            Path currentPath = Paths.get(currentDirectory.toString() + "\\" + directoryList[i]);
            String directoryType = (Files.isRegularFile(currentPath))? "[File] " : "[Folder] ";
            out.writeUTF(directoryType + directoryList[i]);
        }
        out.writeUTF("end");
    }

    private void cd(DataOutputStream out, String[] input) throws Exception {
        String nextDirectory = currentDirectory.toString();
        
        if(input.length != 2) {
            out.writeUTF("No directory or action was listed");
            out.writeUTF("end");
            return;
        }

        String directory = input[1];
        if(directory.equals(".")) {
            out.writeUTF("end");
            return;
        } else if(directory.equals("..")) {

            Path parentDirectory = currentDirectory.getParent();
            if(parentDirectory != null) {
                nextDirectory = currentDirectory.getParent().toString();
            }

        } else {
            String[] currentDirectoryList = currentDirectory.toFile().list();

            for(int i=0; i< currentDirectoryList.length; i++) {
                if(currentDirectoryList[i].equals(input[1])) {
                    nextDirectory += "\\" + currentDirectoryList[i];
                    
                } 
            }

            // if no next directory was found
			if(nextDirectory.equals(currentDirectory.toString()))
			{
				out.writeUTF("The directory " + directory + " does not exist.");
                out.writeUTF("end");
				return;
			}
        } 
        // that path is now the current path
        currentDirectory = Paths.get(nextDirectory);
        out.writeUTF("Current Directory : " + nextDirectory.toString());
        out.writeUTF("end");
    }

    private void mkdir(DataOutputStream out, String[] input) throws Exception{
        String directoryName = input[1];
		Path newFolderPath = Paths.get(currentDirectory.toString(), directoryName);
		if(Files.notExists(newFolderPath)) {
            Files.createDirectory(newFolderPath);
            out.writeUTF(directoryName + " was succesfully created");
            out.writeUTF("end");
		}
		else {
            out.writeUTF(currentDirectory + " already exist !");
            out.writeUTF("end");
		}
    }

    private void download(DataOutputStream out, String name) throws IOException {
        Path directory = Paths.get(currentDirectory.toString() + "\\" + name);
        File file = new File(directory.toString());
        FileInputStream fileInput = new FileInputStream(directory.toString());
        
        if(file.exists() && file.isFile()) {
            byte[] buffer = new byte[4096];
    		int read;
    		out.writeLong(file.length());
    		while ((read = fileInput.read(buffer)) > 0) {
    			out.write(buffer, 0, read);
    		}
    		fileInput.close();
    		out.writeUTF("The file " + name + " was successfully downloaded");
            out.writeUTF("end");

        } else {
            out.writeUTF("Error: unable to download " + name);
            out.writeUTF("end");
        }
    }

    private void upload(DataInputStream in, DataOutputStream out, String name) throws IOException{
        FileOutputStream fileOutput = new FileOutputStream(name);
        byte[] buffer = new byte[4096];
        long fileSize = in.readLong();
        int read = 0;
        while(fileSize > 0 && (read = in.read(buffer)) > 0) {
            fileOutput.write(buffer, 0, read);
            fileSize -= read;
        }
        fileOutput.close();

        out.writeUTF("Sucessfully uploaded " + name);
        out.writeUTF("end");
    }

    private boolean fileExist(DataOutputStream out, String name) throws Exception {
        File file = currentDirectory.resolve(name).toFile();
        if (!(file.isFile())){
            out.writeUTF("This file does not exist.");
            return false;
        } else {
            out.writeUTF("Downloading...");
            return true;
        }
    }

}