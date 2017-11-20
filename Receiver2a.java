import java.io.IOException;
import java.net.SocketTimeoutException;

public class Receiver2a {

	public static void main(String[] args) throws SocketTimeoutException, NoSuchFieldException, IOException {
        if (args.length != 2) {
            System.out.println("Usage: java Receiver2a <LocalPort> <Filename>");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            GBNServer server = new GBNServer(port);
            server.receiveFile(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Port should be a number");
        }
    }

}
