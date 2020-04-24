package com.cameronbarnes.AraneaServer.netowrking;

import com.cameronbarnes.AraneaServer.database.DatabaseHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server extends Thread implements ServerClientInterface {

	private ServerSocket mServerSocket;
	
	private DatabaseHandler mDatabaseHandler;
	
	private volatile boolean running = false;
	private Vector<ClientHandler> mClientHandlers;
	private int mNumClients = 0;
	
	private static Logger log = LogManager.getLogger(Server.class.getName());
	
	public Server(DatabaseHandler handler) {
		
		mDatabaseHandler = handler;
		
	}
	
	@Override
	public void run() {
	
		running = true;
		try {
			init();
			loop();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				cleanup();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	}
	
	public void stopThread() {
		running = false;
	}
	
	private void init() throws IOException {
		
		mServerSocket = new ServerSocket(443322);
		mClientHandlers = new Vector<>();
		mNumClients = 0;
		
	}
	
	private void loop() throws IOException {
		
		Socket socket;
		
		while (running) {
			
			socket =  mServerSocket.accept();
			log.info("New Client request received: " + socket);
			
			ClientHandler clientHandler =
					new ClientHandler(socket, "Client:" + mNumClients,
							mDatabaseHandler, this);
			
			Thread t = new Thread(clientHandler);
			log.info("Adding this client to active clients list");
			mClientHandlers.add(clientHandler);
			
			t.start();
			mNumClients++;
			
		}
	
	}
	
	public int getNumActiveClients() {
		return mClientHandlers.size();
	}
	
	private void cleanup() throws IOException {
		
		for (ClientHandler handler: mClientHandlers) {
			
			handler.stopThread();
			
		}
		
		mServerSocket.close();
		
	}
	
	/**
	 *
	 * @param client the client to remove from the active client list
	 *               This function does not stop the client thread, it only removes it from the server's active client list
	 */
	@Override
	public void removeClient(ClientHandler client) {
		
		mClientHandlers.remove(client);
		
	}
	
}
