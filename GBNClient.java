import java.io.IOException;
import java.nio.ByteBuffer;

/*
 * Michael Michaelides s1447836
 * Class representing a client sending data and receiving control packets
 * on an unreliable channel. The algorithm implemented is go back n.
 */

public class GBNClient extends ParallelClient {
	private ByteBuffer[] window;
	private Timer timer;

	public GBNClient(String remoteHost, int remotePort, 
			int timeout, int windowSize) throws IOException {
		super(remoteHost, remotePort, timeout, windowSize);
		window = new ByteBuffer[windowSize];
		timer = new Timer(timeout);	
	}

	//checks and handles timeouts. Returns if base has timed out.
	@Override
	protected boolean checkTimeout() throws IOException {
		boolean isBaseTimedOut = false;
		if (timer.isExpired()) {
			System.out.println("Timeout! Resending: ");
			timer.start();
			isBaseTimedOut = true;
			// send all packets from base to nextSeq-1
			for (int i = base; i < nextSeq.toInt() ; i++) {
				int index = (i) % window.length;
				window[index].rewind();
				System.out.println("\tSeq: "+window[index].get() + " " +window[index].get());
				window[index].rewind();
				sendPacket(window[index]);
			}
		}
		return isBaseTimedOut;
	}
	
	//handles ack in current window. Returns if base has been acked
	@Override
	protected boolean onReceiveValidAck(int ack) {
		//received cumulative ack in current window, update base
		base = ack + 1;
		if (base == nextSeq.toInt()) {
			//if no pending acks, stop timer
			timer.stop();
		} else {
			// pending acks, restart timer
			timer.start();
		}
		return true;
	}

	@Override
	protected void onCreateNewPkt(ByteBuffer buf) {
		window[nextSeq.toInt() % window.length] = buf;
		if (base == nextSeq.toInt()) {
			System.out.println("Sending first of the window, start timer");
			//first packet in the window sent, start timer
	  		timer.start();
	  	}		
	}
}
