/*
 * Michael Michaelides s1447836
 * Static class that joins multiple byte arrays into one
 * Used to encapsulate headers with data before sending
 */

public class Encapsulator {

    // combines the three headers into a single data field ready to be
    // transmitted
    public static byte[] encapsulate(byte[] seqNo, byte eof, byte[] data) {
        int totalLength = seqNo.length + 1 + data.length;
        byte[] encapsulatedData = new byte[totalLength];
        encapsulatedData = new byte[totalLength];
        encapsulatedData[0] = seqNo[0];
        encapsulatedData[1] = seqNo[1];
        encapsulatedData[2] = eof;
        for (int i = 3; i < encapsulatedData.length; i++) {
            encapsulatedData[i] = data[i - 3];
        }
        return encapsulatedData;
    }
}
