import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/*
 * Michael Michaelides s1447836
 * Class used to write a binary file to disk.
 */
public class BinaryFileWriter {
    private FileOutputStream fis;

    public BinaryFileWriter(String filePath) throws FileNotFoundException {
        this.fis = new FileOutputStream(new File(filePath));
    }

    public void writeBuffer(byte[] buffer, boolean finished) throws IOException {
    	writeBuffer(buffer, 0, finished);
    }
    
    public void writeBuffer(byte[] buffer, int start, boolean finished) throws IOException {
        this.fis.write(buffer, start, buffer.length-start);
        if (finished) this.fis.close();
    }
}
