import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	private BufferedReader in;
	private BufferedReader reader;
    private PrintWriter out;
    private Socket socket;
    private String serverAddress;
    private int port;

    public Client() {

    }

    // Verifie la validite de l'addresse IP
 	// Source: https://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java
 	private static boolean validateIp(final String address) {
 		String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
 		return address.matches(PATTERN);
 	}
	
	// Verifie si le port d'ecoute est entre 5000 et 5050
	public boolean validatePort(final int port) {
		return (port >= 5000 && port <= 5050);
	}
	
	// Initialize l'addresse ip
    public String initializeIp() throws IOException{
        System.out.println("Entrez l'addresse IP:");
  	    String serverAddress = reader.readLine();
  	    
  	    // Si l'addresse ip est invalide
        while (!validateIp(serverAddress)){
            System.out.println("Veuillez entrer une autre addresse Ip:");
        	serverAddress = reader.readLine();
        }
        return serverAddress;
    }
    
	// Initialize le port d'ecoute
    public int initializePort() throws NumberFormatException, IOException{
        System.out.println("Entrez le port d'ecoute :");
        int port = Integer.parseInt(reader.readLine());
        
        // Si le port d'ecoute est invalide
        while (!validatePort(port)){
            System.out.println("Port invalide. Port devrait etre entre 5000 et 5050. Veuillez entrer un autre port:");
            port = Integer.parseInt(reader.readLine());
        }
        return port;
    }
    
    // Verifie si la commande entree est valide
    private boolean isCommandValid(String command) {
    	
    	return (command.equals("ls") || command.equals("exit") ||
    			firstInput(command).equals("cd") ||
    			firstInput(command).equals("mkdir") ||
    			firstInput(command).equals("upload") ||
    			firstInput(command).equals("download"));
    }
    
    // Prends la premiere entree de la ligne de commande
    private String firstInput(String command){
    	String firstInput = "";
        if (command.contains(" ")){
        	firstInput = command.substring(0, command.indexOf(" "));
        }
        else {
        	firstInput = command;
        }
        return firstInput;
    }
    
    // Prends la deuxieme entree de la ligne de commande
    private String secondInput(String command){
    	String secondInput = "";
        if (command.contains(" ")){
        	secondInput = command.substring(command.indexOf(" ") + 1, command.length());
        }
        return secondInput;
    }
    
    // Verifie si le fichier existe dans le dossier actuel
    private boolean isFileExist(String fileName){
    	File file = new File(fileName);
    	if (!(file.isFile())){
    		System.out.println("Ce fichier n'existe pas.");
    		return false;
    	}
    	return true;
    }
    
    // Televerse le fichier du repertoire local du client vers le serveur de stockage
    private void uploadFile(Socket sock, File file) throws IOException {
   		
		DataOutputStream dataOutput = new DataOutputStream(sock.getOutputStream());
		FileInputStream fileInput = new FileInputStream(file.toString());
		byte[] buffer = new byte[4096];
		int read;
		dataOutput.writeLong(file.length());
		while ((read=fileInput.read(buffer)) > 0) {
			dataOutput.write(buffer, 0, read);
		}
		fileInput.close();
    }
    
    // Telecharge le fichier du repertoire courant au niveau du serveur de stockage vers le repertoire local du client
    private void downloadFile(Socket sock, String fileName) throws IOException {
    	
		DataInputStream dataInput = new DataInputStream(sock.getInputStream());
		FileOutputStream fileOutput = new FileOutputStream(fileName);
		byte[] buffer = new byte[4096];
		long fileSize = dataInput.readLong();
		int read = 0;
		
		while(fileSize > 0 && (read = dataInput.read(buffer)) > 0) {
			fileOutput.write(buffer, 0, read);
			fileSize -= read;
		}
		fileOutput.close();
    }
    
    // 
    private void processResponse(){
        String response = "";
        try {
        	do {
        		System.out.println(response);
        		response = in.readLine();
        	} while (!response.equals("done"));

        } catch (IOException ex) {
        	response = "Error: " + ex;
        }
    }
    
    // Connexion vers le serveur
    public void connectToServer() throws IOException {
    	reader = new BufferedReader(new InputStreamReader(System.in));
    	serverAddress = initializeIp();
    	port = initializePort();
        socket = new Socket(serverAddress, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }
    
	public void run() throws IOException {
		
		connectToServer();
		
		// Welcome Message from the server
        System.out.println(in.readLine() + "\n");
        
        // Send the current position to the server, the Ipadress and the port
        out.println(new File(".").toPath().toAbsolutePath());
        out.println(serverAddress + " " + port);
        
        // Enter commands while not exiting command line
        String command = "";
        while (!command.equals("exit")) {

	        System.out.println("\nEntrez une commande:");
	        command = reader.readLine();
	        // Check if command is valid
	        while (!isCommandValid(command)){
	            System.out.println("La commande " + command + " n'existe pas. Veuillez entrer une autre commande:");
	        	command = reader.readLine();
	        }
	        
	        // Uploading file
	        if (firstInput(command).equals("upload")){
	        	if (isFileExist(secondInput(command))){
	    	        out.println(command);
	    	        uploadFile(socket, new File(secondInput(command)));
	    	        processResponse();
	        	}
	        }
	        // Downloading file
	        else if (firstInput(command).equals("download")){
    	        out.println(command);
    	        String response = in.readLine();
    	        if(response.equals("Downloading...")){    	        	
    	        	downloadFile(socket, secondInput(command));
    	        } else {    	        	
    	        	System.out.println(response);
    	        }
	        	processResponse();
	        }
	        // Any other command
	        else {
		        out.println(command);
	        	processResponse();
	        }
	        
    	}
        // Exit
        if (command.equals("exit")){
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");
            }
        	System.out.println("Vous avez ete deconnecte avec succes.");
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.run();
    }

}
