import java.io.IOException;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class NaiveClient {
	private InetAddress destIP;
	private int destPort;
	private Sender sender;
	
	public NaiveClient(String dIP, int destPort) throws UnknownHostException, SocketException {
		this.sender = new Sender();
		this.destIP = InetAddress.getByName(dIP);
		this.destPort = destPort;
	}
	
	public void sendFile(String filename) throws IOException, PortUnreachableException {
		BinaryFileReader fileReader = new BinaryFileReader(filename);
		byte[][] payloads = fileReader.readChunks(1023);
		System.out.println("File read and split into "+ payloads.length + " packets");
		for (int i = 0; i < payloads.length; i++) {
			byte eof = (byte) ((i < payloads.length -1) ? 0 : 1);
			byte[] seq = {0,0};
			this.sender.sendDatagram(seq, eof, payloads[i], this.destIP, this.destPort);
			System.out.print(".");
			try { TimeUnit.MILLISECONDS.sleep(10);} catch (InterruptedException e) {}
		}
		System.out.println("\nFile sent! Length: " + fileReader.getLength());
		this.sender.close();
	}

}
