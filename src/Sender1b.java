import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * Michael Michaelides s1447836
 */

public class Sender1b {
	
	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Usage: java Sender1b <RemoteHost> <RemotePort> <Filename> <RetryTimeout>");
			return;
		}
		try {
			int port = Integer.parseInt(args[1]);
			int retryTimeout = Integer.parseInt(args[3]);
			StopWaitClient sender = new StopWaitClient(args[0], port, retryTimeout);
			//System.out.println("Sender initialized!");
			sender.sendFile(args[2]);
		} catch (NumberFormatException e) {
			System.err.println("RemotePort and RetryTimeout should be a number");
		}  catch (PortUnreachableException e) {
			System.err.println("Port "+ args[1] + " unreachable on destination host");
		} catch (UnknownHostException e) {
			System.err.println("Unknown host " +args[0]);
		} catch (SocketException e) {
			System.err.println("Unable to create a udp socket: " + e.toString());
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + args[2]);
		} catch (IOException e) {
			System.err.println("I/O Exception: " + e.toString());
		}  catch (Exception e) {
			System.err.println("Error: " + e.toString());
		}
	}

}
