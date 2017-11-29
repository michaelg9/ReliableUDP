import java.util.concurrent.TimeUnit;

/*
 * Michael Michaelides s1447836
 * Class representing a timer in java.
 * I'm using the System.nanoTime() method to record
 * the amount of time the program has been running since the 
 * timer was started. 
 */

public class Timer {
	private Long start;
	private int timeout;
	//sequence number is only used in selective repeat packets.
	//a value of -1 means it's unused
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
	
	//running means it is active. It can be valid or expired.
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
