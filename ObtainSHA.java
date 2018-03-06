// author: Abhishek Agrawal
// dateCreated: 01/03/2018

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ObtainSHA {
  public static int hashString(String message, String algorithm) throws HashGenerationException {
    try {
      MessageDigest digest = MessageDigest.getInstance(algorithm);
      byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));
      return convertToHex(hashedBytes);
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
        throw new HashGenerationException("Could not generate hash from String", ex);
    }
  }
  public static int SHA1(String message) throws HashGenerationException {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));
      return convertToHex(hashedBytes);
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
        throw new HashGenerationException("Could not generate hash from String", ex);
    }
  }
  private static int convertToHex(byte[] data) {
    // Trancating here
    // System.out.println("data[0]: " + data[0]);
    if(Peer.m == 5)
      return (data[0] >>> Peer.m) & 0x1F;
    else {
      System.out.println("Change hexcode above since m != 5.");
      return -1;
    }
    // StringBuffer buf = new StringBuffer();
    // for (int i = 0; i < data.length; i++) {
    //   int halfbyte = (data[i] >>> 4) & 0x0F;
    //   int two_halfs = 0;
    //   do {
    //     if ((0 <= halfbyte) && (halfbyte <= 9))
    //       buf.append((char) ('0' + halfbyte));
    //     else
    //       buf.append((char) ('a' + (halfbyte - 10)));
    //     halfbyte = data[i] & 0x0F;
    //   } while(two_halfs++ < 1);
    // }
    // return buf.toString();
  }
}
class HashGenerationException extends Exception {
  public HashGenerationException() {
  	super();
  }
  public HashGenerationException(String message, Throwable throwable) {
  	super(message, throwable);
  }
  public HashGenerationException(String message) {
  	super(message);
  }
  public HashGenerationException(Throwable throwable) {
  	super(throwable);
  }
}
