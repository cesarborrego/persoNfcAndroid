package c.neo.secure;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class PersoSecureParking {
	
	private static final String salt = 
			"The information contained in this communication may be confidential, "
			+ "is intended only for the use of the recipient named above, and may be legally privileged. "
			+ "If the reader of this message is not the intended recipient, you are hereby notified that "
			+ "any dissemination, distribution, or copying of this communication, or any of its contents, "
			+ "is strictly prohibited. If you have received this communication in error, please re-send "
			+ "this communication to the sender and delete the original message and any copy of it from "
			+ "your computer system.";
    private static final int iterations = 2000;
    private static final int keyLength = 128;
    
	public PersoSecureParking(){
		Security.insertProviderAt(new BouncyCastleProvider(),1);
	}
	
	
	public static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES","BC");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	public static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES","BC");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public static String bin2hex(byte[] data) {
		return String.format("%0" + (data.length * 2) + "X", new BigInteger(1,
				data));
	}

	public static byte[] getSHA2(String rawtext) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		digest.update(rawtext.getBytes());
		return digest.digest();
	}

	public static String getSHA2String(String rawtext) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		digest.update(rawtext.getBytes());
		return bin2hex(digest.digest());
	}
	
	public static byte[] getkey(String passphrase) throws Exception{
		SecretKey key = generateKey(passphrase);
        byte[] keybytes = key.getEncoded();
        System.out.println("Key PBE ("+keybytes.length*8+"):" + bin2hex(keybytes));
        return keybytes;
	}
	
	private static SecretKey generateKey(String passphrase) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), salt.getBytes(), iterations, keyLength);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWITHSHAAND128BITAES-CBC-BC","BC");
        return keyFactory.generateSecret(keySpec);
    }

}
