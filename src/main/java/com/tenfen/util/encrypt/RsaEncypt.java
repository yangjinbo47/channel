package com.tenfen.util.encrypt;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.lang.StringUtils;

import com.tenfen.util.LogUtil;
import com.tenfen.util.StreamUtil;

public class RsaEncypt {

	private static final String ALGORITHM = "RSA";

	private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	private static final String DEFAULT_CHARSET = "UTF-8";

	public static String sign(String content, String privateKey) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					BASE64.decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			Signature signature = Signature.getInstance(SIGN_ALGORITHMS);

			signature.initSign(priKey);
			signature.update(content.getBytes(DEFAULT_CHARSET));

			byte[] signed = signature.sign();

			return BASE64.encode(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static boolean rsaCheckContent(String content, String sign,
			String publicKey, String charset) throws Exception {
		boolean b = false;
		try {
			PublicKey pubKey = getPublicKeyFromX509(ALGORITHM,
					new ByteArrayInputStream(publicKey.getBytes()));
			Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
			signature.initVerify(pubKey);
			if (StringUtils.isEmpty(charset))
				signature.update(content.getBytes(DEFAULT_CHARSET));
			else
				signature.update(content.getBytes(charset));
//			return signature.verify(base64.decodeBase64(sign.getBytes()));
			b = signature.verify(BASE64.decode(sign));
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return b;
	}
	
	public static PublicKey getPublicKeyFromX509(String algorithm, InputStream ins)
	        throws Exception
	    {
	        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
	        StringWriter writer = new StringWriter();
	        StreamUtil.io(new InputStreamReader(ins), writer);
//	        byte encodedKey[] = writer.toString().getBytes();
	        byte[] encodedKey = BASE64.decode(writer.toString());
//	        encodedKey = Base64.decodeBase64(encodedKey);
	        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
	    }

}
