import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



public class Serveur {

	private static ServerSocket listener;
	private static BufferedReader reader;
	
	// Verifie la validite de l'addresse IP
	// Source: https://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java
	private static boolean validateIpAddress(final String address) {
		String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
		return address.matches(PATTERN);
	}
	
	// Verifie la validite du port d'ecoute
	private static boolean validatePort(final int port) {
		return (port >= 5000 && port <= 5050);
	}
	
	public static void main(String[] args) throws Exception {
		
		int clientNumber = 0;
		reader = new BufferedReader(new InputStreamReader(System.in));
		
		// L'entree de l'addresse IP du serveur
		System.out.println("Entrez l'addresse IP du serveur:");
		String serverAddress = reader.readLine();
		
		// Si l'addresse rentree est invalide
		while (!Serveur.validateIpAddress(serverAddress)) {
			System.out.println("Addresse invalide. Veuillez entrer une autre:");
			serverAddress = reader.readLine();
		}
		
		// L'entree du port d'ecoute
		System.out.println("Entrez le port d'ecoute:");
		int serverPort = Integer.parseInt(reader.readLine());
		
		// Si le port d'ecoute est invalide
		while (!Serveur.validatePort(serverPort)) {
			System.out.println("Port d'ecoute invalide: Veuillez entrer un port entre 5000 et 5050:");
			serverPort = Integer.parseInt(reader.readLine());
		}
		
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		listener.bind(new InetSocketAddress(serverIP, serverPort));
		
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
		
		try {
			while (true)
			{
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		}
		finally 
		{
			listener.close();
		}
	}
	
	private static class ClientHandler extends Thread {
		
		private Socket socket;
		private int clientNumber;
		private Path actualPath;
        private String IpAddressClient;
        private String portClient;
        private PrintWriter out;      
        private BufferedReader in;
		
		public ClientHandler(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
 			System.out.println("New connection with client#" + clientNumber + " at " + socket);
		}
		
		// Prends la premiere entree de la ligne de commande
        private String firstInput(String command){
        	String input = "";
            if (command.contains(" ")){
            	input = command.substring(0, command.indexOf(" "));
            }
            else {
            	input = command;
            }
            return input;
        }
        
        // Prends la 2e entree de la ligne de commande
        private String secondInput(String command){
        	String input = "";
            if (command.contains(" ")){
            	input = command.substring(command.indexOf(" ") + 1, command.length());
            }
            return input;
        }
        
        // 
        private void command(String command) {
        	switch(firstInput(command)) 
        	{
        	case "cd" :
        		processCd(secondInput(command));
        		break;
        	
        	case "ls" :
        		processLs();
        		break;
        		
        	case "mkdir" :
        		processMkdir(secondInput(command));
        		break;
        		
        	case "upload" :
        		try {
        			saveFile(secondInput(command));
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        		break;
        	
        	case "download" :
        		try {
    				if (isFileExist(secondInput(command))){    					
    					sendFile(secondInput(command));
    				}
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
			    break;
        	case "exit":
        		break;
        	   
    	    default:
    	    	break;
        	
        	}
        }
        
        // Se deplacer vers un repertoire enfant ou parent
        private void processCd(String directory) {
        	
        	// Le repertoire desire
        	Path desiredPath = actualPath.subpath(0, actualPath.getNameCount()-1);
        	
        	// Dans le cas qu'il y a plus que 1 "/" dans le repertoire
        	Path path = Paths.get(directory);
        	for (int i = 0; i < path.getNameCount(); i++) {
        		
        		String subpath = path.subpath(i, i+1).toString();
        		// Vers le repertoire parent
        		if (subpath.equals("..")) {
        			desiredPath = desiredPath.subpath(0, desiredPath.getNameCount()-1);
        	
        		} else {
        			desiredPath = desiredPath.resolve(subpath);
        		}
        	}
        	
        	desiredPath = actualPath.getRoot().resolve(desiredPath);
        	desiredPath = desiredPath.resolve(".");
        	
        	if (desiredPath.toFile().isDirectory()) {
        		actualPath = desiredPath;
        		out.println("Vous etes dans le dossier " + actualPath.subpath(actualPath.getNameCount()-2, actualPath.getNameCount()-1).toString());
        	} else {
        		out.println("Le dossier " + actualPath.subpath(desiredPath.getNameCount()-2, desiredPath.getNameCount()-1).toString() + " n'existe pas");
        	}
        	
        }
        
        // Liste de tous les fichiers et dossiers du repertoire actuel
        private void processLs() {
        	File[] files = new File(actualPath.toString()).listFiles();
        	for (File file : files) {
        		if (file.isFile()) {
        			out.println("[File] " + file.getName());
        		} else if (file.isDirectory()) {
        			out.println("[Folder] " + file.getName());
        		}
        	}
        	
        	if (files.length == 0) {
        		out.println("Aucun fichier dans le repertoire");
        	}
        }
        
        // Creation d'un dossier repertoire
        private void processMkdir(String folder) {
        	
		    if (new File(actualPath.resolve(folder).toString()).mkdirs()) {
		  	  	out.println("Le dossier " + folder + " a bien ete cree");
		    } else {
		    	out.println("Le dossier " + folder + " n'a pas ete cree");
		    }
        }
        
        // Sauvegarde un fichier vers le serveur
    	private void saveFile(String fileName) throws IOException {
    		DataInputStream dis = new DataInputStream(socket.getInputStream());
    		FileOutputStream fos = new FileOutputStream(fileName);
    		byte[] buffer = new byte[4096];
    		long fileSize = dis.readLong();
    		int read = 0;
    		while(fileSize > 0 && (read = dis.read(buffer)) > 0) {
    			fos.write(buffer, 0, read);
    			fileSize -= read;
    		}
    		fos.close();
    		out.println("Le fichier " + fileName + " a bien ete televerse");
    	}
    	
    	// Verifie si le fichier existe
        private boolean isFileExist(String fileName){
        	
        	File file = actualPath.resolve(fileName).toFile();
        	if (!(file.isFile())){
        		out.println("Ce fichier n'existe pas.");
        		return false;
        	} else {
        		out.println("Downloading...");
        		return true;
        	}
        }
    	
        // Telecharge le fichier vers le repertoire local
    	private void sendFile(String fileName) throws IOException {
    		
    		File file = actualPath.resolve(fileName).toFile();
    		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
    		FileInputStream fis = new FileInputStream(file.toString());
    		byte[] buffer = new byte[4096];
    		int read;
    		dos.writeLong(file.length());
    		while ((read=fis.read(buffer)) > 0) {
    			dos.write(buffer, 0, read);
    		}
    		fis.close();
    		out.println("Le fichier " + fileName + " a bien ete telecharge");
    	}
		
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				
				out.println("Hello from server - you are client#" + clientNumber + ".");
				
				String input = in.readLine();
				actualPath = Paths.get(input);
				
				input = in.readLine();
				IpAddressClient = firstInput(input);
				portClient = secondInput(input);
				
				while (true) {
					input = in.readLine();
					if (input == null) {
						break;
					}
					commandLog(input);
					command(input);
					out.println("done");
				}
			} catch (IOException e) {
				System.out.println("Error handling client# " + clientNumber + ": " + e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Couldn't close a socket, what's going on?");
				}
				System.out.println("Connection with client # " + clientNumber + " closed");
			}
		}
		
		private void commandLog(String message) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");
			Date date = new Date();
			System.out.println("[" + IpAddressClient + ":" + portClient + ":" + socket.getPort() + "-" +  dateFormat.format(date) + "]: " + message);
		}
		
	}
	


}
