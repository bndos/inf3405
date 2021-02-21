package tp1;

import java.io.File;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public class Api {
    private File cwd = new File(".");
    private ArrayList<Function<String[], String>> commands;
    private Socket socket;

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

    private Function<String[], String> cd = (args) -> {
        if (args == null || args.length > 2 || args.length < 2)
            return "";

        File nextDir = new File(Paths.get(cwd.getAbsolutePath()).normalize() + "/" + args[1]);
        if (!nextDir.exists())
            return args[0] + ": No such file or directory: " + args[1];
        if (!nextDir.isDirectory())
            return args[0] + ": Not a directory: " + args[1];

        this.cwd = nextDir;

        return "";
    };

    private Function<String[], String> ls = (args) -> {
        String files = "";
        for (String fileName : this.cwd.list()) {
            File file = new File(fileName);
            files += file.isDirectory()? "[Folder] " + fileName + "\n" : "[File]   " + fileName + "\n";
        }

        files = files.substring(0, files.length() - 1);
        return files;
    };

    private Function<String[], String> mkdir = (args) -> {
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

    private Function<String[], String> upload = (args) -> {
        try {
            SocketCommunication.receiveFile(
                this.socket, Paths.get(this.cwd.getAbsolutePath()).normalize().toString(), args[1]);
        } catch (Exception e) {
            return "Error receiving file";
        }

        return "File successfully uploaded";
    };

    private Function<String[], String> download = (args) -> {
        try {
            SocketCommunication.sendFile(this.socket, this.cwd.getAbsolutePath(), args[1]);
        } catch (Exception e) {
            return e.getMessage();
        }

        return "";
    };

    private Function<String[], String> exit = (args) -> {
        return "";
    };

    public Api(Socket s) {
        this.socket   = s;
        commands = new ArrayList<Function<String[], String>>(
            Arrays.asList(cd, ls, mkdir, upload, download, exit));
    }

    public String exec(String[] args) {
        if (args == null)
            return "";

        int commandIndex = Command.getCmd(args[0]).ordinal();
        return this.commands.get(commandIndex).apply(args);
    }
}
