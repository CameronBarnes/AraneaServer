package com.cameronbarnes.AraneaServer.database;

import com.cameronbarnes.AraneaServer.crypto.credentials.UsernamePasswordCredential;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class DatabasePacket {
	
	private final boolean mIsRequest;
	private final PacketType mPacketType;
	private final DatabaseResponse mResponse;
	
	private UsernamePasswordCredential mCredential;
	
	private DatabasePacket(boolean isRequest, PacketType type, DatabaseResponse response) {
		mIsRequest = isRequest;
		mPacketType = type;
		mResponse = response;
	}
	
	@NotNull
	@Contract(value = "_ -> new", pure = true)
	public static DatabasePacket newRequest(PacketType type) {
		return new DatabasePacket(true, type, null);
	}
	
	@NotNull
	@Contract(value = "_, _ -> new", pure = true)
	public static DatabasePacket newResponse(PacketType type, DatabaseResponse response) {
		return new DatabasePacket(false, type, response);
	}
	
	@NotNull
	@Contract(value = "_ -> new")
	public static DatabasePacket credentialVerificationRequest(UsernamePasswordCredential credential) {
		
		DatabasePacket packet = newRequest(PacketType.ProcessCredentials);
		packet.mCredential = credential;
		return packet;
		
	}
	
	public UsernamePasswordCredential getCredential() {
		return mCredential;
	}
	
	public PacketType getPacketType() {
		return mPacketType;
	}
	
	public DatabaseResponse getResponse() {
		return mResponse;
	}
	
	public boolean isRequest() {
		return mIsRequest;
	}
	
	public enum PacketType {
		AddUser,
		ProcessCredentials,
		RequestUserData,
		RequestDocument,
		RequestIndex
	}
	
	public enum DatabaseResponse {
		Success,
		GenericFailure,
		NoSuchDocument,
		InvalidPermissions,
		NoSuchUser,
		AuthenticationError
	}
	
	public static class PacketResponseTypeMismatchException extends Exception {
		
		public PacketResponseTypeMismatchException(boolean expectedRequest) {
			super(expectedRequest ? "Expected request packet, received response" :
					      "Expected response packet, received request");
		}
		
	}
	
}
