// author: Abhishek Agrawal
// dateCreated: 01/03/2018

import java.io.*;
import java.net.*;
import java.util.*;

public class SearchSuccessor {
  public static String returnSuccessor(String IPAddress, int port) throws Exception {
    Socket askForSuccessorServer = new Socket(InetAddress.getByName(IPAddress), port);
    System.out.println("*************** returnSuccessor() ***************");
    BufferedReader br = new BufferedReader(new InputStreamReader(askForSuccessorServer.getInputStream()));
    OutputStream os = askForSuccessorServer.getOutputStream();
    os.write(("SendFirstSuccessor\n").getBytes());
    os.flush();
    String successorAddress = br.readLine();
    os.close(); br.close();
    return successorAddress;
  }
}
