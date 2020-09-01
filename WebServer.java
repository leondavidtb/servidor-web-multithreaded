import java.io.*;
import java.net.*;
import java.util.*;

public final class WebServer {
	public static void main(String[] args) throws Exception {
		System.out.println("\nInicializando main");
		int port = 7813;
		
		ServerSocket server = new ServerSocket(port);
		
		System.out.println("\nEsperando conexão...");
		while (true) {
			Socket client = server.accept();
			System.out.println("\nConexão requisitada, enviando para tratamento...");
			HttpRequest request = new HttpRequest(client);
			Thread thread = new Thread(request);
			System.out.println("\nSocket aceito, inicializando thread");
			thread.start();
		}
	}
}

final class HttpRequest implements Runnable {
	final static String CRLF = "\r\n";
	Socket socket;
	
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("\nIniciando processamento do Request");
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void processRequest() throws Exception {
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		String requestLine = br.readLine();
		String headerLine = null;
		
		System.out.println("\nProcessando Request...");
		System.out.println("\nDataOutputStream: " + os + " BufferedReader: " + br + " Request Line: " + requestLine);
		System.out.println("\nHeaders:");
		System.out.println("=========================================================");
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}
		System.out.println("=========================================================\n");
		StringTokenizer tokens = new StringTokenizer(requestLine);
		
		tokens.nextToken();
		
		String fileFromPath = tokens.nextToken();
		fileFromPath = "." + fileFromPath;
		System.out.println("\nCaminho do arquivo: " + fileFromPath);
		
		FileInputStream fis = null;
		boolean fileExists = true;
		
		try {
			System.out.println("\nTentando criar FIS...");
			fis = new FileInputStream(fileFromPath);
			System.out.println("\nCriando File Input Stream:" + fis);
		} catch (FileNotFoundException e) {
			fileExists = false;
			System.out.println(e);
		}
		
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		
		System.out.println("\nVerificando existência de arquivo...");
		if (fileExists) {
			System.out.println("\nArquivo existente, criando response 200");
			statusLine = "HTTP/1.1 200 OK" + CRLF;
			contentTypeLine = "Content-type:" + contentType(fileFromPath) + CRLF;
		} else {
			System.out.println("\nArquivo inexistente, criando response 404");
			statusLine = "HTTP/1.1 404 Not Found";
			contentTypeLine = "Content-type:" + contentType(fileFromPath) + CRLF;
			entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + "<BODY>Not Found</BODY></BODY>";
		}
		
		System.out.println("\nEnviando response...");
		os.writeBytes(statusLine);
		os.writeBytes(contentTypeLine);
		os.writeBytes(CRLF);
		
		if (fileExists) {
			sendBytes(fis, os);
			fis.close();
		} else {
			os.writeBytes(entityBody);
		}
		
		System.out.println("\nResponse enviada com sucesso!");
		System.out.println("\nPreparando para finalizar comunicação...");
		
		os.close();
		br.close();
		socket.close();
		
		System.out.println("\nComunicação finalizada com sucesso!");
	}

	private void sendBytes(FileInputStream fis, DataOutputStream os) throws IOException {
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		while ((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
		
	}

	private static String contentType(String fileName) {
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			System.out.println("\n(Arquivo a ser enviado é um HTML)");
			return "text/html";
		}
		
		if (fileName.endsWith(".css")) {
            return "text/css";
        }
		
		if (fileName.endsWith(".png")) {
            return "text/png";
        }
		return "application/octet-stream";
	}
}























