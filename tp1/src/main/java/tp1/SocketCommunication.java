package tp1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static String getMessage(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        String message     = in.readUTF();
        return message;
    }

    public static boolean receiveMessage(Socket socket) {
        try {
            String message = getMessage(socket);
            if (!message.isEmpty())
                System.out.println(message);
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
        FileInputStream fis     = null;
        BufferedInputStream bis = null;
        OutputStream out        = null;
        try {
            Path path   = Paths.get(filePath);
            File file   = new File(path.toAbsolutePath().normalize().toString());
            byte[] data = new byte[(int) file.length()];
            fis         = new FileInputStream(file);
            bis         = new BufferedInputStream(fis);
            out         = socket.getOutputStream();

            sendMessage(socket, "" + file.length());

            bis.read(data, 0, data.length);
            out.write(data, 0, data.length);
            out.flush();
        } catch (Exception e) {
            SocketCommunication.sendMessage(socket, ""); // Unlocks receiver
            throw e;
        } finally {
            if (bis != null)
                bis.close();
            if (out != null)
                bis.close();
        }
    }

    public static void receiveFile(Socket socket, String filePath) throws IOException {
        FileOutputStream fos     = null;
        BufferedOutputStream bos = null;
        try {
            int nbytes         = Integer.parseInt(getMessage(socket));
            byte[] mybytearray = new byte[nbytes];
            InputStream is     = socket.getInputStream();
            fos                = new FileOutputStream("test.xml");
            bos                = new BufferedOutputStream(fos);
            int bytesRead      = is.read(mybytearray, 0, mybytearray.length);

            bos.write(mybytearray, 0, bytesRead);
            bos.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            if (fos != null)
                fos.close();
            if (bos != null)
                bos.close();
        }
    }
}
