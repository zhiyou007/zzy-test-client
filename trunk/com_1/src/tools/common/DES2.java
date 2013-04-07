package tools.common;

import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;


public class DES2
{
    public static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";


    public static String encode(String key,String data) throws Exception
    {
        return encode(key, data.getBytes());
    }

    public static String encode(String key,byte[] data) throws Exception
    {
        try
        {
	    	DESKeySpec dks = new DESKeySpec(key.getBytes());
	    	
	    	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            //key???�???��?�??8�????
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            IvParameterSpec iv = new IvParameterSpec(key.getBytes());
            AlgorithmParameterSpec paramSpec = iv;
            cipher.init(Cipher.ENCRYPT_MODE, secretKey,paramSpec);
            
            byte[] bytes = cipher.doFinal(data);
            
            return new String(Base64Util.encode(bytes));
        } catch (Exception e)
        {
            throw new Exception(e);
        }
    }


    public static byte[] decode(String key,byte[] data) throws Exception
    {
        try
        {
        	SecureRandom sr = new SecureRandom();
	    	DESKeySpec dks = new DESKeySpec(key.getBytes());
	    	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            //key???�???��?�??8�????
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            IvParameterSpec iv = new IvParameterSpec(key.getBytes());
            AlgorithmParameterSpec paramSpec = iv;
            cipher.init(Cipher.DECRYPT_MODE, secretKey,paramSpec);
            return cipher.doFinal(data);
        } catch (Exception e)
        {
            throw new Exception(e);
        }
    }
    

    public static String decodeValue(String key,String data) 
    {
    	byte[] datas;
    	String value = null;
		try {
			if(System.getProperty("os.name") != null && (System.getProperty("os.name").equalsIgnoreCase("sunos") || System.getProperty("os.name").equalsIgnoreCase("linux")))
	        {			
	    		datas = decode(key,Base64Util.decode(data));				
	        }
	    	else
	    	{
	    		datas = decode(key,Base64Util.decode(data));
	    	}			
			value = new String(datas);
		} catch (Exception e) {			
			value = "";
		}
    	return value;
    }


}