import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * Michael Michaelides s1447836
 * Class representing a socket sending packets on a link
 */
public class Sender {

    private DatagramSocket socket;

    public Sender() throws SocketException, UnknownHostException {
        this(new DatagramSocket());
    }

    // extra constructor in case the programmer needs specific
    // configurations on the socket (rdt3.0)
    public Sender(DatagramSocket socket) throws UnknownHostException {
        this.socket = socket;
    }

    public void sendDatagram(byte[] seq, byte eof, byte[] payload,
            InetAddress destIP, int destPort) throws IOException,
            PortUnreachableException {
        byte[] encData = Encapsulator.encapsulate(seq, eof, payload);
        DatagramPacket packet = new DatagramPacket(encData, encData.length,
                destIP, destPort);
        this.socket.send(packet);
    }

    public void sendACK(byte[] seq, InetAddress destIP, int destPort)
            throws IOException {
        // ACK packet only has sequence number field so no need to encapsulate
        DatagramPacket packet = new DatagramPacket(seq, seq.length, destIP,
                destPort);
        this.socket.send(packet);
    }

    public void close() {
        this.socket.close();
    }
}
