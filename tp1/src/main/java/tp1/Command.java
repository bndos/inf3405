package tp1;

public enum Command {
    cd,
    ls,
    mkdir,
    upload,
    download,
    exit;

    public static Command getCmd(String op) {
        try {
            return valueOf(op);
        } catch (Exception e) {
            return null;
        }
    }
}
