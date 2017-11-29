import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
//import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/*
 * Michael Michaelides s1447836
 * Abstract skeleton class representing a client sending data and receiving control packets
 * on an unreliable channel. It can be extended by both go back n and selective repeat
 * clients. It is single threaded.
 */

public abstract class ParallelClient {
	protected DatagramChannel channel;
	protected int base = 1;
	protected SequenceNumber nextSeq = new SequenceNumber(1);
	private int chunkSize = 1024;
	private int windowSize;
	private SocketAddress dest;
	// counts number of times a specific base sequence has timed out
	// used to avoid lost last ack problem 
	private int baseRetx = 0;
    private long startTime;
    private long stopTime;
	
	public ParallelClient(String remoteHost, int remotePort, int timeout, int windowSize ) throws IOException {
		channel = DatagramChannel.open();
		channel.configureBlocking(false);
//		channel.setOption(StandardSocketOptions.SO_SNDBUF,new Integer(windowSize * 2));
//		channel.setOption(StandardSocketOptions.SO_SNDBUF, new Integer(chunkSize));
		dest = new InetSocketAddress(remoteHost, remotePort);
		this.windowSize = windowSize;
	}
	
	public void sendFile(String filename) throws IOException {
		BinaryFileReader fileReader = new BinaryFileReader(filename, chunkSize);
		int numberOfChunks = fileReader.getNumberOfChunks();
		// while not all are acked and the base hasn't timed out
		// more than 30 times, keep going 
		startTime = System.currentTimeMillis();
		while(base <= numberOfChunks && baseRetx < 30) {
			boolean hasMoreToSend = nextSeq.toInt()<=numberOfChunks;
			if (nextSeq.toInt() < base + windowSize && hasMoreToSend) {
				byte eof = (byte) (nextSeq.toInt() == numberOfChunks ? 1 : 0);
				byte[] payload = fileReader.readChunk();
				sendNextPacket(payload, eof);
			}
			//receive acks
            boolean isBaseAcked = receive();
            if (isBaseAcked) baseRetx = 0;
            //check for time outs
            boolean isBaseTimedOut = checkTimeout();
            if (isBaseTimedOut && !hasMoreToSend) {
            	// if everything has already been sent once and the
            	// same base seq no keeps timing out, increase counter
            	baseRetx++;
//            	System.out.println("BaseRetx: "+baseRetx+ " "+ isBaseTimedOut);
            }
		}
		float throughput = (float) ((fileReader.getLength()/1024.0)/
        ((this.stopTime-this.startTime)/1000.0));
		System.out.print(throughput);
	}
	
	//checks and handles time outs
	protected abstract boolean checkTimeout() throws IOException;
	
	//receives and handles acks. Returns if base has been acked
	protected boolean receive() throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(chunkSize * 2);
		channel.receive(buf);
		buf.flip();
		int bytesRcvd = buf.limit();
		boolean isBaseAcked = false;
		// investigate all acks received (2 bytes each)
		while (bytesRcvd >= 2) {
			int ack = new SequenceNumber(new byte[]{buf.get(), buf.get()}).toInt();
//			System.out.print("rcvd ack: "+ack);
			if (ack >= base && ack < base+windowSize) {
				// rcv ack is in current window. Accept it
//				System.out.println(". Accepted");
				isBaseAcked = onReceiveValidAck(ack);
				stopTime = System.currentTimeMillis();
			} else {
//				System.out.println(". Discarded(out of window).");
			}
			bytesRcvd -= 2;
		}
		return isBaseAcked;
	}

	protected abstract boolean onReceiveValidAck(int ack);
	
	//sends next packet in the window
	protected void sendNextPacket(byte[] payload, byte eof) throws IOException {
		assert payload.length <= 1024;
		ByteBuffer buf = ByteBuffer.allocate(payload.length+3)
				.put(nextSeq.toBytes()).put(eof).put(payload);
		buf.flip();
		onCreateNewPkt(buf);
//		System.out.println("pkt seq "+nextSeq.toInt()+" sending("+buf.capacity()+")...");
		sendPacket(buf);
        nextSeq.increment();
        }
	
	//caches sent packet for retransmissions
	protected abstract void onCreateNewPkt(ByteBuffer buf);
	
	protected void resendPacket(ByteBuffer buf) throws IOException {
		buf.rewind();
		sendPacket(buf);
	}

	protected void sendPacket(ByteBuffer buf) throws IOException {
		assert buf.limit() > 0;
		int bytesSend = channel.send(buf, dest);
        assert bytesSend == buf.limit(): bytesSend +" sent but had "+buf.limit();
	}
}

