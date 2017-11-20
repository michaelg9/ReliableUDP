import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class SRClient extends ParallelClient {
	private int timeout;
	private SRPacket[] window;
	private Queue<Timer> timers = new LinkedList<Timer>();
	
	public SRClient(String remoteHost, int remotePort, int timeout,
			int windowSize) throws IOException {
		super(remoteHost, remotePort, timeout, windowSize);
		window = new SRPacket[windowSize];
		this.timeout = timeout;
	}

	@Override
	protected void checkTimeout() throws IOException {
		assert timers.size() <= window.length;
		while (timers.size() > 0) {
			if (!timers.element().isRunning()) {
				//oldest timer belongs to already reveived pkt
				timers.remove();
			}
			else if (timers.element().isExpired()) {
				//oldest timer expired
				Timer t = timers.remove();
				t.start();
				System.out.println("\tTimeout for "+ t.getSeq()+". Resending...");
				timers.add(t);
				sendPacket(window[t.getSeq() % window.length].pkt);
			} else {
				//oldest timer still valid
				break;
			}
		}
		assert timers.size() == 0 || !timers.element().isExpired();
	}

	@Override
	protected void onReceiveAck(int ack) {
		window[ack % window.length].timer.stop();
		//update base
		while (base < nextSeq.toInt()) {
			if (window[base % window.length].timer.isRunning()) {
				//timer running indicates unacked packet, this is the new base
				break;
			} else {
				//acked packet, continue
				base++;
				System.out.println("Base: "+base);
			}
		}
		assert (window[base%window.length].timer.isRunning() || timers.size() == 0);
	}

	@Override
	protected void onCreateNewPkt(ByteBuffer buf) {
		Timer t = new Timer(timeout, nextSeq.toInt());
		SRPacket pkt = new SRPacket(t, buf);
		window[nextSeq.toInt() % window.length] = pkt;
		t.start();
		timers.add(t);
	}	
}


//
//public class SRClient {
//	private int timeout;
//	private SRPacket[] window;
//	private DatagramChannel channel;
//	private int base = 1;
//	private SequenceNumber nextSeq = new SequenceNumber(1);
//	private int chunkSize = 1024;
//	Queue<Timer> timers = new LinkedList<Timer>();
//
//	public SRClient(String remoteHost, int remotePort, int timeout, int windowSize ) throws IOException {
//		channel = DatagramChannel.open();
//		channel.configureBlocking(false);
//		channel.setOption(StandardSocketOptions.SO_SNDBUF,new Integer(windowSize * 2));
//		channel.setOption(StandardSocketOptions.SO_SNDBUF, new Integer(chunkSize));
//		channel.connect(new InetSocketAddress(remoteHost, remotePort));
//		window = new SRPacket[windowSize];
//		this.timeout = timeout;
//	}
//	
//	public void sendFile(String filename) throws IOException {
//		BinaryFileReader fileReader = new BinaryFileReader(filename, chunkSize);
//		int numberOfChunks = fileReader.getNumberOfChunks();
//		while(base <= numberOfChunks) {
//			if (nextSeq.toInt() < base + window.length && nextSeq.toInt()<=numberOfChunks) {
//				byte eof = (byte) (nextSeq.toInt() == numberOfChunks ? 1 : 0);
//				byte[] payload = fileReader.readChunk();
//				sendNextPacket(payload, eof);
//			}
//          receive();
//          checkTimeout();
//		}
//		System.out.println("eof");
//	}
//	
//	private void checkTimeout() throws IOException {
//		assert timers.size() <= window.length;
//		for (int i = base; i < nextSeq.toInt(); i++) {
//			if (!timers.element().isRunning()) {
//				timers.remove();
//			}
//			else if (timers.element().isExpired()) {
//				Timer t = timers.remove();
//				t.start();
//				sendPacket(window[t.getSeq() % window.length].pkt);
//				timers.add(t);
//			} else {
//				break;
//			}
//		}
//		assert timers.size() == 0 || !timers.element().isExpired();
//	}
//
//	private void receive() throws IOException {
//		ByteBuffer buf = ByteBuffer.allocate(chunkSize * 2);
//		int bytesRcvd = channel.read(buf);
//		buf.flip();
//		while (bytesRcvd >= 2) {
//			int ack = new SequenceNumber(new byte[]{buf.get(), buf.get()}).toInt();
//			System.out.print("rcvd ack: "+ack);
//			if (ack >= base && ack < base+window.length) {
//				System.out.println(". Accepted");
//				window[ack % window.length].timer.stop();
//				if (base == ack) {
//					updateBase();
//				}
//			} else {
//				System.out.println(". Discarded(out of window).");
//			}
//			bytesRcvd -= 2;
//		}
//	}
//
//	private void updateBase() {
//		int i = 0;
//		for (i = base + 1; i < nextSeq.toInt(); i++) {
//			if (window[i % window.length].timer.isRunning()) {
//				break;
//			}
//		}
//		base = i;
//		assert (window[base%window.length].timer.isRunning() || base == nextSeq.toInt());
//	}
//
//	private void sendNextPacket(byte[] payload, byte eof) throws IOException {
//		assert payload.length <= 1024;
//		ByteBuffer buf = ByteBuffer.allocate(payload.length+3).put(nextSeq.toBytes()).put(eof).put(payload);
//		buf.flip();
//		sendPacket(buf);
//
//		Timer t = new Timer(timeout, nextSeq.toInt());
//		SRPacket pkt = new SRPacket(t, buf);
//		window[nextSeq.toInt() % window.length] = pkt;
//		t.start();
//		timers.add(t);
//		
//		System.out.println("pkt seq "+nextSeq.toInt()+" sent("+buf.capacity()+")");
//      nextSeq.increment();
//	}
//	
//	private void sendPacket(ByteBuffer buf) throws IOException {
//		int bytesSend = channel.write(buf);
//      assert bytesSend == buf.capacity();
//	}
//}
//