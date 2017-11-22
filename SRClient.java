import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/*
 * Michael Michaelides s1447836
 * Class representing a client sending data and receiving control packets
 * on an unreliable channel. The algorithm implemented is selective repeat.
 * Each packet is assigned a timer and all of these timers are also stored in
 * a queue, according to their start time. If first timer isn't expired, none is.
 */

public class SRClient extends ParallelClient {
	private int timeout;
	//keeps packet with its timer
	private SRPacket[] window;
	//keeps all timers in FIFO manner
	private Queue<Timer> timers = new LinkedList<Timer>();
	
	public SRClient(String remoteHost, int remotePort, 
			int timeout, int windowSize) throws IOException {
		super(remoteHost, remotePort, timeout, windowSize);
		window = new SRPacket[windowSize];
		this.timeout = timeout;
	}

	//checks and handles timeouts. Returns if base has timed out.
	@Override
	protected boolean checkTimeout() throws IOException {
		boolean isBaseTimedOut = false;
		while (timers.size() > 0) {
			if (!timers.element().isRunning()) {
				//oldest timer belongs to already received pkt
				timers.remove();
			}
			else if (timers.element().isExpired()) {
				//oldest timer expired
				Timer t = timers.remove();
				t.start();
				if (t.getSeq() == base) isBaseTimedOut = true;
//				System.out.println("\tTimeout for "+ t.getSeq()+". Resending...");
				//put restarted timer back in queue and send again
				timers.add(t);
				resendPacket(window[t.getSeq() % window.length].pkt);
			} else {
				//oldest timer still valid
				assert timers.size() == 0 || timers.element().isRunning();
				break;
			}
		}
		return isBaseTimedOut;
	}

	//handles ack in current window. Returns if base has been acked
	@Override
	protected boolean onReceiveValidAck(int ack) {
		window[ack % window.length].timer.stop();
		boolean isBaseAcked = false;
		//base could go up to nextSeq
		while (base < nextSeq.toInt()) {
			if (window[base % window.length].timer.isRunning()) {
				//timer running indicates unacked packet, this is the new base
				break;
			} else {
				//acked packet, increase base
				base++;
				isBaseAcked = true;
//				System.out.println("Base: "+base);
			}
		}
		assert (nextSeq.toInt()==base || window[base%window.length].timer.isRunning());
		return isBaseAcked;
	}

	//buffers newly sent packets for retransmission
	@Override
	protected void onCreateNewPkt(ByteBuffer buf) {
		Timer t = new Timer(timeout, nextSeq.toInt());
		SRPacket pkt = new SRPacket(t, buf);
		window[nextSeq.toInt() % window.length] = pkt;
		t.start();
		timers.add(t);
	}	
}
