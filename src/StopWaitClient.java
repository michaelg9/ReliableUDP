import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

/*
 * Michael Michaelides s1447836
 * Class representing a client sending data and receiving control packets
 * on an unreliable channel. The algorithm implemented is Stop and Wait.
 */

public class StopWaitClient {
	private Sender sender;
	private Receiver receiver;
	private InetAddress destIP;
	private int destPort;
	private int chunckSize = 1024;
	private long startTime;
	private long stopTime;
			
	public StopWaitClient(String dIP, int destPort, int timeout) 
			throws UnknownHostException, SocketException {
		DatagramSocket socket = new DatagramSocket();
		// server has to response in less than 'timeout' milliseconds, otherwise retransmit
		socket.setSoTimeout(timeout);
		this.sender = new Sender(socket);
		this.receiver = new Receiver(socket);
		this.destIP = InetAddress.getByName(dIP);
		this.destPort = destPort;
	}
	
	public void sendFile(String filename) throws IOException, PortUnreachableException {
		BinaryFileReader fileReader = new BinaryFileReader(filename, this.chunckSize);
		int retransmissionCount = 0;
		int numberOfChunks = fileReader.getNumberOfChunks();
		this.startTime = System.currentTimeMillis();
		//System.out.println("File read and split into "+ numberOfChunks + " chunks");
		for (int pktsTrxedSuccessfully = 0; pktsTrxedSuccessfully < numberOfChunks; pktsTrxedSuccessfully++) {
			// sequence number alternates between 0 and 1
			byte[] seq = {0,(byte) (pktsTrxedSuccessfully % 2)};
			byte eof = (byte) ((pktsTrxedSuccessfully == numberOfChunks-1) ? 1 : 0);
			byte[] payload = fileReader.readChunk();
			//System.out.println("Packet "+ pktsTrxedSuccessfully + " transmitted("+(payload.length+3)+"B):");
			this.sender.sendDatagram(seq, eof, payload, this.destIP, this.destPort); 
			boolean received = false;
			//measure number of retransmissions for this specific datagram.
			int trial = 1;
			do {
				//after 10 trials, skip this retransmission. Used to avoid last ACK missed problem
				if (trial++ >= 10) break;
				try {
					byte[] response = this.receiver.receiveACK();
					// if we received ack of the currently transmitted datagram then it was received
					received = Arrays.equals(seq, response);
					if (received) {
						//System.out.println("\tSuccess! Received ACK"+response[1]);
						//used to how much time it took to send the whole file.
						this.stopTime = System.currentTimeMillis();
					} else {
						//System.out.println("\tFailure! ACK expected: "+ seq[0]+seq[1]+". Received: "+response[0]+response[1]+". Waiting...");
					}
				} catch (SocketTimeoutException e) {
					//System.out.println("\tNo answer. Retransmitting... ");
					this.sender.sendDatagram(seq, eof, payload, this.destIP, this.destPort); 
					retransmissionCount++;
				}
				//keep retransmitting until received correct ack or 10 trials 
			} while (!received);
		}
		float throughput = (float) ((this.stopTime-this.startTime)/(fileReader.getLength()/1024.0));
		System.out.println(retransmissionCount+" "+throughput);
		this.cleanup();
	}

	private void cleanup() {
		this.sender.close();
		this.receiver.close();		
	}
	
}
