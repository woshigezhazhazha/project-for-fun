package signin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private Socket socket;
	private ServerSocket serverSocket;
	private int port=6000;
	
	public void startServer(){
		try{
			if(serverSocket==null){
				serverSocket=new ServerSocket(port);
			}
			System.out.println("the server is open now...");
			while(true){
				socket=serverSocket.accept();
				SocketThread socketThread=new SocketThread(socket);
				Thread thread=new Thread(socketThread);
				thread.start();
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
