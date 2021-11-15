import java.io.*;
import java.net.*;
import java.nio.channels.ServerSocketChannel;

public class UploadServer extends Thread {

    public static int BUFFER_SIZE = 1024;
    public static int SUCCESS = 0;
    public static int FAILURE = 1;

    private Socket socket;
    private DataInputStream sin;
    private DataOutputStream sout;

    public UploadServer(Socket socket) throws IOException {
        this.socket = socket;
        this.sin = new DataInputStream(socket.getInputStream());
        this.sout = new DataOutputStream(socket.getOutputStream());
    }

    public void run() {
        try {
            String fileName = this.sin.readUTF();
            File file = new File(fileName);
            if (file.exists()) {
                this.sout.writeInt(FAILURE);
            } else {
                this.sout.writeInt(SUCCESS);
            }
            this.sout.flush();
            byte[] buffer = new byte[BUFFER_SIZE];
            OutputStream fout = new BufferedOutputStream(new FileOutputStream(file));
            long length = this.sin.readLong();

            while (length != 0) {
                int readSize = (int) Math.min(length, BUFFER_SIZE);
                this.sin.readFully(buffer, 0, readSize);
                fout.write(buffer, 0, readSize);
                length -= readSize;
            }
            fout.close();
            this.sout.writeInt(0);
            this.sout.flush();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                this.sout.writeInt(FAILURE);
                this.sout.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                this.sin.close();
                this.sout.close();
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 8000;
        ServerSocket ss = new ServerSocket(port);
        while (true) {
            Socket socket = ss.accept();
            UploadServer upload = new UploadServer(socket);
            upload.start();
        }
    }
}
