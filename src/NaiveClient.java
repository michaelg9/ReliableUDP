import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/*
 * Michael Michaelides s1447836
 * Represents a client sending packets on a reliable channel(rdt1.0).
 * No provision for retransmissions, only sends the data and exits
 */
public class NaiveClient {
    private InetAddress destIP;
    private int destPort;
    private Sender sender;
    private int chunckSize = 1024;
    
    public NaiveClient(String dIP, int destPort) throws UnknownHostException, SocketException {
        this.sender = new Sender();
        this.destIP = InetAddress.getByName(dIP);
        this.destPort = destPort;
    }
    
    public void sendFile(String filename) throws IOException {
        BinaryFileReader fileReader = new BinaryFileReader(filename, this.chunckSize);
        int numberOfChunks = fileReader.getNumberOfChunks();
        //sequence number is just a constant since it's not being used
        byte[] seq = {0,0};
        for (int pktsTrxedSuccessfully = 0; pktsTrxedSuccessfully < numberOfChunks; pktsTrxedSuccessfully++) {
            byte eof = (byte) ((pktsTrxedSuccessfully == numberOfChunks-1) ? 1 : 0);
            byte[] payload = fileReader.readChunk();
            this.sender.sendDatagram(seq, eof, payload, this.destIP, this.destPort);
            try { TimeUnit.MILLISECONDS.sleep(10);} catch (InterruptedException e) {}
        }
        this.sender.close();
        
    }
}
