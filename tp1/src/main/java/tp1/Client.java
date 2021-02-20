package tp1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client
{   
    private static String clientIP = "";
    private static int clientPort = 0;
    private static Scanner clientInput = new Scanner(System.in); // Creation du Scanner pour avoir le user input
    private static Socket socket;

    /* Application client*/
    public static void main( String[] args ) throws Exception {

        // Tant que l'addresse et le port ne sont pas valides demander de nouveau
        while(!validInput());

       // Création d'une nouvelle connection avec le serveur
       socket = new Socket(clientIP, clientPort);

       System.out.format("The server is running on %s:%d%n", clientIP, clientPort);
        
       // Création d'un canal entrant pour recevoir les messages 
       DataInputStream in = new DataInputStream(socket.getInputStream());

       // Attente de la reception d'un message envoye par le serveur sur le canal
       String helloMessageFromServer = in.readUTF();
       System.out.println(helloMessageFromServer);

		while(commands());

       // Fermeture de la connexion avec le serveur
       socket.close();
    }

        /*
    * Demander le IP addresse et le port, puis verifier s'il sont valides
    */
    private static Boolean validInput() {   
        // Demander au client sur quel port il veut se connecter
        System.out.println("Please enter the IP address and the port (between 5000 and 5050) to connect to: ");
        String input = clientInput.nextLine();
        // Decouper le input de maniere que A.B.C.D:E -> [A, B, C, D, E]
        String[] inputArray = input.split("[:\\.]", 6);

        return(validAddress(inputArray) && validIP(inputArray) && validPort(inputArray));
    }

    /*
    * Verifier si le input addresse est valide
    */
    private static Boolean validAddress(String[] inputArray) {
        boolean validInput = true;

        if(inputArray.length != 5) {
            validInput = false;
            System.out.println("Please enter a valid input! (eg. 127.0.0.1:5001)");
        }

        return(validInput);

    }

    /*
    * Verifier si le IP addresse est valide
    */
    private static Boolean validIP(String[] inputArray) {
        int ipSize = 4; // 4 octets
        for(int i = 0; i < ipSize; i++) {
            
            int octet = 0;
            
            // Verifier si ce n'est pas des lettres
            try {
                octet = Integer.parseInt(inputArray[i]);
            }
            catch(NumberFormatException e) {
                System.out.println("Please enter a valid IP number! (no letters)");
                return false;
            }

            if(octet < 0 || octet > 255) {
                System.out.println("Please enter an IP address inside bounderies! (XX.XX.XX.XX:YYYY where XX = [0, 255])");
                return false;
            }
        }
        clientIP = inputArray[0] + '.' + inputArray[1] + '.' + inputArray[2] + '.' + inputArray[3];
        return true;
    }

    /*
    * Verifier si le port est valide
    */
    private static Boolean validPort(String[] inputArray) {
        String portInput = inputArray[4];
            
        int port = 0;
        
        // Verifier si ce n'est pas des lettres
        try {
            port = Integer.parseInt(portInput);
        }
        catch(NumberFormatException e) {
            System.out.println("Please enter a valid port number! (no letters)");
            return false;
        }

        // Verifier si respecte [0, 255]
        if(port < 5000 || port > 5050) {
            System.out.println("Please enter a port number inside bounderies! (XX.XX.XX.XX:YYYY where YYYY = [5000, 5050])");
            return false;
        }
        
        clientPort = port;

        return true;
    }

	private static Boolean commands() throws Exception{
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());	
        DataInputStream in = new DataInputStream(socket.getInputStream());	

		String cmd = clientInput.nextLine();
		String[] input = cmd.split(" ", 2);

        if(input[0].equals("exit")) {
			System.out.println("quitting...");
			return false;
		} else if(input[0].equals("upload")) {
            if(fileExist(input[1])) {
                out.writeUTF(cmd);
                upload(out, new File(input[1]));
            } else {
                System.out.println("There was problem with the upload.." );
            }
    	    return true; 
        } else if(input[0].equals("download")) {
            if(fileExist(input[1])) {
                out.writeUTF(cmd);
                String res = in.readUTF();
                if(res.equals("Downloading...")) {
                    download(in, new File(input[1])); 
                } else {
                    System.out.println(res);
                } 
             } else {
                System.out.println("There was problem with the download..");
             }
             return true; 
        } else {
            out.writeUTF(cmd);
		    while(serverAnswer());
		    return true;
        }
	}

	private static Boolean serverAnswer() throws Exception{
		DataInputStream in = new DataInputStream(socket.getInputStream());			
		
		while(true) {
			String serverAnswer = in.readUTF();
            if(serverAnswer.equals("end")) {break;}
			System.out.println(serverAnswer);
		}
		return false;
	}

    private static boolean fileExist(String fileName){
    	File file = new File(fileName);
        return file.isFile();
    }

    private static void upload(DataOutputStream out, File file) throws Exception {
    
        FileInputStream fileInput = new FileInputStream(file.toString());
        byte[] buffer = new byte[4096];
        int read;
        out.writeLong(file.length());
        while ((read = fileInput.read(buffer)) > 0) {
            out.write(buffer, 0, read);
        }
        fileInput.close();
    }
    

    private static void download(DataInputStream in, File file) throws Exception {
        
        FileOutputStream fileOutput = new FileOutputStream(file.getName());
        byte[] buffer = new byte[4096];
        long fileSize = in.readLong();
        int read = 0;

        while(fileSize > 0 && (read = in.read(buffer)) > 0) {
            fileOutput.write(buffer, 0, read);
            fileSize -= read;
        }
        fileOutput.close();
    }
		
}