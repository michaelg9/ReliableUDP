import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketException;

/*
 * Michael Michaelides s1447836
 * Runs an rdt3.0 receiver
 */

public class Receiver1b {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java Receiver1b <LocalPort> <Filename>");
			return;
		}

		try {
			int port = Integer.parseInt(args[0]);
			StopWaitServer server = new StopWaitServer(port);
			// System.out.println("Receiver initialized! Listening for packets");
			server.receiveFile(args[1]);
		} catch (NumberFormatException e) {
			System.err.println("Port should be a number");
		} catch (PortUnreachableException e) {
			System.err.println("Port " + args[1]
					+ " unreachable on destination host");
		} catch (SocketException e) {
			System.err
					.println("Unable to create a udp socket: " + e.toString());
		} catch (IOException e) {
			System.err.println("I/O Exception: " + e.toString());
		} catch (Exception e) {
			System.err.println("Error: " + e.toString());
		}
	}

}