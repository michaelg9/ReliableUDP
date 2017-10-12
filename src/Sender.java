import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class Sender {
	
	private InetAddress destIP;
	private int destPort;
	private DatagramSocket socket;
	
	public Sender(String dIP, int destPort) throws UnknownHostException, SocketException {
		this.destIP = InetAddress.getByName(dIP);
		this.destPort = destPort;
		this.socket = new DatagramSocket();
	}
	
	public void send(byte[] payload) throws IOException, PortUnreachableException {
		byte[] seq = {0,0};
		Encapsulator enc = new Encapsulator(seq, (byte)1, payload);
		byte[] encData = enc.getEncapsulatedData();
		DatagramPacket packet = new DatagramPacket(encData, encData.length, this.destIP, this.destPort);
		this.socket.send(packet);
	}
	
	public void send(byte[][] payloads) throws IOException, PortUnreachableException {
		DatagramPacket packet;
		for (int i = 0; i < payloads.length; i++) {
			byte eof = (byte) ((i < payloads.length -1) ? 0 : 1);
			byte[] seq = {0,0};
			Encapsulator enc = new Encapsulator(seq, eof, payloads[i]);
			byte[] encData = enc.getEncapsulatedData();
			packet = new DatagramPacket(encData, encData.length, this.destIP, this.destPort);
			this.socket.send(packet);
			System.out.print(".");
			try { TimeUnit.MILLISECONDS.sleep(10);} catch (InterruptedException e) {}
		}
	}
	
	public void close() {
		this.socket.close();
	}
}
