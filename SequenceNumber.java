import java.math.BigInteger;
import java.util.Arrays;

public class SequenceNumber {
	private int seq = 0;
	
	public SequenceNumber(int s){seq=s;}
	
	public SequenceNumber(byte[] l) {
		seq = SequenceNumber.fromByteToInt(l);
	}

	public int increment() {
		seq++;
		return seq;
	}
	
	public int toInt() {
		return seq;
	}
	
	public String toIntString() {
		return Integer.toString(seq);
	}
	
	public String toBytesString() {
		return Arrays.toString(SequenceNumber.fromIntToByte(seq));
	}
	
	public byte[] toBytes() {
		return SequenceNumber.fromIntToByte(seq);
	}
	
	public boolean equals(byte[] l) {
		assert l.length == 2;
		return SequenceNumber.fromByteToInt(l) == this.seq;
	}
	
	private static int fromByteToInt(byte[] l){
		return new BigInteger(1, l).intValue();
	}
	
	private static byte[] fromIntToByte(int l){
		return new byte[]{(byte)((l >> 8) & 0xFF),(byte) (l & 0xFF)};
	}
}
