import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

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
        byte eof = 0;
        do {
            DatagramPacket packet = this.receiver.receiveData();
            byte[] dataRcved = packet.getData();
            SequenceNumber rcvdSeq = new SequenceNumber(Deencapsulator.getSeqNo(dataRcved));
            System.out.println("rcvd ("+packet.getLength()+"): "+ rcvdSeq.toIntString());
            if (expectedSeq.equals(rcvdSeq.toBytes())) {
            	eof = Deencapsulator.getEof(dataRcved);
            	System.out.println("\texpected! Acking: "+expectedSeq.toInt());
                // received expected segment. Deliver data and send ACK with current seq number
            	byte[] payload = Deencapsulator.getData(dataRcved, packet.getLength());
                out.writeBuffer(payload, eof==1);
                this.sender.sendACK(expectedSeq.toBytes(), packet.getAddress(), packet.getPort());
                //expect next datagram, increment seqNo
                expectedSeq.increment();
            } else {
                // received retransmitted segment. Ignore data and ACK with previous seq number
            	System.out.println("\tUNexpected! rcvd: "+rcvdSeq.toIntString()+ "exp: "+expectedSeq.toIntString());
                this.sender.sendACK(new SequenceNumber(expectedSeq.toInt()-1).toBytes(), packet.getAddress(), packet.getPort());
            }
            // if we didn't make it into any other conditionals above, then wereceived an
            // unexpected sequence number. Reject data and don't respond
        } while (eof != 1);
        System.out.println("Received eof!");
        this.cleanup();
    }
    
	private void cleanup() {
        this.sender.close();
        this.receiver.close();        
    }

}
