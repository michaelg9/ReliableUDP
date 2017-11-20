import java.nio.ByteBuffer;


public class SRPacket {
	public final Timer timer;
	public final ByteBuffer pkt;
	
	public SRPacket(Timer t, ByteBuffer buf) {
		timer = t;
		pkt = buf;
	}

}
