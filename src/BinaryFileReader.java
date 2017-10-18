import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/*
 * Michael Michaelides s1447836
 * Class used to read a file from disk.
 * 
 */

public class BinaryFileReader {
	private File file;
	private BufferedInputStream bis;
	private int chunkSize = 1024;

	public BinaryFileReader(String filePath, int chunkSize) throws FileNotFoundException {
		this.file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException("File not found");
		}
		this.bis = new BufferedInputStream(new FileInputStream(this.file));
		this.chunkSize = chunkSize;
	}
	
	/*returns the next chunk of size this.chunksize
	* if it's the last chunk, it can be less than that
	* or we we've reached the end of stream, it will be null
	*/
	public byte[] readChunk() throws IOException {
		byte[] chunk =  new byte[this.chunkSize];
		int bytesRead = this.bis.read(chunk);
		if (bytesRead < this.chunkSize) {
			//reached end of stream
			if (bytesRead == -1) chunk = null;
			else chunk = Arrays.copyOfRange(chunk, 0, bytesRead);
			this.bis.close();
		}
		return chunk;
	}

	public int getNumberOfChunks() {
		return (int) Math.ceil((double) file.length() / this.chunkSize);
	}

	public long getLength() {
		return file.length();
	}
}