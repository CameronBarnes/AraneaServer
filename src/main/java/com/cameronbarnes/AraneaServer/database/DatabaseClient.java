package com.cameronbarnes.AraneaServer.database;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DatabaseClient extends Thread {
	
	private ConcurrentLinkedQueue<DatabasePacket> mDatabasePackets;
	private boolean running;
	
	public DatabaseClient(ConcurrentLinkedQueue<DatabasePacket> databasePackets) {
		mDatabasePackets = databasePackets;
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
	
	private void processRequest(DatabasePacket request) {
	
	
	
	}
	
	public void stopThread() {
		running = false;
	}
	
	private void cleanup() {
	
	}
	
}
