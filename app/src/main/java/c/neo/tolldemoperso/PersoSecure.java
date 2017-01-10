package c.neo.tolldemoperso;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class PersoSecure {
	
	private static final String salt = "A long, but constant phrase that will be used each time as the salt.";
    private static final int iterations = 2000;
    private static final int keyLength = 128;
    
	public PersoSecure(){
		Security.insertProviderAt(new BouncyCastleProvider(),1);
	}
	
	public static byte[] createkey(String clearkey)
			throws NoSuchAlgorithmException, NoSuchProviderException {
		
		//byte[] keyStart = clearkey.getBytes();
		KeyGenerator kgen = KeyGenerator.getInstance("AES","BC");
		//SecureRandom sr = SecureRandom.getInstance(AES_ALGORITHM);
		//sr.setSeed(keyStart);
		kgen.init(128); // 192 and 256 bits may not be available
		SecretKey skey = kgen.generateKey();
		byte[] key = skey.getEncoded();
		return key;
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
