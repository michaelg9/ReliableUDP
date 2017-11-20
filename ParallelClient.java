import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;

public abstract class ParallelClient {
	protected DatagramChannel channel;
	protected int base = 1;
	protected SequenceNumber nextSeq = new SequenceNumber(1);
	private int chunkSize = 1024;
	private int windowSize;

	public ParallelClient(String remoteHost, int remotePort, int timeout, int windowSize ) throws IOException {
		channel = DatagramChannel.open();
		channel.configureBlocking(false);
		channel.setOption(StandardSocketOptions.SO_SNDBUF,new Integer(windowSize * 2));
		channel.setOption(StandardSocketOptions.SO_SNDBUF, new Integer(chunkSize));
		channel.connect(new InetSocketAddress(remoteHost, remotePort));
		this.windowSize = windowSize;
	}
	
	public void sendFile(String filename) throws IOException {
		BinaryFileReader fileReader = new BinaryFileReader(filename, chunkSize);
		int numberOfChunks = fileReader.getNumberOfChunks();
		int lastRetries = 0;
		while(base <= numberOfChunks && lastRetries<windowSize+10) {
			if (nextSeq.toInt() < base + windowSize && nextSeq.toInt()<=numberOfChunks) {
				byte eof = (byte) (nextSeq.toInt() == numberOfChunks ? 1 : 0);
				byte[] payload = fileReader.readChunk();
				sendNextPacket(payload, eof);
			}
            receive();
            checkTimeout();
            if (nextSeq.toInt() == numberOfChunks+1) lastRetries++;
		}
		System.out.println("eof");
	}
	
	protected abstract void checkTimeout() throws IOException;
	
	protected void receive() throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(chunkSize * 2);
		int bytesRcvd = channel.read(buf);
		buf.flip();
		while (bytesRcvd >= 2) {
			int ack = new SequenceNumber(new byte[]{buf.get(), buf.get()}).toInt();
			System.out.print("rcvd ack: "+ack);
			if (ack >= base && ack < base+windowSize) {
				System.out.println(". Accepted");
				onReceiveAck(ack);
			} else {
				System.out.println(". Discarded(out of window).");
			}
			bytesRcvd -= 2;
		}
	}

	protected abstract void onReceiveAck(int ack);

	protected void sendNextPacket(byte[] payload, byte eof) throws IOException {
		assert payload.length <= 1024;
		ByteBuffer buf = ByteBuffer.allocate(payload.length+3).put(nextSeq.toBytes()).put(eof).put(payload);
		buf.flip();
		
		onCreateNewPkt(buf);
		System.out.println("pkt seq "+nextSeq.toInt()+" sending("+buf.capacity()+")...");
		sendPacket(buf);
        nextSeq.increment();
        }
	
	protected abstract void onCreateNewPkt(ByteBuffer buf);

	protected void sendPacket(ByteBuffer buf) throws IOException {
		//////////REMOVE
		Random r = new Random();
		if (r.nextInt(100) < 30) {
			System.out.println("LOST");
			return;
		}
		
		int bytesSend = channel.write(buf);
        assert bytesSend == buf.capacity();
	}
}


