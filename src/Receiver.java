import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/*
 * Michael Michaelides s1447836
 * Class that represents a socket receiving data or control packets
 * from a link
 */

public class Receiver {
	private DatagramSocket socket;

	public Receiver(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
	}

	// extra constructor in case the programmer needs specific
	// configurations on the socket (rdt3.0)
	public Receiver(DatagramSocket socket) {
		this.socket = socket;
	}

	public DatagramPacket receiveData() throws IOException,
			SocketTimeoutException {
		byte[] buffer = new byte[1030];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		return packet;
	}

	public byte[] receiveACK() throws IOException, SocketTimeoutException {
		DatagramPacket packet = this.receiveData();
		if (packet.getLength() != 2)
			throw new IllegalStateException(
					"Expecting control packet but received data.");
		return Deencapsulator.getSeqNo(Arrays.copyOfRange(packet.getData(),
				packet.getOffset(), packet.getLength()));
	}

	public void close() {
		this.socket.close();
	}
}
