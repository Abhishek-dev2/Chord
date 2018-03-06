// author: Abhishek Agrawal
// dateCreated: 01/03/2018

import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {
  static boolean firstNode = false;
  static ServerSocket welcomeSocket = null;
  static String myIPAdress = "", peerIPAdress;
  static int myPort, myKey, m = 5, peerPort;
  static RowInFingerTable[] fingerTable = new RowInFingerTable[m];
  static String predecessor = "";
  static String[] successorIPAdress = new String[3];
  static int[] successorPort = new int[3];
  public static void main(String[] args) throws Exception {
    turnServerOn();
    myKey = ObtainSHA.SHA1(myIPAdress + ":" + myPort);
    System.out.println("key: " + myKey);
    String x = peerIPAdress, w = myIPAdress;
    int y = peerPort, z = myPort;
    for(int i = 0;i < 3;i++) {
      String[] temp = obtainSuccessor(ObtainSHA.SHA1(w + ":" + z), x, y).split(":");
      successorIPAdress[i] = temp[0];
      successorPort[i] = Integer.parseInt(temp[1]);
      x = successorIPAdress[i];
      y = successorPort[i];
      w = x; z = y;
    }
    predecessor = obtainPredecessor(successorIPAdress[0], successorPort[0]);
    createFingerTable();
  }
  private static String obtainSuccessor(int key, String IPAddress, int port) throws Exception {
    return SearchFile.returnFileAddress(key, IPAddress, port);
  }
  private static String obtainPredecessor(String IPAddress, int port) {
    return "";
  }
  private static void createFingerTable() {
    int start = myKey + 1, end;
    for(int i = 0;i < m;i++) {
      String succ = SearchFile.returnFileAddress(start, peerIPAdress, peerPort);
      end = start + (int)Math.pow(2, i);
      String[] temp = succ.split(":");
      fingerTable[i] = new RowInFingerTable(start, end, Integer.parseInt(temp[1]), temp[0]);

    }
  }
  // private void sendFingerTable() throws Exception {
  //   ServerSocket sendFTServer = new ServerSocket(myPort, 0, InetAddress.getByName(myIPAdress));
  //   System.out.println("Opened port for sending finger table at machine: "+myKey+" at: "+myIPAdress+":"+myPort+".");
  //   Socket connectionSocket = sendFTServer.accept();
  //   OutputStream os = connectionSocket.getOutputStream();
  //   String response = "";
  //   for (Map.Entry<Integer, String> pair : fingerTable.entrySet()) {
  //     response += (pair.getKey() + "->" + pair.getValue() + "|");
  //   }
  //   os.write(response.getBytes());
  //   os.close();
  //   connectionSocket.close();
  //   sendFTServer.close();
  // }
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
        myPort = rand.nextInt(65536);
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
