import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

/*
 * Michael Michaelides s1447836
 * Class representing a client receiving data and sending control packets
 * on an unreliable channel. The algorithm implemented is go back n.
 */

public class GBNServer {
    private Sender sender;
    private Receiver receiver;
    private SequenceNumber expectedSeq = new SequenceNumber(1);
		
	public GBNServer(int localPort) throws SocketException, UnknownHostException {
        DatagramSocket socket = new DatagramSocket(localPort);
        this.sender = new Sender(socket);
        this.receiver = new Receiver(socket);
	}
	
    public void receiveFile(String filepath) throws IOException, NoSuchFieldException {
        BinaryFileWriter out = new BinaryFileWriter(filepath);
        // will keep the sequence number of the last packet.
     	// used to help determine when to terminate.
        SequenceNumber eofSeq = null;
        do {
            DatagramPacket packet = this.receiver.receiveData();
            byte[] dataRcved = packet.getData();
            SequenceNumber rcvdSeq = new SequenceNumber(Deencapsulator.getSeqNo(dataRcved));
            System.out.println("rcvd ("+packet.getLength()+"): "+ rcvdSeq.toIntString());
            if (expectedSeq.equals(rcvdSeq.toBytes())) {
            	// received base packet
            	boolean eof = Deencapsulator.getEof(dataRcved) == 1;
				if (eof) eofSeq = rcvdSeq;
            	System.out.println("\texpected! Acking: "+expectedSeq.toInt());
                // trim packet to its actual length
            	byte[] payload = Deencapsulator.getData(dataRcved, packet.getLength());
            	// deliver data
                out.writeBuffer(payload, eof);
                if (new Random().nextInt(100) < 80) { 
                	this.sender.sendACK(expectedSeq.toBytes(), packet.getAddress(), packet.getPort());
                } else System.out.println("\tLOST");
                //expect next datagram, increment seqNo
                expectedSeq.increment();
            } else {
                // received retransmitted segment. Ignore data and ACK with previous seq number
            	System.out.println("\tUNexpected! rcvd: "+rcvdSeq.toIntString()+ "exp: "+expectedSeq.toIntString());
                this.sender.sendACK(new SequenceNumber(expectedSeq.toInt()-1).toBytes(), packet.getAddress(), packet.getPort());
            }
          //stop when last packet has arrived and has been acked
        } while (eofSeq == null || eofSeq.toInt() >= expectedSeq.toInt());
        System.out.println("Received eof!");
        this.cleanup();
    }
    
	private void cleanup() {
        this.sender.close();
        this.receiver.close();        
    }

}
