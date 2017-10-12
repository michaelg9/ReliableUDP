import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class Receiver {
	private DatagramSocket socket;
	
	public Receiver(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
	}
	
	public Receiver(DatagramSocket socket){
		this.socket = socket;
	}
	
	public DatagramPacket receiveData() throws IOException, SocketTimeoutException {
		byte[] buffer = new byte[1030];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		return packet;		
	}
	
	public byte[] receiveACK() throws IOException, SocketTimeoutException {
		DatagramPacket packet = this.receiveData();
		if (packet.getLength() != 2) return null;
		Deencapsulator de = new Deencapsulator(Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength()));
		return de.getSeqNo();
	}
	
	public void close() {
		this.socket.close();
	}
}
