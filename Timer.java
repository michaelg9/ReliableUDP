import java.util.concurrent.TimeUnit;


public class Timer {
	private Long start;
	private int timeout;
	private int seq = -1;
	
	public Timer(int timeout, int seq) {
		this(timeout);
		this.seq = seq;
	}
	
	public Timer(int timeout) {
		this.timeout = timeout;
	}
	
	public void start() {
		start = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
	}
	
	public void stop() {
		start = null;
	}
	
	public boolean isRunning() {
		return start != null;
	}
	
	public int getSeq() {
		assert seq != -1;
		return seq;
	}
	
	public boolean isExpired() {
		if (!isRunning()) return false;
		return  (TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) - start) >= timeout;
	}
}
