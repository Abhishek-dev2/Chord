// author: Abhishek Agrawal
// dateCreated: 01/03/2018

import java.io.*;
import java.net.*;
import java.util.*;

public class SearchFile {
  public static String returnFileAddress(int fileKey, String IPAddress, int port) throws Exception {
    if(ObtainSHA.SHA1(IPAddress + ":" + port) == fileKey)
      return (IPAddress + ":" + port);
    // System.out.println("Peer IP: " + IPAddress + ", port: " + port);
    Socket askForFileAddressServer = new Socket(InetAddress.getByName(IPAddress), port);
    // System.out.println("XXXXXXXXXXXXX returnFileAddress() of " + fileKey + " XXXXXXXXXXXXX");
    BufferedReader br = new BufferedReader(new InputStreamReader(askForFileAddressServer.getInputStream()));
    OutputStream os = askForFileAddressServer.getOutputStream();
    os.write(("SendFileAddress\n").getBytes());
    os.flush();
    os.write((fileKey + "\n").getBytes());
    os.flush();
    String fileAddress = br.readLine();
    os.close(); br.close();
    return fileAddress;
  }
}
