// author: Abhishek Agrawal
// dateCreated: 01/03/2018

import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {
  static boolean firstNode = false;
  static ServerSocket welcomeSocket = null;
  static String myIPAdress = "";
  static int myPort = 1025;
  static int myKey = 0;
  static String peerIPAdress = "";
  static int peerPort = 1025;
  public static void main(String[] args) throws Exception {
    turnServerOn();
    int key = ObtainSHA.SHA1(myIPAdress + ":" + myPort);
    System.out.println("key: " + key);
    // int i = Integer.parseInt(key, 16);
    // String bin = Integer.toBinaryString(i);
    // System.out.println("key in binary: " + bin);
    // createFingerTable();
  }
  private static void turnServerOn() throws Exception {
    Scanner sc = new Scanner(System.in);
    Random rand = new Random();
    System.out.println("Is this the first instance?(y/n)");
    switch(sc.nextLine()) {
      case "y":
        firstNode = true;
        break;
      case "n":
        firstNode = false;
        System.out.print("I need a IP Address and port of a peer(node) already running.\nIP Address: ");
        peerIPAdress = sc.nextLine();
        System.out.print("Port: ");
        peerPort = sc.nextInt();
        myPort = (rand.nextInt(65535) + 1) % 65535;
        if(myPort <= 1024)
          myPort += 1024;
        break;
      default:
        System.out.println("BC");
        System.exit(0);
    }
    welcomeSocket = new ServerSocket(myPort);
    myIPAdress = InetAddress.getLocalHost().getHostAddress().toString();
    System.out.println("Server for this peer is running at: " + myIPAdress + ":" + myPort);
  }
}
