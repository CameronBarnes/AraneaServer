package com.cameronbarnes.AraneaServer.netowrking;

import com.cameronbarnes.AraneaCore.crypto.Crypto;
import com.cameronbarnes.AraneaCore.networking.NetworkData;
import com.cameronbarnes.AraneaServer.database.DatabaseInterface;
import com.cameronbarnes.AraneaCore.database.DatabasePacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ClientHandler extends Thread implements ClientDatabaseCallbackListener {
	
	private String mName;
	private UUID mUUID;
	private final ObjectInputStream mObjectIn;
	private final ObjectOutputStream mObjectOut;
	private Socket mSocket;
	private boolean mLoggedIn;
	
	private Crypto mCrypto;
	
	DatabaseInterface mDatabase;
	ServerClientInterface mServer;
	
	private volatile boolean running;
	
	private static Logger log = LogManager.getLogger(ClientHandler.class.getName());
	
	public ClientHandler(@NotNull Socket socket, String name, DatabaseInterface data, ServerClientInterface server) throws IOException {
	
		mName = name;
		mServer = server;
		mDatabase = data;
		mSocket = socket;
		
		mObjectIn = new ObjectInputStream(socket.getInputStream());
		mObjectOut = new ObjectOutputStream(socket.getOutputStream());
	
	}
	
	@Override
	public void run() {
		
		running = true;
		try {
			init();
			loop();
		}
		catch (Exception e) {
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
	
	private void init() throws IOException, NoSuchAlgorithmException,
			                           InvalidKeyException, NoSuchPaddingException {
	
		mLoggedIn = false;
		mCrypto = new Crypto(true);
		sendNetworkData(NetworkData.RSAKeyRequest(mCrypto.getPublicKey()));
	
	}
	
	private void loop() throws IOException, ClassNotFoundException,
			                           BadPaddingException, IllegalBlockSizeException {
	
		NetworkData data;
		while (running) {
			
			data = (NetworkData) mObjectIn.readObject();
			
			if (data != null) {
				processNetworkData(data, false);
			}
		
		}
	
	}
	
	private void processNetworkData(@NotNull NetworkData data, boolean decrypted) throws BadPaddingException, IllegalBlockSizeException, IOException {
	
		if (mLoggedIn) {
			if (!mCrypto.checkUUID(data.getUUID())) {
				sendNetworkData(new NetworkData(NetworkData.DataType.InvalidUUIDError, null));
				return;
			}
		}
		
		//Forces the function to exit if the data is unencrypted when we expect it to be encrypted
		if (!decrypted) {
			if (data.getDataType() != NetworkData.DataType.EncryptedNetworkDataAES ||
					    data.getDataType() != NetworkData.DataType.EncryptedNetworkDataRSA) {
				log.warn("Received expected encrypted data of type: " + data.getDataType().name());
				sendExpectedEncryptedError();
				return;
			}
		}
		
		switch (data.getDataType()) {
			
			//Requests and responses that only the client should receive
			case CredentialRequest:
			case DocumentRequest:
			case DocumentUpdateResponse:
			case IssueAESKey:
			case RSAKeyRequest:
				log.warn("Server received unexpected request, likely a client side error");
				break;
				
			//Expected server side responses and requests here
			case RSAKeyResponse:
				try {
					mCrypto.setRemotePublicKey(data.getPublicKey());
					issueAESKey();
				}
				catch (InvalidKeyException e) {
					sendNetworkData(NetworkData.invalidKeyError(data.getPublicKey()));
					log.warn("Invalid key received from client");
				}
				break;
			case AESKeyResponse:
				sendNetworkData(new NetworkData(NetworkData.DataType.CredentialRequest, null), true);
				break;
			case EncryptedNetworkDataRSA:
				processNetworkData(mCrypto.decryptNetworkData(data, false), true);
				break;
			case EncryptedNetworkDataAES:
				processNetworkData(mCrypto.decryptNetworkData(data, true), true);
				break;
			case CredentialResponse:
				mDatabase.submitRequest(
						DatabasePacket.credentialVerificationRequest(
								data.getProjectName(),
								data.getCredential(),
								data.getUUID())
				);
				break;
		}
	
	}
	
	public void databaseResponse(@NotNull DatabasePacket response) throws DatabasePacket.PacketResponseTypeMismatchException {
		
		if (response.isRequest()) {
			throw new DatabasePacket.PacketResponseTypeMismatchException(false);
		}
		
		DatabasePacket.DatabaseResponse responseType = response.getResponse();
		
		//TODO handle database response
		switch (response.getPacketType()) {
			
			case AddUser:
				if (responseType.equals(DatabasePacket.DatabaseResponse.Success)) {
				
				}
				break;
			case ProcessCredentials:
				if (responseType.equals(DatabasePacket.DatabaseResponse.Success)) {
					mLoggedIn = true;
					mCrypto.genUUID();
					
				}
				break;
			case RequestUserData:
				break;
			case RequestDocument:
				break;
			case RequestIndex:
				break;
		}
		
	}
	
	@Contract(pure = true)
	private void sendDatabaseResponseError(@NotNull DatabasePacket.DatabaseResponse response) {
	
		switch (response) {
			
			case Success:
				log.error("Success is not a valid error type to the error reporting function");
				break;
			case GenericFailure:
				break;
			case NoSuchDocument:
				break;
			case InvalidPermissions:
				break;
			case NoSuchUser:
				break;
			case AuthenticationError:
				break;
		}
	
	}
	
	private void issueAESKey() throws BadPaddingException, IllegalBlockSizeException, IOException {
	
		sendNetworkData(NetworkData.issueAESKey(mCrypto.getAESKey()), false);
	
	}
	
	private void sendExpectedEncryptedError() throws IOException {
		sendNetworkData(new NetworkData(NetworkData.DataType.ExpectedEncryptedError, null));
	}
	
	public void sendNetworkData(NetworkData data) throws IOException {
		
		mObjectOut.writeObject(data);
		mObjectOut.flush();
		
	}
	
	public void sendNetworkData(NetworkData data, boolean symmetricEncryption) throws BadPaddingException, IllegalBlockSizeException, IOException {
		
		sendNetworkData(mCrypto.encryptNetworkData(data, symmetricEncryption));
		
	}
	
	public void stopThread() {
		running = false;
	}
	
	private void cleanup() throws IOException {
		
		mServer.removeClient(this);
		mObjectIn.close();
		mObjectOut.close();
		mSocket.close();
		mLoggedIn = false;
		log.info(mName + " Disconnected");
		
	}
	
}
