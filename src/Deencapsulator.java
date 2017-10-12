public class Deencapsulator {
	private byte[] seqNo;
	private byte eof;
	private byte[] data;
	private byte[] encapsulatedData;
	
	public Deencapsulator(byte[] encapsulatedData) {
		if (encapsulatedData.length < 2) {
			throw new IllegalArgumentException("Encapsulated data should be at least 2 bytes long. Yours: " + encapsulatedData.length);
		}
		this.encapsulatedData = encapsulatedData;
	}

	public byte[] getSeqNo() {
		if (this.seqNo == null) {
			this.seqNo = new byte[2];
			this.seqNo[0] = this.encapsulatedData[0];
			this.seqNo[1] = this.encapsulatedData[1];
		}
		return this.seqNo;
	}

	public byte getEof() {
		if (this.encapsulatedData.length <= 2 ) throw new NoSuchFieldError();
		this.eof = this.encapsulatedData[2];
		return eof;
	}

	public byte[] getData() {
		if (this.data == null) {
			this.data = new byte[this.encapsulatedData.length - 3];
			for (int i = 0; i < this.data.length; i++) {
				this.data[i] = this.encapsulatedData[i+3];
			}
		}
		return data;
	}
	
}