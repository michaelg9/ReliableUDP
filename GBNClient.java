import java.io.IOException;
import java.nio.ByteBuffer;

public class GBNClient extends ParallelClient {
	private ByteBuffer[] window;
	private Timer timer;

	public GBNClient(String remoteHost, int remotePort, int timeout,
			int windowSize) throws IOException {
		super(remoteHost, remotePort, timeout, windowSize);
		window = new ByteBuffer[windowSize];
		timer = new Timer(timeout);	}

	@Override
	protected void checkTimeout() throws IOException {
		if (timer.isExpired()) {
			System.out.println("Timeout! Resending: ");
			timer.start();
			for (int i = base; i < nextSeq.toInt() ; i++) {
				int index = (i) % window.length;
				window[index].rewind();
				System.out.println("\tSeq: "+window[index].get() + " " +window[index].get());
				window[index].rewind();
				sendPacket(window[index]);
			}
		}
	}

	@Override
	protected void onReceiveAck(int ack) {
		base = ack + 1;
		if (base == nextSeq.toInt()) {
			timer.stop();
		} else {
			timer.start();
		}	
	}

	@Override
	protected void onCreateNewPkt(ByteBuffer buf) {
		window[nextSeq.toInt() % window.length] = buf;
		if (base == nextSeq.toInt()) {
			System.out.println("Sending first of the window, start timer");
	  		timer.start();
	  	}		
	}
	
}

//
//public class GBNClient {
//	private ByteBuffer[] window;
//	private DatagramChannel channel;
//	private int base = 1;
//	private SequenceNumber nextSeq = new SequenceNumber(1);
//	private int chunkSize = 1024;
//	private Timer timer;
//
//	public GBNClient(String remoteHost, int remotePort, int timeout, int windowSize ) throws IOException {
//		channel = DatagramChannel.open();
//		channel.configureBlocking(false);
//		channel.setOption(StandardSocketOptions.SO_SNDBUF,new Integer(windowSize * 2));
//		channel.setOption(StandardSocketOptions.SO_SNDBUF, new Integer(chunkSize));
//		channel.connect(new InetSocketAddress(remoteHost, remotePort));
//		window = new ByteBuffer[windowSize];
//		timer = new Timer(timeout);
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
//            receive();
//            checkTimeout();
//		}
//		System.out.println("eof");
//	}
//	
//	private void checkTimeout() throws IOException {
//		if (timer.isExpired()) {
//			System.out.println("Timeout! Resending: ");
//			timer.start();
//			for (int i = base; i < nextSeq.toInt() ; i++) {
//				int index = (i) % window.length;
//				window[index].rewind();
//				System.out.println(index+ ": "+window[index].get() + " " +window[index].get()+ " " +window[index].get());
//				window[index].rewind();
//				sendPacket(window[index]);
//			}
//		}
//	}
//
//	private void receive() throws IOException {
//		ByteBuffer buf = ByteBuffer.allocate(chunkSize * 2);
//		int bytesRcvd = channel.read(buf);
//		buf.flip();
//		while (bytesRcvd >= 2) {
//			int ack = new SequenceNumber(new byte[]{buf.get(), buf.get()}).toInt();
//			System.out.print("rcvd ack: "+ack);
//			if (ack >= base) {
//				System.out.println(". Accepted");
//				base = ack + 1;
//				adjustTimer();
//			} else {
//				System.out.println(". Duplicate.");
//			}
//			bytesRcvd -= 2;
//		}
//	}
//	
//	private void adjustTimer() {
//		if (base == nextSeq.toInt()) {
//			System.out.println("stopping timer");
//			timer.stop();
//		} else {
//			System.out.println("restarting timer");
//			timer.start();
//		}
//	}
//
//	private void sendNextPacket(byte[] payload, byte eof) throws IOException {
//		assert payload.length <= 1024;
//		ByteBuffer buf = ByteBuffer.allocate(payload.length+3).put(nextSeq.toBytes()).put(eof).put(payload);
//		buf.flip();
//		sendPacket(buf);
//
//		window[nextSeq.toInt() % window.length] = buf;
//        if (base == nextSeq.toInt()) {
//        	System.out.println("\tfirst of the window, start timer");
//        	timer.start();
//        }
//        
//		System.out.println("pkt seq "+nextSeq.toInt()+" sent("+buf.capacity()+")");
//        nextSeq.increment();
//	}
//	
//	private void sendPacket(ByteBuffer buf) throws IOException {
//		int bytesSend = channel.write(buf);
//        assert bytesSend == buf.capacity();
//	}
//}
//
//
