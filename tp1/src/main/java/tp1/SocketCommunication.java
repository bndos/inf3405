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
            System.out.println("Erreure lors de la reception du message");
            return false;
        }
        return true;
    }

    public static boolean exchangeMessages(Socket socket, String message) {
        boolean success = (sendMessage(socket, message)) ? receiveMessage(socket) : false;
        return success;
    }

    public static void sendFile(Socket socket, String currentPath, String fileName, boolean feedback)
        throws IOException, NoSuchFileException {
        FileInputStream fis     = null;
        BufferedInputStream bis = null;
        OutputStream out        = null;
        try {
            File file       = new File(joinPaths(currentPath, fileName));
            long fileLength = file.length();
            int freeMem     = Runtime.getRuntime().freeMemory() > Integer.MAX_VALUE
                                ? Integer.MAX_VALUE
                                : (int) Runtime.getRuntime().freeMemory();
            int curLength   = fileLength > freeMem - 1 ? freeMem - 1 : (int) fileLength;

            byte[] data = new byte[curLength];

            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            out = socket.getOutputStream();

            sendMessage(socket, "" + file.length());

            long leftSize   = fileLength;
            long percentage = 0;
            while (curLength > 0) {
                bis.read(data, 0, curLength);
                out.write(data, 0, curLength);

                leftSize -= curLength;
                curLength = leftSize > freeMem - 1 ? freeMem - 1 : (int) leftSize;
                data      = new byte[curLength];

                if (feedback && 100 * (fileLength - leftSize) / fileLength > percentage) {
                    System.out.println((percentage = (fileLength - leftSize) * 100 / fileLength)
                                       + "%");
                }
            }

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

    public static String getFileName(String pathName) {
        Path path = Paths.get(pathName).normalize();
        return path.getFileName().toString();
    }

    public static String joinPaths(String currentDir, String pathName) {
        Path path = Paths.get(pathName).normalize();
        return path.isAbsolute() ? pathName : currentDir + "/" + pathName;
    }

    public static void receiveFile(Socket socket, String currentPath, String fileName, boolean feedback)
        throws IOException {
        FileOutputStream fos     = null;
        BufferedOutputStream bos = null;
        try {
            long fileSize  = Long.parseLong(getMessage(socket));
            int freeMem    = Runtime.getRuntime().freeMemory() > Integer.MAX_VALUE
                               ? Integer.MAX_VALUE
                               : (int) Runtime.getRuntime().freeMemory();
            int curSize    = fileSize > freeMem - 1 ? freeMem - 1 : (int) fileSize;
            byte[] buffer  = new byte[curSize];
            InputStream is = socket.getInputStream();
            fos            = new FileOutputStream(joinPaths(currentPath, getFileName(fileName)));
            bos            = new BufferedOutputStream(fos);

            long leftSize   = fileSize;
            long percentage = 0;
            while (curSize > 0) {
                int totalRead = 0;
                while (totalRead < curSize) {
                    int bytesRead = is.read(buffer, totalRead, curSize - totalRead);
                    if (bytesRead < 0) {
                        throw new IOException("Data stream ended prematurely");
                    }
                    totalRead += bytesRead;
                }

                bos.write(buffer, 0, totalRead);

                leftSize -= curSize;
                curSize = leftSize > freeMem - 1 ? freeMem - 1 : (int) leftSize;
                buffer  = new byte[curSize];

                if (feedback && 100 * (fileSize - leftSize) / fileSize > percentage) {
                    System.out.println((percentage = (fileSize - leftSize) * 100 / fileSize) + "%");
                }
            }
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
