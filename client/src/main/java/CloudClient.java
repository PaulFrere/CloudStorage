import common.FSWorker;
import messages.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class CloudClient {

    private FSWorker fsWorker;

    private List<String> filesList;
    private String rootDir;
    String address;
    int port;

    public CloudClient(String address, int port) {
        this.address = address;
        this.port = port;
        rootDir = "С:\\ClientStorage\\";
        fsWorker = new FSWorker(rootDir);
    }

    public void connect() {
        NetworkClient.getInstance().connect(address, port);
    }


    public void startReadingThread(ControllerFX controllerFX) {
        Thread thread = new Thread(() -> {

            while (NetworkClient.getInstance().isConnected()) {
                Object msg = NetworkClient.getInstance().readObject();

                if (msg != null) {
                    System.out.println("Получено одно сообщение " + msg.toString());

                    if (msg instanceof AbstractMsg) {
                        AbstractMsg incomingMsg = (AbstractMsg) msg;
                        if (incomingMsg instanceof CommandMsg) {

                            CommandMsg cmdMsg = (CommandMsg) incomingMsg;

                            if (cmdMsg.getCommand() == CommandMsg.AUTH_OK) {
                                System.out.println("AUTHOK");
                                controllerFX.loginOk();
                            } else if (cmdMsg.getCommand() == CommandMsg.CREATE_DIR) {
                                System.out.println("CREATE_DIR");
                                createDirectory(cmdMsg);
                            }
                        }

                        if (incomingMsg instanceof FileListMsg) {
                            filesList = ((FileListMsg) incomingMsg).getFileList();
                            controllerFX.setCloudFilesList(filesList);
                        }

                        if (incomingMsg instanceof FileTransferMsg) {
                            saveFileToLocalStorage((FileTransferMsg) incomingMsg);
                        }
                    }
                }
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    private void createDirectory(CommandMsg cmdMsg) {

        Path rootPath = Paths.get(rootDir + "\\");
        Object inObj1 = cmdMsg.getObject()[0];

        if (inObj1 instanceof String) {
            Path tempPath1 = Paths.get((String) inObj1);
            Path newPath = Paths.get(rootPath.toString(), "\\",
                    tempPath1.subpath(1, tempPath1.getNameCount()).toString());
            fsWorker.mkDir(newPath);
        }
    }

    private void saveFileToLocalStorage(FileTransferMsg fileMsg) {
        Path newFilePath = Paths.get(rootDir +
                fileMsg.getFileName());
        fsWorker.mkFile(newFilePath, fileMsg.getData());
    }

    public List<String> getCloudFilesList() {
        return filesList;
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String newRootDir) {
        rootDir = newRootDir;
    }

    public List<String> getLocalFilesList() {
        return fsWorker.listDir(Paths.get(rootDir));
    }

    public void uploadFileOrFolder(String itemName) {
        Path path = Paths.get(getRootDir(), itemName);
        if (Files.isDirectory(path)) {
            try {
                sendFolder(path);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Ошибка при отправке директории!" + itemName);
            }
        } else {
            try {
                uploadFile(path);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Ошибка при отправке файла!" + itemName);
            }
        }
    }

    private void sendFolder(Path folderPath) throws IOException {
        Files.walkFileTree(folderPath, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                System.out.println("Обнаружили директорию" + dir.toString());
                NetworkClient.getInstance()
                        .sendObject(new CommandMsg(
                                CommandMsg.CREATE_DIR,
                                dir.toString(), dir.getParent().toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("Отправляем файл " + file);
                uploadFile(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void uploadFile(Path filePath) throws IOException {
        System.out.println("Send file - " + filePath.getFileName().toString());
        NetworkClient.getInstance()
                .sendObject(new FileTransferMsg(filePath));
    }

    public void sendFileToStorage(String fileName) {
        Path filePath = Paths.get(getRootDir(), fileName);
        try {
            NetworkClient.getInstance()
                    .sendObject(new FileTransferMsg(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(String fileName) {
        NetworkClient.getInstance()
                .sendObject(new CommandMsg(CommandMsg.DOWNLOAD_FILE, fileName));
    }

    public void deleteCloudFsObj(String fileName) {
        NetworkClient.getInstance()
                .sendObject(new CommandMsg(CommandMsg.DELETE, fileName));
    }

    public void deleteLocalFile(String fileName) {
        Path newFilePath = Paths.get(getRootDir(), fileName);
        System.out.println(fileName);
        fsWorker.deleteFileFromStorage(newFilePath);
    }

    public void doAuth(String login, String pwd) {
        NetworkClient.getInstance()
                .sendObject(new AuthMsg(login, pwd));
    }

    public void listCloudFiles(String itemName) {
        CommandMsg cmd;

        if (itemName != null) {
            if (itemName.equals(".."))
                cmd = new CommandMsg(CommandMsg.LIST_FILES, "..");
            else
                cmd = (new CommandMsg(CommandMsg.LIST_FILES, itemName));
        } else
            cmd = (new CommandMsg(CommandMsg.LIST_FILES));

        NetworkClient.getInstance().sendObject(cmd);
    }
}
