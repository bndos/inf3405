package tp1;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server
{   
    // Initialisation des valeurs importantes
    private static ServerSocket listener;
    private static String serverIP = "";
    private static int serverPort = 0;
    private static Scanner clientInput = new Scanner(System.in); // Creation du Scanner pour avoir le user input

    /* Application Serveur*/
    public static void main( String[] args ) throws Exception { 
        // Compteur incremente a chaque connexion d'un client au serveur
        int clientNumber = 1; // premier client

        // Tant que l'addresse et le port ne sont pas valides demander de nouveau
        while(invalidInput());

        // Creation de la connexion pour communiquer avec les clients
        listener = new ServerSocket();
        listener.setReuseAddress(true);
        InetAddress serverAddress = InetAddress.getByName(serverIP);

        //Association de l'adresse et du port a la connexion
        listener.bind(new InetSocketAddress(serverIP, serverPort));

        System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);

        try {
            /* A chaque fois un nouveau client se connecte, on execute la fonction 
               Run() de l<objet ClientHandler 
            */
            while(true) {
                // Important : la fonction accept() est bloquante : attend prochain client se connecte
                // Une nouvelle connection : on incremente le compteur clientNumber
                new ClientHandler(listener.accept(), clientNumber++).start();
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
    private static Boolean invalidInput() {   
        // Demander au client sur quel port il veut se connecter
        System.out.println("Please enter the IP address and the port (between 5000 and 5050) to connect to: ");
        String input = clientInput.nextLine();
        // Decouper le input de maniere que A.B.C.D:E -> [A, B, C, D, E]
        String[] inputArray = input.split("[:\\.]", 6);

        return(validInput(inputArray) && validIP(inputArray) && validPort(inputArray));
    }

    /*
    * Verifier si le input addresse est valide
    */
    private static Boolean validInput(String[] inputArray) {
        boolean validInput = true;

        if(inputArray.length != 5) {
            validInput = false;
            System.out.println(" Please enter a valid input! (eg. 127.0.0.1:5001)");
        }
    
        return(validInput);

    }

    /*
    * Verifier si le IP addresse est valide
    */
    private static Boolean validIP(String[] inputArray) {
        boolean validIP = true;
        int ipSize = 4; // 4 octets
        for(int i = 0; i < ipSize; i++) {
            
            int octet = 0;
            
            // Verifier si ce n'est pas des lettres
            try {
                octet = Integer.parseInt(inputArray[i]);
            }
            catch(NumberFormatException e) {
                validIP = false;
                System.out.println(" Please enter a valid IP number! (no letters)");
            }

            // Verifier si respecte [0, 255]
            if(octet < 0 || octet > 255) {
                validIP = false;
                System.out.println(" Please enter an IP address inside bounderies! (XX.XX.XX.XX:YYYY where XX = [0, 255])");
            }
            
            serverIP = inputArray[i] + '.';
        }
        return validIP;
    }

    /*
    * Verifier si le port est valide
    */
    private static Boolean validPort(String[] inputArray) {

        boolean validPort = true;
        String portInput = inputArray[5];
            
            int port = 0;
            
            // Verifier si ce n'est pas des lettres
            try {
                port = Integer.parseInt(portInput);
            }
            catch(NumberFormatException e) {
                validPort = false;
                System.out.println(" Please enter a valid port number! (no letters)");
            }

            // Verifier si respecte [0, 255]
            if(port < 5000 || port > 5050) {
                validPort = false;
                System.out.println(" Please enter a port number inside bounderies! (XX.XX.XX.XX:YYYY where YYYY = [5000, 5050])");
            }
            
            serverPort = port;

        return validPort;
    }


    /*
    * Un thread qui se charge de traiter la demande de chaque client 
    * sur un socket particulier
    */
    private static class ClientHandler extends Thread {
        private Socket socket;
        private int clientNumber;

        public ClientHandler(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            System.out.println("New connection with client#" + clientNumber + " at " + socket);
        }

        // Un thread se charge d'envoyer au client un message de bienvenue
        public void run() {
            try {
                // Creation d<un canal sortant pour envoyer des messages au client
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                // Envoie d<un message au client 
                out.writeUTF("Hello from server - you are client#" + clientNumber);

            }
            catch (IOException e) {
                System.out.println("Error handling clinet#" + clientNumber + ": " + e);
            }
            finally {
                try {
                    // Fermeture de la connexion avex le client
                    socket.close();
                }
                catch (IOException e) {
                    System.out.println("Couldn't close a socket, what's going on?");
                }
                System.out.println("Connection with client# " + clientNumber + " closed");
            }
        }
    }
}