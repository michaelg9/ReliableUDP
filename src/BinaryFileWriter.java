import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BinaryFileWriter {
	private File file;
	
	public BinaryFileWriter(String filePath) {
		this.file = new File(filePath);
	}
	
	public void writeAll(byte[] contents) throws FileNotFoundException, IOException {
		try (FileOutputStream stream = new FileOutputStream(file)) {
			stream.write(contents);
		}
	}

}
