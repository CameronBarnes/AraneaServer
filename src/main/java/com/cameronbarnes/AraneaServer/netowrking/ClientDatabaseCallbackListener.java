package com.cameronbarnes.AraneaServer.netowrking;

import com.cameronbarnes.AraneaServer.database.DatabasePacket;

public interface ClientDatabaseCallbackListener {
	
	void databaseResponse(DatabasePacket packet) throws DatabasePacket.PacketResponseTypeMismatchException;
	
}
