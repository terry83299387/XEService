package xextension.operation.run_app;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * decryption .
 * 
 */
public class DesDecrypt {
	private static final String DES = "DES";

	private String defaultCharset = "UTF-8";

	/**
	 * decrypt data based on DES.
	 * 
	 * @param data
	 *          data.
	 * @param key
	 *          key.
	 * @return decription.
	 * @throws UnsupportedEncodingException .
	 * @throws BadPaddingException .
	 * @throws IllegalBlockSizeException .
	 * @throws InvalidKeySpecException .
	 * @throws NoSuchPaddingException .
	 * @throws NoSuchAlgorithmException .
	 * @throws InvalidKeyException .
	 * @throws UnsupportedEncodingException .
	 * 
	 */
	public String decrypt(String data, String key) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException {
		return new String(decrypt(hexStringToBytes(data), key.getBytes(defaultCharset)));
	}

	private byte[] decrypt(byte[] src, byte[] key) throws IllegalBlockSizeException, BadPaddingException,
			InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {

		SecureRandom sr = new SecureRandom();
		DESKeySpec dks = new DESKeySpec(key);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance(DES);
		cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

		return cipher.doFinal(src);
	}

	/**
	 * convert hex String To Bytes.
	 * 
	 * @param hexString
	 *          hexString.
	 * @return hex byte[]
	 */
	private byte[] hexStringToBytes(String hexString) {
		if (hexString == null || "".equals(hexString)) {
			return null;
		}
		String upperhex = hexString.toUpperCase();
		int length = upperhex.length() / 2;
		char[] hexChars = upperhex.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public void setKeyCharset(String charset) {
		defaultCharset = charset;
	}
}
