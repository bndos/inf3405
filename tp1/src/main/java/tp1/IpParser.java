package tp1;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpParser {
    private static final Pattern IPV4_PATTERN =
        Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
                        + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
                        + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
                        + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    private static final Pattern PORT_PATTERN = Pattern.compile("^(50[0-5][0-9])$");

    private static Scanner scanner = new Scanner(System.in);

    public static String getIP() throws Exception {
        System.out.print("Adresse ip: ");

        String ip       = scanner.nextLine();
        String error    = "L'adresse ip devrait etre dans le format '0-255.0-255.0-255.0-255'";
        Matcher matcher = IPV4_PATTERN.matcher(ip);

        if (!matcher.matches())
            throw new Exception(error);

        return ip;
    }

    public static int getPort() throws Exception {
        System.out.print("Port: ");

        String error    = "Le port devrait etre entre 5000 et 5050";
        String port     = scanner.nextLine();
        Matcher matcher = PORT_PATTERN.matcher(port);

        if (!matcher.matches())
            throw new Exception(error);

        return Integer.parseInt(port);
    }
}
