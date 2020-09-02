package com.cameronbarnes.AraneaServer.core;

import com.cameronbarnes.AraneaServer.database.DatabaseHandler;
import com.cameronbarnes.AraneaServer.netowrking.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
	
	private static final int NUM_DATABASE_THREADS = 5;
	
	private static boolean run = true;
	
	private static final Logger log = LogManager.getLogger(Main.class.getName());
	
	public static void main(String[] args) {
	
		System.out.println("Hello World!");
		
		String uri = "localhost";
		
		DatabaseHandler databaseHandler = new DatabaseHandler(NUM_DATABASE_THREADS, uri);
		Server server = new Server(databaseHandler);
		server.start();
		
		while (run) {
			
			int idealThreadCount = server.getNumActiveClients() / 5;
			int currThreads = databaseHandler.getNumDatabaseHandlerThreads();
			
			if (idealThreadCount > NUM_DATABASE_THREADS) {
				if (idealThreadCount < currThreads) {
					int newThreads = idealThreadCount - currThreads;
					databaseHandler.allocateAdditionalDatabaseThreads(newThreads);
					log.info("Creating " + newThreads + " new threads for an ideal count of" +
							         idealThreadCount);
				}
				else {
					int trimThreads = currThreads - idealThreadCount;
					databaseHandler.trimExtraDatabaseThreads(trimThreads);
					log.info("removing " + trimThreads +
							         " threads for an ideal count of" + idealThreadCount);
				}
			}
			
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				databaseHandler.cleanup();
			}
			
		}
		
		server.stopThread();
		
	}
	
}
