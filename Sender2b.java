import java.io.FileNotFoundException;
import java.io.IOException;

public class Sender2b {

	public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Usage: java Sender2b <RemoteHost> <RemotePort> <Filename> <RetryTimeout> <WindowSize>");
            return;
        }
        try {
            int remotePort = Integer.parseInt(args[1]);
            int retryTimeout = Integer.parseInt(args[3]);
            int windowSize = Integer.parseInt(args[4]);
            SRClient sender = new SRClient(args[0], remotePort, retryTimeout, windowSize);
            sender.sendFile(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("RemotePort, RetryTimeout and WindowSize should be integers");
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + args[2]);
        } catch (IOException e) {
            System.err.println("I/O Exception: " + e.toString());
        }
    }
}
