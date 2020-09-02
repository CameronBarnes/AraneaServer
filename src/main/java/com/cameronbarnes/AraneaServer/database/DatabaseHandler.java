package com.cameronbarnes.AraneaServer.database;

import com.cameronbarnes.AraneaCore.crypto.credentials.Permissions;
import com.cameronbarnes.AraneaCore.database.DatabasePacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DatabaseHandler implements DatabaseInterface {
	
	private final ConcurrentLinkedQueue<DatabasePacket> mDatabasePackets;
	private final ConcurrentHashMap<UUID, Permissions> mUUIDPermissions;
	private DatabaseClient[] mDatabaseClients;
	
	private String mURI;
	
	public DatabaseHandler(int numClient, String uri) {
		
		mURI = uri;
		
		mUUIDPermissions = new ConcurrentHashMap<>();
		mDatabasePackets = new ConcurrentLinkedQueue<>();
		mDatabaseClients = new DatabaseClient[numClient];
		
		for (int i = 0; i < numClient; i++) {
			mDatabaseClients[i] = new DatabaseClient(mDatabasePackets, mURI);
			mDatabaseClients[i].start();
		}
		
	}
	
	public void allocateAdditionalDatabaseThreads(int numNewThreads) {
		
		DatabaseClient[] clients =
				new DatabaseClient[mDatabaseClients.length + numNewThreads];
		System.arraycopy(mDatabaseClients, 0, clients, 0, mDatabaseClients.length);
		for (int i = mDatabaseClients.length; i < clients.length; i++) {
			DatabaseClient client = new DatabaseClient(mDatabasePackets, mURI);
			client.start();
			clients[i] = client;
		}
		mDatabaseClients = clients;
		
	}
	
	public void trimExtraDatabaseThreads(int numThreadsRemoved) {
		
		int numIdealClients = mDatabaseClients.length - numThreadsRemoved;
		DatabaseClient[] clients =
				new DatabaseClient[mDatabaseClients.length - numThreadsRemoved];
		System.arraycopy(mDatabaseClients, 0, clients, 0, numIdealClients);
		for (int i = clients.length; i < mDatabaseClients.length; i++) {
			
			mDatabaseClients[i].stopThread();
			
		}
		mDatabaseClients = clients;
		
	}
	
	public int getNumDatabaseHandlerThreads() {
		return mDatabaseClients.length;
	}
	
	public void cleanup() {
		
		for (DatabaseClient client: mDatabaseClients) {
			client.stopThread();
		}
		
	}
	
	@Override
	public void submitRequest(@NotNull DatabasePacket request) {
		mDatabasePackets.add(request);
		mDatabasePackets.notify();
	}
	
}
