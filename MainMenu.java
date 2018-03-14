// author: Abhishek Agrawal
// dateCreated: 08/03/2018

import java.util.*;
import java.io.*;

public class MainMenu {
  public static void display() {
    System.out.println("\nMenu:\n1. IP Address and ID\n2. IP address and ID of the successors and predecessor");
    System.out.println("3. The file key IDs it contains\n4. Finger table\n5. File address search");
    System.out.print("6. Obtain SHA1 Key");
    System.out.print("\n[USE CONTROL-C TO STOP THIS PEER]\nEnter choice: ");
  }
  public static void run() {
    Scanner sc = new Scanner(System.in);
    while(true) {
      display();
      int x = sc.nextInt();
      try {
        switch(x) {
          case 1:
            System.out.println("IP Address: " + Peer.myIPAdress + ", Port: " + Peer.myPort + "\nID: " + Peer.myKey + "\n");
            break;
          case 2:
            for(int i = 0;i < 3;i++)
              System.out.println("Successor " + (i + 1) + ": "
                                  + ObtainSHA.SHA1(Peer.successorIPAdress[i] + ":" + Peer.successorPort[i])
                                  + " (" + Peer.successorIPAdress[i] + ":" + Peer.successorPort[i] + ")");
            System.out.println("Predecessor: " + ObtainSHA.SHA1(Peer.predecessor)
                                    + " (" + Peer.predecessor + ")" + "\n");
            break;
          case 3:
            File[] listOfFiles = (new File("./files")).listFiles();
            TreeSet< Integer > uniqueFileKey = new TreeSet< Integer >();
            for (int i = 0; i < listOfFiles.length; i++)
              uniqueFileKey.add(ObtainSHA.SHA1(listOfFiles[i].getName()));
            System.out.print("This node has files with following keys: ");
            System.out.println(uniqueFileKey);
            break;
          case 4:
            for(RowInFingerTable i: Peer.fingerTable) {
              String y = i.IPAddress + ":" + i.port;
              System.out.println("[" + i.startInterval + ", " + i.endInterval+") -> " + ObtainSHA.SHA1(y) + " (" + y + ")");
            }
            System.out.println();
            break;
          case 5:
            System.out.print("Enter the key of the required file: ");
            int y = sc.nextInt();
            if(y >= Math.round(Math.pow(2, Peer.m)) || y < 0) {
              System.out.println("Invalid key.");
              break;
            }
            String addr = SearchFile.returnFileAddress(y, Peer.myIPAdress, Peer.myPort);
            int addrKey = ObtainSHA.SHA1(addr);
            System.out.println("File with the required key must be at node: " + addrKey + " (" + addr + ")" + "\n");
            break;
          case 6:
            System.out.print("Enter the string: ");
            sc.nextLine();
            String t = sc.nextLine();
            System.out.println("The SHA1 key truncated is: " + ObtainSHA.SHA1(t) + "\n");
            break;
          default:
            System.out.println("XXXXXXXXXXXXX Invalid Menu Item XXXXXXXXXXXXX\n");
        }
      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
