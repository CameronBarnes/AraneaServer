package com.cameronbarnes.AraneaServer.database;

import com.cameronbarnes.AraneaCore.database.DatabasePacket;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DatabaseClient extends Thread {
	
	private ConcurrentLinkedQueue<DatabasePacket> mDatabasePackets;
	private boolean running;
	
	private final String  mDBURI;
	private MongoClient mClient;
	
	public DatabaseClient(ConcurrentLinkedQueue<DatabasePacket> databasePackets, String uri) {
		mDatabasePackets = databasePackets;
		mDBURI = uri;
	}
	
	@Override
	public void run() {
		
		running = true;
		try {
			init();
			loop();
		}
		finally {
			cleanup();
		}
		
	}
	
	private void init() {
		
		mClient = MongoClients.create(mDBURI);
	
	}
	
	private void loop() {
		
		while (running) {
			
			if (!mDatabasePackets.isEmpty()) {
				
				processRequest(mDatabasePackets.poll());
				
			}
			else {
				try {
					mDatabasePackets.wait();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	private void processRequest(@NotNull DatabasePacket request) {
		
		DatabasePacket.PacketType type = request.getPacketType();
		
		switch (type) {
			
			case AddUser:
				break;
			case ProcessCredentials:
				break;
			case RequestUserData:
				break;
			case RequestDocument:
				break;
			case RequestIndex:
				break;
		}
	
	}
	
	public void stopThread() {
		running = false;
	}
	
	private void cleanup() {
	
	}
	
}
