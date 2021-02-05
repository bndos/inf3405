package tp1;

import java.io.DataInputStream;
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
        while(invalidInput());

       // Création d'une nouvelle connection avec le serveur
       socket = new Socket(clientIP, clientPort);

       System.out.format("The server is running on %s:%d%n", clientIP, clientPort);
        
       // Création d'un canal entrant pour recevoir les messages 
       DataInputStream in = new DataInputStream(socket.getInputStream());

       // Attente de la reception d'un message envoye par le serveur sur le canal
       String helloMessageFromServer = in.readUTF();
       System.out.println(helloMessageFromServer);

       // Fermeture de la connexion avec le serveur
       socket.close();
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
            
            clientIP = inputArray[i] + '.';
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
            
            clientPort = port;

        return validPort;
    }
}

