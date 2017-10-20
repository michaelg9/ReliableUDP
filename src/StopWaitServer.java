import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

/*
 * Michael Michaelides s1447836
 * Class representing a server receiving data on an unreliable channel.
 * The algorithm implemented is Stop and Wait thus the server need to 
 * acknowledge every packet
 */

public class StopWaitServer {
    private Sender sender;
    private Receiver receiver;
    private byte[] expectedSeq = {0,0};

    public StopWaitServer(int localPort) throws UnknownHostException, SocketException {
        DatagramSocket socket = new DatagramSocket(localPort);
        this.sender = new Sender(socket);
        this.receiver = new Receiver(socket);
    }
    
    public void receiveFile(String filepath) throws IOException, SocketTimeoutException, NoSuchFieldException {
        BinaryFileWriter out = new BinaryFileWriter(filepath);
        byte eof = 0;
        int packetNumber = 0;
        do {
            DatagramPacket packet = this.receiver.receiveData();
            byte[] dataRcved = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength());
            byte[] rcvdSeq = Deencapsulator.getSeqNo(dataRcved);
            eof = Deencapsulator.getEof(dataRcved);
            if (Arrays.equals(expectedSeq, rcvdSeq)) {
                // received expected segment. Deliver data and send ACK with current seq number
                out.writeBuffer(Deencapsulator.getData(dataRcved), eof==1);
                this.sender.sendACK(rcvdSeq, packet.getAddress(), packet.getPort());
                //expect next datagram, seq number alternates between 0 and 1
                this.expectedSeq[1] = (byte) ((this.expectedSeq[1]+1) % 2);
                packetNumber++;
            } else if (this.isRetransmission(rcvdSeq)) {
                // received retransmitted segment. Ignore data and ACK with previous seq number
                this.sender.sendACK(rcvdSeq, packet.getAddress(), packet.getPort());
            }
            // if we didn't make it into any other conditionals above, then wereceived an
            // unexpected sequence number. Reject data and don't respond
        } while (eof != 1);
        this.cleanup();
    }

    //checks if received sequence number is the previous of what expected
    private boolean isRetransmission(byte[] seqRcvd) {
        return (seqRcvd[0] == this.expectedSeq[0]) && 
                ((seqRcvd[1]+1)%2 == this.expectedSeq[1]);
    }
    
    private void cleanup() {
        this.sender.close();
        this.receiver.close();        
    }
}
