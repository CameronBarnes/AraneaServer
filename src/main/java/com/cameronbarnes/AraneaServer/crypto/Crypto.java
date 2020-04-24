package com.cameronbarnes.AraneaServer.crypto;

import com.cameronbarnes.AraneaServer.netowrking.NetworkData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import java.security.*;
import java.util.UUID;

public class Crypto {
	
	public static final int SYMMETRIC_KEY_SIZE = 128;
	public static final int ASYMMETRIC_KEY_SIZE = 1024;
	public static final String SYMMETRIC_ALGORITHM = "AES";
	public static final String ASYMMETRIC_ALGORITHM = "RSA";
	public static final String ASYMMETRIC_ALGORITHM_FORMATTING = "RSA/None/OAEPWithSHA1AndMGF1Padding";
	public static final String SYMMETRIC_ALGORITHM_FORMATTING = "AES/CBC/PKCS5Padding";
	
	private UUID mUUID = null;
	
	private SecretKey mAESKey;
	private PublicKey mRemotePublicKey;
	private KeyPair mKeyPair;
	private boolean mIsServer;
	
	private KeyPairGenerator mKeyPairGenerator;
	private KeyGenerator mAESKeyGenerator;
	public static final SecureRandom secureRandom = new SecureRandom();
	
	private Cipher mRSAEncryptCipher;
	private Cipher mRSADecryptCipher;
	
	private Cipher mAESEncryptCipher;
	private Cipher mAESDecryptCipher;
	
	public Crypto(KeyPair localKeys, PublicKey remotePublicKey, boolean isServer) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		
		mKeyPair = localKeys;
		mRemotePublicKey = remotePublicKey;
		mIsServer = isServer;
		
		mRSAEncryptCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM_FORMATTING);
		mRSAEncryptCipher.init(Cipher.ENCRYPT_MODE, mKeyPair.getPrivate());
		mRSADecryptCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM_FORMATTING);
		mRSADecryptCipher.init(Cipher.DECRYPT_MODE, remotePublicKey);
		
		if (isServer) {
			
			mAESKeyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
			mAESKeyGenerator.init(SYMMETRIC_KEY_SIZE, secureRandom);
			
		}
		updateAESKey(null);
	
	}
	
	public Crypto(KeyPair localKeys, boolean isServer) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		
		mKeyPair = localKeys;
		mIsServer = isServer;
		
		mRSAEncryptCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM_FORMATTING);
		mRSAEncryptCipher.init(Cipher.ENCRYPT_MODE, mKeyPair.getPrivate());
		mRSADecryptCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM_FORMATTING);
		
		if (isServer) {
			
			mAESKeyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
			mAESKeyGenerator.init(SYMMETRIC_KEY_SIZE, secureRandom);
			
		}
		updateAESKey(null);
		
	}
	
	public Crypto(boolean isServer) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		
		mIsServer = isServer;
		
		mKeyPairGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
		mKeyPairGenerator.initialize(ASYMMETRIC_KEY_SIZE, secureRandom);
		mKeyPair = genRSAKey();
		
		mRSAEncryptCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM_FORMATTING);
		mRSAEncryptCipher.init(Cipher.ENCRYPT_MODE, mKeyPair.getPrivate());
		mRSADecryptCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM_FORMATTING);
		
		if (isServer) {
			
			mAESKeyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
			mAESKeyGenerator.init(SYMMETRIC_KEY_SIZE, secureRandom);
			
		}
		updateAESKey(null);
		
	}
	
	public void updateAESKey(SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		
		if (isServer()) {
			
			if (key == null) {
				
				mAESKey = genAESKey();
				
			}
			else {
				mAESKey = key;
			}
			
		}
		else {
			mAESKey = key;
		}
		
		if (mAESKey != null) {
			mAESEncryptCipher = Cipher.getInstance(SYMMETRIC_ALGORITHM_FORMATTING);
			mAESEncryptCipher.init(Cipher.ENCRYPT_MODE, mAESKey);
			mAESDecryptCipher = Cipher.getInstance(SYMMETRIC_ALGORITHM_FORMATTING);
			mAESDecryptCipher.init(Cipher.DECRYPT_MODE, mAESKey);
		}
		
	}
	
	public NetworkData decryptNetworkData(@NotNull NetworkData networkData, boolean symmetric) throws BadPaddingException, IllegalBlockSizeException {
	
		byte[] rawBytes = networkData.getByteData();
		
		byte[] bytes;
		if (symmetric) {
			bytes = decryptBytesAES(rawBytes);
		}
		else {
			bytes = decryptBytesRSA(rawBytes);
		}
		
		return NetworkData.deserializeFromBytes(bytes);
	
	}
	
	public NetworkData encryptNetworkData(@NotNull NetworkData networkData, boolean symmetric) throws BadPaddingException, IllegalBlockSizeException {
		
		if (mUUID != null && networkData.getUUID() == null) {
			networkData.setUUID(mUUID);
		}
		
		byte[] rawBytes = networkData.serializeToBytes();
		
		byte[] encryptedBytes;
		if (symmetric) {
			encryptedBytes = encryptBytesAES(rawBytes);
		}
		else {
			encryptedBytes = encryptBytesRSA(rawBytes);
		}
		
		NetworkData data =
				new NetworkData(NetworkData.DataType.EncryptedNetworkDataRSA, mUUID);
		data.setByteData(encryptedBytes);
		return data;
		
	}
	
	public byte[] decryptBytesRSA(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
		
		return mRSADecryptCipher.doFinal(bytes);
		
	}
	
	public byte[] encryptBytesRSA(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
		
		return mRSAEncryptCipher.doFinal(bytes);
		
	}
	
	public byte[] decryptBytesAES(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
		
		return mAESDecryptCipher.doFinal(bytes);
	
	}
	
	public byte[] encryptBytesAES(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
		
		return mAESEncryptCipher.doFinal(bytes);
		
	}
	
	/**
	 *
	 * @param key the crypto key to set
	 *
	 * sets the value of the AES key and initialize the AES ciphers to use it
	 */
	public void setAESKey(SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		updateAESKey(key);
	}
	
	/**
	 *
	 * @param key the crypto key to set
	 *
	 * sets the value of the remote key and initialize the RSA decryption cipher to use it
	 */
	public void setRemotePublicKey(PublicKey key) throws InvalidKeyException {
		mRemotePublicKey = key;
		mRSADecryptCipher.init(Cipher.DECRYPT_MODE, key);
	}
	
	public SecretKey getAESKey() {
		return mAESKey;
	}
	
	public PublicKey getPublicKey() {
		return mKeyPair.getPublic();
	}
	
	public PublicKey getRemotePublicKey() {
		return mRemotePublicKey;
	}
	
	public boolean isServer() {
		return mIsServer;
	}
	
	public void genUUID() {
		mUUID = UUID.randomUUID();
	}
	
	public boolean checkUUID(UUID uuid) {
		return mUUID.equals(uuid);
	}
	
	@NotNull
	@Contract(pure = true)
	public KeyPair genRSAKey() {
		return mKeyPairGenerator.generateKeyPair();
	}
	
	@NotNull
	@Contract(pure = true)
	public SecretKey genAESKey() {
		return mAESKeyGenerator.generateKey();
	}
	
}
