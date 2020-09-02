package com.cameronbarnes.AraneaServer.database;

import com.cameronbarnes.AraneaCore.database.DatabasePacket;

public interface DatabaseInterface {
	
	void submitRequest(DatabasePacket request);
	
}
