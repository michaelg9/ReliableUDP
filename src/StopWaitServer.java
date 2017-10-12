import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

//what do if rcv unexpected seq num?

public class StopWaitServer {
	private Sender sender;
	private Receiver receiver;
	private byte[] expectedSeq = {0,0};
	private Random r = new Random();

	public StopWaitServer(int localPort) throws UnknownHostException, SocketException {
		DatagramSocket socket = new DatagramSocket(localPort);
		this.sender = new Sender(socket);
		this.receiver = new Receiver(socket);
	}
	
	public void receiveFile(String filepath) throws IOException, SocketTimeoutException, PortUnreachableException {
		ByteArrayOutputStream store = new ByteArrayOutputStream();
		Deencapsulator de;
		do {
			DatagramPacket packet = this.receiver.receiveData();
			de = new Deencapsulator(Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength()));
			System.out.println(packet.getLength());
			//System.out.print("ACKed. Received: "+de.getSeqNo()[1]+". Expected: "+this.expectedSeq[1]+". ");
			if (Arrays.equals(expectedSeq, de.getSeqNo())) {
				this.acceptDatagram(store, de.getData());
			} else {
				System.out.println("Rejected - retransmission ACK"+de.getSeqNo()[1]);
			}
			this.sender.sendACK(de.getSeqNo(), packet.getAddress(), packet.getPort());
		} while (de.getEof() != 1);
		this.writeFile(filepath, store.toByteArray());
		this.cleanup();
	}
	
	private void writeFile(String filepath, byte[] contents) throws FileNotFoundException, IOException {
		BinaryFileWriter out = new BinaryFileWriter(filepath);
		out.writeAll(contents);
		System.out.println("Wrote " + contents.length + " bytes to disk as " + filepath);
	}
	
	private void acceptDatagram(ByteArrayOutputStream store, byte[] data) throws IOException {
		this.expectedSeq[1] = (byte) ((this.expectedSeq[1]+1) % 2);
		store.write(data);
		System.out.println("Accepted length: "+data.length);
	}
	
	private void cleanup() {
		this.sender.close();
		this.receiver.close();		
	}
}
