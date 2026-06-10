import http.server
import socketserver
import os

PORT = 8000

os.chdir("C:\\Users\\20245\\WeChatProjects\\psychology-final\\admin-web")

Handler = http.server.SimpleHTTPRequestHandler

try:
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        print(f"前端服务器已启动: http://localhost:{PORT}/")
        print("按 Ctrl+C 停止服务器")
        httpd.serve_forever()
except Exception as e:
    print(f"启动失败: {e}")
    input("按回车键退出...")