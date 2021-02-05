package messages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class FileTransferMsg extends AbstractMsg {
    private String fileName;
    private String path;
    private byte[] data;

    public FileTransferMsg(Path filePaths) throws IOException {
        this.path = filePaths.toString();
        this.fileName = filePaths.getFileName().toString();
        this.data = Files.readAllBytes(filePaths);
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public String getPath() {
        return path;
    }

}
