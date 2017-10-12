import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

public class BinaryFileWriter {
	private File file;
	
	public BinaryFileWriter(String filePath) throws FileAlreadyExistsException {
		this.file = new File(filePath);
		if (file.exists()) {
			throw new FileAlreadyExistsException("File already exists, can't overwrite");
		}
	}
	
	public void write(byte[] contents) throws FileNotFoundException, IOException {
		try (FileOutputStream stream = new FileOutputStream(file)) {
			stream.write(contents);
		}
	}

}
