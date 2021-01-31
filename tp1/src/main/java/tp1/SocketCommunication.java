package tp1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.NoSuchFileException;

public class SocketCommunication {
    public static boolean sendMessage(Socket socket, String message) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(message);
        } catch (IOException e) {
            System.out.println("Error sending message");
            return false;
        }
        return true;
    }

    private static String getMessage(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        String response    = in.readUTF();
        return response;
    }

    public static boolean receiveMessage(Socket socket) {
        try {
            String response = getMessage(socket);
            if (!response.isEmpty())
                System.out.println(response);
        } catch (IOException e) {
            System.out.println("Error receiving message");
            return false;
        }
        return true;
    }

    public static boolean exchangeMessages(Socket socket, String message) {
        boolean success = (sendMessage(socket, message)) ? receiveMessage(socket) : false;
        return success;
    }

    public static void sendFile(Socket socket, String filePath)
        throws IOException, NoSuchFileException {
        File file               = new File(filePath);
        byte[] data             = new byte[(int) file.length()];
        FileInputStream fis     = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        OutputStream out        = socket.getOutputStream();

        sendMessage(socket, "" + file.length());

        bis.read(data, 0, data.length);
        out.write(data, 0, data.length);
        out.flush();
        System.out.println("Done.");

        if (bis != null)
            bis.close();
        if (out != null)
            bis.close();
    }

    public static void receiveFile(Socket socket, String filePath) throws IOException {
        int nbytes               = Integer.parseInt(getMessage(socket));
        byte[] mybytearray       = new byte[nbytes];
        InputStream is           = socket.getInputStream();
        FileOutputStream fos     = new FileOutputStream("test.xml");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int bytesRead            = is.read(mybytearray, 0, mybytearray.length);

        bos.write(mybytearray, 0, bytesRead);
        bos.flush();
        System.out.println("File " + filePath + " downloaded (" + bytesRead + " bytes read)");

        if (fos != null)
            fos.close();
        if (bos != null)
            bos.close();

        SocketCommunication.receiveMessage(socket);
    }
}
