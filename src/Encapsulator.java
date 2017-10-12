public class Encapsulator {
	private byte[] seqNo;
	private byte eof;
	private byte[] data;
	private byte[] encapsulatedData;
	
	public Encapsulator(byte[] seqNo, byte eof, byte[] data) {
		if (seqNo.length != 2) {
			throw new IllegalArgumentException("Sequence number is 2 bits long");
		}
		if (data.length < 0 || data.length > 1023) {
			throw new IllegalArgumentException("Data should be between [0, 1023] bits long. Yours: "+ data.length);
		}
		this.seqNo = seqNo;
		this.eof = eof;
		this.data = data;
	}
	
	private void encapsulate() {
		int totalLength = this.seqNo.length + 1 + this.data.length;
		this.encapsulatedData = new byte[totalLength];
		this.encapsulatedData[0] = this.seqNo[0];
		this.encapsulatedData[1] = this.seqNo[1];
		this.encapsulatedData[2] = this.eof;	
		for (int i=3; i < this.encapsulatedData.length; i++) {
			this.encapsulatedData[i] = this.data[i-3];
		}
	}

	public byte[] getEncapsulatedData() {
		if (encapsulatedData == null) this.encapsulate();
		return encapsulatedData;
	}
}
