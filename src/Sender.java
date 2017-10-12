import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Sender {
	
	private DatagramSocket socket;

	public Sender() throws SocketException, UnknownHostException {
		this(new DatagramSocket());
	}
	
	public Sender(DatagramSocket socket) throws UnknownHostException {
		this.socket = socket;
	}
	
	public void sendDatagram(byte[] seq, byte eof, byte[] payload, InetAddress destIP, int destPort) throws IOException, PortUnreachableException {
		Encapsulator enc = new Encapsulator(seq, eof, payload);
		byte[] encData = enc.getEncapsulatedData();
		DatagramPacket packet = new DatagramPacket(encData, encData.length, destIP, destPort);
		this.socket.send(packet);
	}
	
	public void sendACK(byte[] seq, InetAddress destIP, int destPort) throws IOException {
		DatagramPacket packet = new DatagramPacket(seq, seq.length, destIP, destPort);
		this.socket.send(packet);
	}
	
	public void close() {
		this.socket.close();
	}
}
