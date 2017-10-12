import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.nio.file.FileAlreadyExistsException;

public class Receiver1a {
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java Receiver 1a <Port> <Filename>");
			return;
		}
		
		try {
			BinaryFileWriter out = new BinaryFileWriter(args[1]);
			int port = Integer.parseInt(args[0]);
			Receiver receiver = new Receiver(port);
			System.out.println("Receiver initialized! Listening for packets");
			byte[] data = receiver.receive();
			System.out.println("\nGot it!! Length: " + data.length);
			receiver.close();
			out.write(data);
		} catch (NumberFormatException e) {
			System.err.println("Port should be a number");
		} catch (FileAlreadyExistsException e) {
			System.err.println("File already exists. Can't overwrite");
		} catch (PortUnreachableException e) {
			System.err.println("Port "+ args[1] + " unreachable on destination host");
		} catch (SocketException e) {
			System.err.println("Unable to create a udp socket: " + e.toString());
		} catch (IOException e) {
			System.err.println("I/O Exception: " + e.toString());
		}  catch (Exception e) {
			System.err.println("Error: " + e.toString());
		}
	}

}