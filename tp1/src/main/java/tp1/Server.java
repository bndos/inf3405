package tp1;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server
{   
    // Initialisation des valeurs importantes
    private static ServerSocket listener;
    private static String serverIP = "";
    private static int serverPort = 0;
    private static Scanner clientInput = new Scanner(System.in); // Creation du Scanner pour avoir le user input
    private static Path currentDirectory = Paths.get("C:/");

    /* Application Serveur*/
    public static void main( String[] args ) throws Exception { 
        // Compteur incremente a chaque connexion d'un client au serveur
        int clientNumber = 1; // premier client

        // Tant que l'addresse et le port ne sont pas valides demander de nouveau
        while(!validInput());

        // Creation de la connexion pour communiquer avec les clients
        listener = new ServerSocket();
        listener.setReuseAddress(true);

        //Association de l'adresse et du port a la connexion
        listener.bind(new InetSocketAddress(serverIP, serverPort));

        System.out.format("The server is running on %s:%d%n", serverIP, serverPort);

        try {
            /* A chaque fois un nouveau client se connecte, on execute la fonction 
               Run() de l'objet ClientHandler 
            */
            while(true) {
                // Important : la fonction accept() est bloquante : attend prochain client se connecte
                // Une nouvelle connection : on incremente le compteur clientNumber
                new ClientHandler(listener.accept(), clientNumber++, currentDirectory).start();
            }
        }
        finally {
            // Fermeture de la connection
            listener.close();
        }
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

            // Verifier si respecte [0, 255]
            if(octet < 0 || octet > 255) {
                System.out.println("Please enter an IP address inside bounderies! (XX.XX.XX.XX:YYYY where XX = [0, 255])");
                return false;
            }
        }
        serverIP = inputArray[0] + '.' + inputArray[1] + '.' + inputArray[2] + '.' + inputArray[3];
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
            
            serverPort = port;

        return true;
    }
}