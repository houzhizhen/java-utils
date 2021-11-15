import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class UploadClient {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: server port file");
        }
        Socket socket = new Socket(args[0], Integer.valueOf(args[1]));
        try {
            File file = new File(args[2]);
            if (! file.exists()) {
                System.err.println("File " + file + " doesn't exist.");
                System.exit(1);
            }
            long length = file.length();
            DataInputStream fin = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            //构建IO
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(file.getName());
            out.flush();
            int fileExist = in.readInt();
            if (fileExist == 1) {
                System.out.println("File " + file.getName() + " exists on server.");
                socket.close();
                System.exit(1);
            }
            out.writeLong(length);
            byte[] buffer = new byte[UploadServer.BUFFER_SIZE];
            while (length != 0) {
                int readSize = (int) Math.min(length, UploadServer.BUFFER_SIZE);
                fin.readFully(buffer, 0, readSize);
                out.write(buffer, 0, readSize);
                length -= readSize;
            }
            out.flush();
            fin.close();
            int result = in.readInt();
            if (fileExist == 0) {
                System.out.println("File " + file.getName() + " upload success.");

                System.exit(0);
            } else {
                System.out.println("File " + file.getName() + " upload failure.");
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
