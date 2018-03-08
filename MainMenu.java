// author: Abhishek Agrawal
// dateCreated: 08/03/2018

import java.util.*;
import java.io.*;

public class MainMenu {
  private static void display() {
    System.out.println("\nMenu:\n1. IP Address and ID\n2. IP address and ID of the successor and predecessor");
    System.out.print("3. The file key IDs it contains\n4. Finger table\nEnter choice: ");
  }
  public static void run() {
    Scanner sc = new Scanner(System.in);
    System.out.println("\n[USE CONTROL-C TO STOP THIS PEER]\n");
    while(true) {
      display();
      int x = sc.nextInt();
      switch(x) {
        case 1:
          System.out.println("IP Address: " + Peer.myIPAdress + ", Port: " + Peer.myPort + "\nID: " + Peer.myKey + "\n");
          break;
        case 2:
          try {
            System.out.println("Address of successor: " + Peer.successorIPAdress[0] + ":" + Peer.successorPort[0]
                                  + " with id: " + ObtainSHA.SHA1(Peer.successorIPAdress[0] + ":" + Peer.successorPort[0]));
            System.out.println("Address of predecessor: " + Peer.predecessor
                                  + " with id: " + ObtainSHA.SHA1(Peer.predecessor) + "\n");
          } catch(Exception ex) {
            ex.printStackTrace();
          }
          break;
        case 3:
          File[] listOfFiles = (new File("./files")).listFiles();
          TreeSet< Integer > uniqueFileKey = new TreeSet< Integer >();
          try {
            for (int i = 0; i < listOfFiles.length; i++)
              uniqueFileKey.add(ObtainSHA.SHA1(listOfFiles[i].getName()));
                // System.out.println(listOfFiles[i].getName() + " with key: " + ObtainSHA.SHA1(listOfFiles[i].getName()));
          } catch(Exception ex) {
            ex.printStackTrace();
          }
          System.out.print("This node has files with following keys: ");
          System.out.println(uniqueFileKey);
          break;
        case 4:
          for(RowInFingerTable i: Peer.fingerTable) {
            String y = i.IPAddress + ":" + i.port;
            try {
              System.out.println("[" + i.startInterval + ", " + i.endInterval+") -> " + ObtainSHA.SHA1(y) + " (" + y + ")");
            } catch(Exception ex) {
              ex.printStackTrace();
            }
          }
          System.out.println();
          break;
        default:
          System.out.println("XXXXXXXXXXXXX Invalid Menu Item XXXXXXXXXXXXX\n");
      }
    }
  }
}
