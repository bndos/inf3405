package tp1;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Api {
    private File cwd;
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

    private void changeDir(String path) {
        this.cwd = new File(path);
    }

    private abstract class Operation {
        public File currentDirectory;

        Operation(File dir) {
            System.out.println(dir == null);
            this.currentDirectory = dir;
        }

        public String execute(String[] args) {
            return "";
        }
    }

    private class cd extends Operation {
        cd(File dir) {
            super(dir);
        }

        public String execute(String[] args) {
            if (args == null || args.length > 2 || args.length < 2)
                return "";

            File nextDir = new File(args[1]);
            System.out.println(nextDir.getAbsolutePath());
            if (!nextDir.exists())
                return args[0] + ": No such file or directory: " + args[1];
            if (!nextDir.isDirectory())
                return args[0] + ": Not a directory: " + args[1];

            this.currentDirectory = new File(nextDir.getAbsolutePath());

            return cwd.getAbsolutePath();
        }
    }

    private class ls extends Operation {
        ls(File dir) {
            super(dir);
        }

        public String execute(String[] args) {
            String files = "";
            for (String file : this.currentDirectory.list()) {
                files += file + " ";
            }

            System.out.println(cwd.getAbsolutePath());

            return files;
        }
    }
    private class mkdir extends Operation {
        mkdir(File dir) {
            super(dir);
        }

        public String execute(String[] args) {
            int i = 0;
            for (String dir : args) {
                if (i++ != 0) {
                    File dirFile = new File(dir);
                    if (!dirFile.mkdir())
                        return "Couldn't make dir: " + dir;
                }
            }

            return "";
        }
    }

    private class upload extends Operation {
        upload(File dir) {
            super(dir);
        }

        public String execute(String[] args) {
            for (String dir : args) {
                File absoluteFile = new File(cwd.getAbsoluteFile() + dir);
                if (!absoluteFile.mkdir())
                    return "Couldn't make dir: " + dir;
            }

            return "";
        }
    }

    private class download extends Operation {
        download(File dir) {
            super(dir);
        }

        public String execute(String[] args) {
            String files = "";
            for (String file : cwd.list()) {
                files += file + " ";
            }

            return files;
        }
    }

    private class exit extends Operation {
        exit(File dir) {
            super(dir);
        }

        public String execute(String[] args) {
            return "";
        }
    }

    private ArrayList<Operation> commands =
        new ArrayList<Operation>(Arrays.asList(new cd(this.cwd),
                                               new ls(this.cwd),
                                               new mkdir(this.cwd),
                                               new upload(this.cwd),
                                               new download(this.cwd),
                                               new exit(this.cwd)));

    public Api(Socket s) {
        this.cwd    = new File(".");
        this.socket = s;
    }

    public String exec(String[] args) {
        if (args == null)
            return "";

        int commandIndex = Command.getCmd(args[0]).ordinal();
        return commands.get(commandIndex).execute(args);
    }
}
