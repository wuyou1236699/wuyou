import java.io.*;
import java.net.*;

public class SimpleHttpServer {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(8000);
        System.out.println("前端服务器已启动: http://localhost:8000/");
        System.out.println("按 Ctrl+C 停止服务器");
        
        while (true) {
            Socket socket = server.accept();
            new Thread(new RequestHandler(socket)).start();
        }
    }
    
    static class RequestHandler implements Runnable {
        private Socket socket;
        
        public RequestHandler(Socket socket) {
            this.socket = socket;
        }
        
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream out = socket.getOutputStream();
                
                String requestLine = in.readLine();
                if (requestLine == null) return;
                
                String[] parts = requestLine.split(" ");
                String path = parts[1];
                
                if (path.equals("/")) {
                    path = "/index.html";
                }
                
                File file = new File("C:\\Users\\20245\\WeChatProjects\\psychology-final\\admin-web" + path);
                
                if (file.exists() && file.isFile()) {
                    byte[] content = readFile(file);
                    String contentType = getContentType(file.getName());
                    
                    out.write(("HTTP/1.1 200 OK\r\n").getBytes());
                    out.write(("Content-Type: " + contentType + "\r\n").getBytes());
                    out.write(("Content-Length: " + content.length + "\r\n").getBytes());
                    out.write("\r\n".getBytes());
                    out.write(content);
                } else {
                    out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                }
                
                out.flush();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private byte[] readFile(File file) throws IOException {
            FileInputStream fis = new FileInputStream(file);
            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            fis.close();
            return content;
        }
        
        private String getContentType(String fileName) {
            if (fileName.endsWith(".html")) return "text/html";
            if (fileName.endsWith(".css")) return "text/css";
            if (fileName.endsWith(".js")) return "application/javascript";
            if (fileName.endsWith(".png")) return "image/png";
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
            return "text/plain";
        }
    }
}