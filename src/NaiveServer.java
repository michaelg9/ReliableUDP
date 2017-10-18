import java.io.IOException;
import java.net.DatagramPacket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/*
 * Michael Michaelides s1447836
 * Class representing a server receiving packets from a
 * reliable channel(rdt1.0). No provision for ACKs
 */
public class NaiveServer {
	private Receiver receiver;

	public NaiveServer(int port) throws SocketException {
		this.receiver = new Receiver(port);
	}

	public void receiveFile(String filepath) throws IOException,
			SocketTimeoutException, PortUnreachableException,
			NoSuchFieldException {
		BinaryFileWriter out = new BinaryFileWriter(filepath);
		byte eof = 0;
		do {
			DatagramPacket packet = this.receiver.receiveData();
			byte[] dataRcved = Arrays.copyOfRange(packet.getData(),
					packet.getOffset(), packet.getLength());
			eof = Deencapsulator.getEof(dataRcved);
			out.writeBuffer(Deencapsulator.getData(dataRcved), eof == 1);
			// System.out.println("Received "+ packet.getLength()+ " bytes");
		} while (eof != 1);
		this.receiver.close();
	}

}
