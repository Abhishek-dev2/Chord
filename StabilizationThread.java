// author: Abhishek Agrawal
// dateCreated: 09/03/2018

import java.util.*;
import java.io.*;
import java.net.*;

public class StabilizationThread extends Thread {
  public void run() {
    while(true) {
      try {
        Socket areYouThereServer = new Socket(InetAddress.getByName(Peer.successorIPAdress[0]), Peer.successorPort[0]);
        System.out.println("XXXXXXXXXXXXX StabilizationThread XXXXXXXXXXXXX");
        OutputStream os = areYouThereServer.getOutputStream();
        os.write("areYouThere\n".getBytes());
        os.flush();
        Thread.sleep(1000);
        os.close(); areYouThereServer.close();
      } catch (Exception ex) {
        try {
          System.out.println("\n=== My successor left ===");
          System.out.println("\n=== Updating finger table ===");
          Peer.fingerTable[0].IPAddress = Peer.successorIPAdress[1];
          Peer.fingerTable[0].port = Peer.successorPort[1];
          Peer.fingerTable[0].key = ObtainSHA.SHA1(Peer.successorIPAdress[1] + ":" + Peer.successorPort[1]);
          Thread.sleep(1000);
          System.out.println("\n=== Updating three successors ===");
          Peer.successorPort[0] = Peer.successorPort[1];
          Peer.successorIPAdress[0] = Peer.successorIPAdress[1];
          Peer.successorPort[1] = Peer.successorPort[0];
          Peer.successorIPAdress[1] = Peer.successorIPAdress[0];
          String[] temp = SearchSuccessor.returnSuccessor(Peer.successorIPAdress[1], Peer.successorPort[1]).split(":");
          Peer.successorIPAdress[2] = temp[0]; Peer.successorPort[2] = Integer.parseInt(temp[1]);
          Thread.sleep(2000);
          System.out.println("\n=== Updating predecessor of immediate successor ===");
          Peer.updateSuccessor();
          Thread.sleep(2000);
          System.out.println("\n=== Updating successors and finger table of predecessors ===");
          Peer.updatePredecessors();
          Thread.sleep(2000);
        } catch(Exception exp) {
          exp.printStackTrace();
        }
      }
    }
  }
}
