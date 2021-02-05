import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class NetworkClient {

    private static NetworkClient ourInstance = new NetworkClient();

    private Socket clientSocket;
    private ObjectDecoderInputStream dis;
    private ObjectEncoderOutputStream dos;

    private boolean isConnected;

    public static NetworkClient getInstance() {
        return ourInstance;
    }

    private NetworkClient() {
    }

    public boolean connect(String serverAddress, int serverPort) {
        try {
            clientSocket = new Socket(serverAddress, serverPort);
            dos = new ObjectEncoderOutputStream(clientSocket.getOutputStream());
            dis = new ObjectDecoderInputStream(clientSocket.getInputStream());
            isConnected = true;
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
        return isConnected;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void sendObject(Object outObject) {
        try {
            dos.writeObject(outObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object readObject() {
        Object incomingObj = null;
        try {
            incomingObj = dis.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return incomingObj;
    }

    public void close() {
        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        isConnected = false;
    }

}
