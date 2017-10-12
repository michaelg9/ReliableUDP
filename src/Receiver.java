import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class Receiver {
	private DatagramSocket socket;
	
	public Receiver(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
	}
	
	public byte[] receive() throws IOException, SocketTimeoutException, PortUnreachableException {
		ByteArrayOutputStream store = new ByteArrayOutputStream();
		Deencapsulator de;
		do {
			byte[] buffer = new byte[1026];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			de = new Deencapsulator(Arrays.copyOfRange(buffer, 0, packet.getLength()));
			store.write(de.getData());
			System.out.println("Received "+ packet.getLength()+ "bytes");
		} while (de.getEof() != 1);
		return store.toByteArray();
	}
	
	public void close() {
		this.socket.close();
	}
}
