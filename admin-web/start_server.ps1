$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add('http://localhost:8000/')
$listener.Start()
Write-Host '前端服务器已启动: http://localhost:8000/'
Write-Host '按 Ctrl+C 停止服务器'

while ($listener.IsListening) {
    $context = $listener.GetContext()
    $request = $context.Request
    $response = $context.Response
    $url = $request.Url.LocalPath
    
    if ($url -eq '/') {
        $url = '/index.html'
    }
    
    $file = Join-Path -Path 'C:\Users\20245\WeChatProjects\psychology-final\admin-web' -ChildPath ($url -replace '/', '\')
    
    if (Test-Path $file) {
        $ext = [System.IO.Path]::GetExtension($file)
        $mimeType = 'text/html'
        
        if ($ext -eq '.css') { $mimeType = 'text/css' }
        elseif ($ext -eq '.js') { $mimeType = 'application/javascript' }
        elseif ($ext -eq '.png') { $mimeType = 'image/png' }
        elseif ($ext -eq '.jpg' -or $ext -eq '.jpeg') { $mimeType = 'image/jpeg' }
        
        $content = [System.IO.File]::ReadAllBytes($file)
        $response.ContentType = $mimeType
        $response.ContentLength64 = $content.Length
        $response.OutputStream.Write($content, 0, $content.Length)
    } else {
        $response.StatusCode = 404
    }
    
    $response.Close()
}