import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class NaiveServer {
	private Receiver receiver;
	
	public NaiveServer(int port) throws SocketException {
		this.receiver = new Receiver(port);
	}
	
	public void receiveFile(String filepath) throws IOException, SocketTimeoutException, PortUnreachableException {
		ByteArrayOutputStream store = new ByteArrayOutputStream();
		Deencapsulator de;
		do {
			DatagramPacket packet = this.receiver.receiveData();
			de = new Deencapsulator(Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength()));
			store.write(de.getData());
			System.out.println("Received "+ packet.getLength()+ " bytes");
		} while (de.getEof() != 1);
		this.writeFile(filepath, store.toByteArray());
		this.receiver.close();
	}
	
	private void writeFile(String filepath, byte[] contents) throws FileNotFoundException, IOException {
		BinaryFileWriter out = new BinaryFileWriter(filepath);
		out.writeAll(contents);
		System.out.println("Wrote " + contents.length + " bytes to disk as " + filepath);
	}
	
}
