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
        // System.out.println("XXXXXXXXXXXXX StabilizationThread XXXXXXXXXXXXX");
        OutputStream os = areYouThereServer.getOutputStream();
        os.write("areYouThere\n".getBytes());
        os.flush();
        Thread.sleep(1000);
        os.close(); areYouThereServer.close();
      } catch (Exception ex) {
        try {
          System.out.println("\n=== My successor left ===");
          System.out.println("\n=== Updating finger table ===");
          for(int i = 0;i < Peer.m;i++) {
            if(Peer.fingerTable[i].key == ObtainSHA.SHA1(Peer.successorIPAdress[0] + ":" + Peer.successorPort[0])) {
              Peer.fingerTable[i].IPAddress = Peer.successorIPAdress[1];
              Peer.fingerTable[i].port = Peer.successorPort[1];
              Peer.fingerTable[i].key = ObtainSHA.SHA1(Peer.successorIPAdress[1] + ":" + Peer.successorPort[1]);
            }
          }
          Thread.sleep(1000);
          System.out.println("\n=== Updating first successors ===");
          Peer.successorPort[0] = Peer.successorPort[1];
          Peer.successorIPAdress[0] = Peer.successorIPAdress[1];
          System.out.println("\n=== Updating predecessor of immediate successor ===");
          Peer.updateSuccessor(false);
          Thread.sleep(2000);
          System.out.println("\n=== Updating two other successors ===");
          String[] temp = SearchSuccessor.returnSuccessor(Peer.successorIPAdress[0], Peer.successorPort[0]).split(":");
          Peer.successorIPAdress[1] = temp[0]; Peer.successorPort[1] = Integer.parseInt(temp[1]);
          temp = SearchSuccessor.returnSuccessor(Peer.successorIPAdress[1], Peer.successorPort[1]).split(":");
          Peer.successorIPAdress[2] = temp[0]; Peer.successorPort[2] = Integer.parseInt(temp[1]);
          Thread.sleep(2000); // correct
          System.out.println("\n=== Updating successors and finger table of predecessors ===");
          Peer.updatePredecessorsAfterDelete();
          Thread.sleep(2000);
          System.out.println("\n=== System is stable now ===");
          MainMenu.display();
        } catch(Exception exp) {
          exp.printStackTrace();
        }
      }
    }
  }
}
