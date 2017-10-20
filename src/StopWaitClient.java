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
        for (int pktsTrxedSuccessfully = 0; pktsTrxedSuccessfully < numberOfChunks; pktsTrxedSuccessfully++) {
            // sequence number alternates between 0 and 1
            byte[] seq = {0,(byte) (pktsTrxedSuccessfully % 2)};
            byte eof = (byte) ((pktsTrxedSuccessfully == numberOfChunks-1) ? 1 : 0);
            byte[] payload = fileReader.readChunk();
            this.sender.sendDatagram(seq, eof, payload, this.destIP, this.destPort); 
            boolean received = false;
            //measure number of retransmissions for this specific datagram.
            int trial = 1;
            do {
                //after 10 trials, skip this retransmission. Used to avoid last ACK missed problem
                if (trial++ >= 10) break;
                try {
                    byte[] response = this.receiver.receiveACK();
                    received = Arrays.equals(seq, response);
                    if (received) {
                        // if we received ack of the currently transmitted datagram then it was received
                        // update end time for throughput calculation
                        this.stopTime = System.currentTimeMillis();
                    }
                    // if we received an ack but it wasn't equal to the expected one
                    // then we don't need to do anything
                } catch (SocketTimeoutException e) {
                    //No answer, retransmitting
                    this.sender.sendDatagram(seq, eof, payload, this.destIP, this.destPort); 
                    retransmissionCount++;
                }
                //keep retransmitting until received correct ack or 10 trials 
            } while (!received);
        }
        float throughput = (float) ((fileReader.getLength()/1024.0)/
                ((this.stopTime-this.startTime)/1000.0));
        System.out.println(retransmissionCount+" "+throughput);
        this.cleanup();
    }

    private void cleanup() {
        this.sender.close();
        this.receiver.close();        
    }
    
}
