package tp1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public class Api {
    private static File cwd = new File(".");
    private static ArrayList<Function<String[], String>> commands;
    private static Socket socket;

    public static enum Command {
        CD,
        LS,
        MKDIR,
        UPLOAD,
        DOWNLOAD,
        EXIT;

        public static Api.Command getCmd(String op) {
            try {
                return valueOf(op.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }
    }

    static private Function<String[], String> cd = (args) -> {
        if (args == null || args.length > 2 || args.length < 2)
            return "";

        File nextDir = new File(Paths.get(cwd.getAbsolutePath()).normalize() + "/" + args[1]);
        if (!nextDir.exists())
            return args[0] + ": No such file or directory: " + args[1];
        if (!nextDir.isDirectory())
            return args[0] + ": Not a directory: " + args[1];

        cwd = nextDir;

        return "";
    };

    static private Function<String[], String> ls = (args) -> {
        String files = "";
        for (String file : cwd.list()) {
            files += file + " ";
        }

        return files;
    };

    static private Function<String[], String> mkdir = (args) -> {
        int i = 0;
        for (String dir : args) {
            if (i++ != 0) {
                File dirFile = new File(dir);
                if (!dirFile.mkdir())
                    return "Couldn't make dir: " + dir;
            }
        }

        return "";
    };

    static private Function<String[], String> upload = (args) -> {
        for (String dir : args) {
            File absoluteFile = new File(cwd.getAbsoluteFile() + dir);
            if (!absoluteFile.mkdir())
                return "Couldn't make dir: " + dir;
        }

        return "";
    };

    static private Function<String[], String> download = (args) -> {
        try {
            SocketCommunication.sendFile(socket, args[1]);
        } catch (Exception e) {
            return "An error occured downloading file";
        }

        return "";
    };

    static private Function<String[], String> exit = (args) -> {
        return "";
    };

    public Api(Socket s) {
        socket   = s;
        commands = new ArrayList<Function<String[], String>>(
            Arrays.asList(cd, ls, mkdir, upload, download, exit));
    }

    public String exec(String[] args) {
        if (args == null)
            return "";

        int commandIndex = Command.getCmd(args[0]).ordinal();
        return commands.get(commandIndex).apply(args);
    }
}
