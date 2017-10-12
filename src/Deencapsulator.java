public class Deencapsulator {
	private byte[] seqNo;
	private byte eof;
	private byte[] data;
	private byte[] encapsulatedData;
	
	public Deencapsulator(byte[] encapsulatedData) {
		if (encapsulatedData.length < 3) {
			throw new IllegalArgumentException("Encapsualted data should be longer than 2 bytes");
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