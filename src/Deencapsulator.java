import java.util.Arrays;

/*
 * Michael Michaelides s1447836
 * Class that takes the data out of of DatagramPacket
 * and extracts sequence number, eof flag and payload
 */

public class Deencapsulator {

	// retrieves sequence number from received packet. Should always exist
	public static byte[] getSeqNo(byte[] data) {
		if (data.length < 2)
			throw new IllegalArgumentException(
					"Received data length is shorter than expected");
		return Arrays.copyOfRange(data, 0, 2);
	}

	// retrieves eof flag from received packet. Only for data packets
	public static byte getEof(byte[] data) throws NoSuchFieldException {
		if (data.length <= 2)
			throw new NoSuchFieldException("No byte field");
		return data[2];
	}

	// retrieves contents from received packet. Only for data packets
	public static byte[] getData(byte[] data) throws NoSuchFieldException {
		if (data.length <= 2)
			throw new NoSuchFieldException("No data field");
		return Arrays.copyOfRange(data, 3, data.length);

	}

}
