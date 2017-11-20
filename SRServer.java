import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

/*
 * Michael Michaelides s1447836
 * Class representing a receiver receiving data and sending control packets
 * on an unreliable channel. The algorithm implemented is selective repeat.
 */

public class SRServer {
	private Sender sender;
	private Receiver receiver;
	private SequenceNumber base = new SequenceNumber(1);
	//keeps all received unordered packets, with their headers
	private byte[][] window;

	public SRServer(int localPort, int windowSize) throws SocketException,
			UnknownHostException {
		DatagramSocket socket = new DatagramSocket(localPort);
		this.sender = new Sender(socket);
		this.receiver = new Receiver(socket);
		window = new byte[windowSize][];
	}

	public void receiveFile(String filepath) throws IOException, NoSuchFieldException {
		BinaryFileWriter out = new BinaryFileWriter(filepath);
		// will keep the sequence number of the last packet.
		// used to help determine when to terminate.
		SequenceNumber eofSeq = null;
		do {
			DatagramPacket packet = this.receiver.receiveData();
			byte[] dataRcved = packet.getData();
			SequenceNumber rcvSeq = new SequenceNumber(Deencapsulator.getSeqNo(dataRcved));
			System.out.println("rcvd (" + packet.getLength() + "): "+ rcvSeq.toIntString());
			if (isInPreviousWindow(rcvSeq) || isInCurrentWindow(rcvSeq)) {
				System.out.println("\tAcking it");
				this.sender.sendACK(rcvSeq.toBytes(), packet.getAddress(), packet.getPort());
			}
			if (isInCurrentWindow(rcvSeq) && isFirstArrivalOf(rcvSeq)) {
				System.out.println("\tFirst arrival! Buffering");
				boolean eof = Deencapsulator.getEof(dataRcved) == 1;
				if (eof) eofSeq = rcvSeq;
				// trim packet to its received length
				byte[] pkt = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength());
				// buffer it
				window[rcvSeq.toInt() % window.length] = pkt;
				while (isInOrder()) {
					//deliver in order packets and update base
					out.writeBuffer(window[base.toInt() % window.length], 3, eof);
					System.out.print("\tDelivering " + base.toInt());
					base.increment();
					System.out.println(". Base': " + base.toInt());
				}
			}
			//stop when last packet has arrived and has been acked
		} while (eofSeq == null || eofSeq.toInt() >= base.toInt());
		System.out.println("EOF!");
		this.cleanup();
	}

	//checks is base has arrived
	private boolean isInOrder() {
		byte[] basePkt = window[base.toInt() % window.length];
		if (basePkt == null) return false;
		//if the packet stored in the place where base should be
		//has base's seq number, then base has arrived. Otherwise
		//packet stored there belongs to previous window
		SequenceNumber s = new SequenceNumber(Deencapsulator.getSeqNo(basePkt));
		return s.equals(base.toBytes());
	}

	private boolean isFirstArrivalOf(SequenceNumber s) {
		byte[] pkt = window[s.toInt() % window.length];
		if (pkt == null) return true;
		int curSeq = new SequenceNumber(Deencapsulator.getSeqNo(pkt)).toInt();
		// if the packet stored where the received packet will be stored
		// has a smaller seq number, then it's the first time we rcv this packet
		assert s.toInt() >= curSeq;
		assert (s.toInt() % window.length) == (curSeq % window.length);
		return curSeq < s.toInt();
	}

	private boolean isInCurrentWindow(SequenceNumber s) {
		return s.toInt() >= base.toInt()
				&& s.toInt() < (base.toInt() + window.length);
	}

	private boolean isInPreviousWindow(SequenceNumber s) {
		return s.toInt() >= (base.toInt() - window.length)
				&& s.toInt() < base.toInt();
	}

	private void cleanup() {
		this.sender.close();
		this.receiver.close();
	}

}
