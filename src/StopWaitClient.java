import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

public class StopWaitClient {
	private Sender sender;
	private Receiver receiver;
	private InetAddress destIP;
	private int destPort;
	private Random r = new Random();
			
	public StopWaitClient(String dIP, int destPort, int timeout) throws UnknownHostException, SocketException {
		DatagramSocket socket = new DatagramSocket();
		socket.setSoTimeout(timeout);
		this.sender = new Sender(socket);
		this.receiver = new Receiver(socket);
		this.destIP = InetAddress.getByName(dIP);
		this.destPort = destPort;
	}

	public void sendFile(String filename) throws IOException, PortUnreachableException {
		BinaryFileReader fileReader = new BinaryFileReader(filename);
		byte[][] payloads = fileReader.readChunks(1023);
		System.out.println("File read and split into "+ payloads.length + " packets");
		for (int i = 0; i < payloads.length; i++) {
			byte eof = (byte) ((i < payloads.length -1) ? 0 : 1);
			byte[] seq = {0,(byte) (i % 2)};
			boolean received = false;
			do {
				this.sender.sendDatagram(seq, eof, payloads[i], this.destIP, this.destPort);
				System.out.print("Packet "+ i + " transmitted. ");
				byte[] response = {1,1};
				try {
					response = this.receiver.receiveACK();
				} catch (SocketTimeoutException e) {
					System.out.println("No answer. Retrying...");
				}
				received = Arrays.equals(seq, response);
				if (received) {
					System.out.println("Success! ACK expected: "+ seq[1]+". Received: "+response[1]);
				} else {
					System.out.println("Failure! ACK expected: "+ seq[1]+". Received: "+response[1]);
				}
			} while (!received);
		}
		System.out.println("\nFile sent! Length: " + fileReader.getLength());
		this.cleanup();
	}

	private void cleanup() {
		this.sender.close();
		this.receiver.close();		
	}
	
}
