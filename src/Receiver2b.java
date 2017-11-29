import java.io.IOException;
import java.net.SocketTimeoutException;

/*
 * Michael Michaelides s1447836
 * Runs a selective repeat receiver
 */

public class Receiver2b {

	public static void main(String[] args) throws SocketTimeoutException, NoSuchFieldException, IOException {
        if (args.length != 3) {
            System.out.println("Usage: java Receiver2b <LocalPort> <Filename> <WindowSize>");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            int windowSize = Integer.parseInt(args[2]);
            SRServer server = new SRServer(port, windowSize);
            server.receiveFile(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Port and WindowSize should be a positive integer");
        }
    }

}
