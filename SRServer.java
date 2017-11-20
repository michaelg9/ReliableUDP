import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class SRServer {
	private Sender sender;
	private Receiver receiver;
	private SequenceNumber base = new SequenceNumber(1);
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
		byte eof = 0;
		do {
			DatagramPacket packet = this.receiver.receiveData();
			byte[] dataRcved = packet.getData();
			SequenceNumber rcvSeq = new SequenceNumber(Deencapsulator.getSeqNo(dataRcved));
			System.out.println("rcvd (" + packet.getLength() + "): "+ rcvSeq.toIntString());
			if (isInPreviousWindow(rcvSeq) || isInCurrentWindow(rcvSeq)) {
				System.out.println("\tAcking it");
				this.sender.sendACK(rcvSeq.toBytes(), packet.getAddress(),
						packet.getPort());
			}
			if (isInCurrentWindow(rcvSeq) && isFirstArrivalOf(rcvSeq)) {
				System.out.println("\tFirst arrival! Buffering");
				eof = Deencapsulator.getEof(dataRcved);
				byte[] pkt = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength());
				window[rcvSeq.toInt() % window.length] = pkt;
				while (isInOrder()) {
					out.writeBuffer(window[base.toInt() % window.length], 3, eof == 1);
					System.out.print("\tDelivering " + base.toInt());
					base.increment();
					System.out.println(". Base': " + base.toInt());
				}
			}
		} while (eof != 1);
		System.out.println("Received eof!");
		this.cleanup();
	}

	private boolean isInOrder() {
		byte[] basePkt = window[base.toInt() % window.length];
		if (basePkt == null) return false; 
		SequenceNumber s = new SequenceNumber(Deencapsulator.getSeqNo(basePkt));
		return s.equals(base.toBytes());
	}

	private boolean isFirstArrivalOf(SequenceNumber s) {
		byte[] pkt = window[s.toInt() % window.length];
		if (pkt == null) return true;
		int curSeq = new SequenceNumber(Deencapsulator.getSeqNo(pkt)).toInt();
		System.out.println(curSeq + " stored VS rcvd:"+ s.toInt());
		assert s.toInt() >= curSeq;
		assert (s.toInt() % window.length) == (curSeq & window.length);
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
