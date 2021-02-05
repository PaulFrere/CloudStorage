import common.FSWorker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CloudServerHandler extends ChannelInboundHandlerAdapter {

    private String nick;
    private boolean isLogged;
    private String currentFolderPath;
    private Logger logger;
    private FSWorker fsWorker;

    public CloudServerHandler() {
        this.logger = LoggerFactory.getLogger(CloudServerHandler.class);
        fsWorker = new FSWorker();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        try {
            if (msg == null)
                return;

            if (msg instanceof AbstractMsg) {
                processMsg((AbstractMsg) msg, ctx);
            } else {
                System.out.println("Server received wrong object!");
                logger.debug("Server received wrong object!");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println(cause.getMessage());
    }

    private void processMsg(AbstractMsg msg, ChannelHandlerContext ctx) {
        System.out.println("username: " + nick);
        if (isLogged) {
            if (msg instanceof FileTransferMsg) {
                saveFileToStorage((FileTransferMsg) msg);
            } else if (msg instanceof CommandMsg) {
                System.out.println("Server received a command " +
                        ((CommandMsg) msg).getCommand());
                logger.debug("Server received a command", msg);
                processCommand((CommandMsg) msg, ctx);
            }
        } else {
            if (msg instanceof AuthMsg) {
                System.out.println("Nickname in CloudServerHandler" +
                        ((AuthMsg) msg).getNickname());
                checkAuth((AuthMsg) msg, ctx);
            }
        }
    }

    private void processCommand(CommandMsg msg, ChannelHandlerContext ctx) {
        switch (msg.getCommand()) {
            case CommandMsg.LIST_FILES:
                sendFileList(msg, ctx);
                break;
            case CommandMsg.DOWNLOAD_FILE:
                sendFile(msg, ctx);
                break;
            case CommandMsg.DELETE:
                deleteFileOrFolder(msg);
                break;
            case CommandMsg.CREATE_DIR:
                createDirectory(msg);
                break;
        }
    }

    private void sendFileList(CommandMsg msg, ChannelHandlerContext ctx) {
        sendData(new FileListMsg(getClientFilesList(msg.getObject()[0])), ctx);
    }

    private void sendFile(CommandMsg msg, ChannelHandlerContext ctx) {
        try {
            Path filePath = Paths.get(currentFolderPath + "\\",
                    (String) (msg.getObject()[0]));
            sendData(
                    new FileTransferMsg(filePath), ctx);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFileOrFolder(CommandMsg msg) {

        String folderName = currentFolderPath + msg.getObject()[0];
        fsWorker.delFsObject(folderName);

    }

    private void createDirectory(CommandMsg msg) {

        logger.debug("Попытка создать директорию.");

        Path rootPath = Paths.get(currentFolderPath + "\\");
        Object inObj1 = msg.getObject()[0];

        if (inObj1 instanceof String) {
            Path tempPath1 = Paths.get((String) inObj1);
            Path newPath = Paths.get(rootPath.toString(), "\\",
                    tempPath1.subpath(1, tempPath1.getNameCount()).toString());

            fsWorker.mkDir(newPath);
        }
    }


    private void saveFileToStorage(FileTransferMsg msg) {

        Path filePath = Paths.get(msg.getPath());

        String relPath = filePath.subpath(1, filePath.getNameCount()).toString();

        Path newFilePath = Paths.get(currentFolderPath + "\\" + relPath);

        fsWorker.mkFile(newFilePath, msg.getData());

    }

    private List<String> getClientFilesList(Object folderName) {
        List<String> fileList;
        Path listFolderPath = Paths.get(currentFolderPath);

        logger.debug("current folderName = " + folderName);
        logger.debug("current currentFolderPath = " + currentFolderPath);

        if (folderName != null) {
            if (folderName.equals("..")) {
                listFolderPath = Paths.get(currentFolderPath).getParent();
                currentFolderPath = listFolderPath.toString() + "\\";
            } else {
                currentFolderPath += folderName + "\\";
                listFolderPath = Paths.get(currentFolderPath);
            }
        }
        System.out.println("current currentFolderPath = " + currentFolderPath);
        fileList = fsWorker.listDir(listFolderPath);
        return fileList;
    }

    private void checkAuth(AuthMsg incomingMsg, ChannelHandlerContext ctx) {
        if (incomingMsg != null) {

            nick = incomingMsg.getNickname();
            if (nick != null) {

                String rootDir = "D:\\ServerStorage\\";
                currentFolderPath = rootDir + nick + "\\";

                System.out.println("Client Auth OK");
                isLogged = true;
                fsWorker.setRootDir(currentFolderPath);

                sendData(new CommandMsg(CommandMsg.AUTH_OK), ctx);

                logger.debug("Client Auth OK");
            } else {
                System.out.println("Client not found");
                isLogged = false;
                logger.debug("Client not found");
            }
        }
    }

    private void sendData(AbstractMsg msg, ChannelHandlerContext ctx) {
        ctx.writeAndFlush(msg);
    }


}
