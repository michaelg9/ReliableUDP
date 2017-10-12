import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BinaryFileReader {
	private File file;
	
	public BinaryFileReader(String filePath) throws FileNotFoundException {
		this.file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException("File not found");
		}
	}

	public byte[] readAll() throws IOException {
		byte[] payload = new byte[(int) file.length()];
		try (FileInputStream stream = new FileInputStream(file)) {
			stream.read(payload);
		}
		return payload;
	}
	
	public byte[][] readChunks(int size) throws IOException{
		int numberOfChunks = (int)Math.ceil((double)file.length()/size);
		byte[][] chunks = new byte[numberOfChunks][];
		byte[] data = this.readAll();
		for (int i = 0; i < numberOfChunks; i++) {
			int offset = i * size;
			int chunkLength = Math.min(size, data.length - offset);
			chunks[i] = new byte[chunkLength];
			for (int k = 0; k < chunkLength; k++) {
				chunks[i][k] = data [offset + k];
			}
		}
		return chunks;
	}
	
	public long getLength() {
		return file.length();
	}
}
