import java.nio.ByteBuffer;

/*
 * Michael Michaelides s1447836
 * Class representing a packet with its own timer.
 * Used in the selective repeat algorithm.
 */

public class SRPacket {
	public final Timer timer;
	public final ByteBuffer pkt;
	
	public SRPacket(Timer t, ByteBuffer buf) {
		timer = t;
		pkt = buf;
	}

}
