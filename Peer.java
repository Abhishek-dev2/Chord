// author: Abhishek Agrawal
// dateCreated: 01/03/2018

import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {
  public static boolean firstNode = false;
  public static String myIPAdress, peerIPAdress;
  public static int myPort, myKey, m = 5, peerPort;
  public static RowInFingerTable[] fingerTable = new RowInFingerTable[m];
  public static String predecessor = "";
  public static String[] successorIPAdress = new String[3];
  public static int[] successorPort = new int[3];
  public static void main(String[] args) throws Exception {
    turnServerOn();
    myKey = ObtainSHA.SHA1(myIPAdress + ":" + myPort);
    System.out.println("key: " + myKey);
    createFingerTable();
    // finding successors
    String[] temp = SearchSuccessor.returnSuccessor(myIPAdress, myPort, peerIPAdress, peerPort).split(":");
    successorIPAdress[0] = temp[0]; successorPort[0] = Integer.parseInt(temp[1]);
    temp = SearchSuccessor.returnSuccessor(successorIPAdress[0], successorPort[0], peerIPAdress, peerPort).split(":");
    successorIPAdress[1] = temp[0]; successorPort[1] = Integer.parseInt(temp[1]);
    temp = SearchSuccessor.returnSuccessor(successorIPAdress[1], successorPort[1], peerIPAdress, peerPort).split(":");
    successorIPAdress[2] = temp[0]; successorPort[2] = Integer.parseInt(temp[1]);
    // finding predecessor and updating it
    obtainAndUpdatePredecessor();
    updateSuccessor();
    Thread t = new MenuThread();
    t.start();
    queryProcessing();
  }
  // different peers will talk using this method
  private static void queryProcessing() throws Exception {
    ServerSocket queryProcessingServer = new ServerSocket(myPort, 0, InetAddress.getByName(myIPAdress));
    System.out.println("[MAIN THREAD] Opened port for queryProcessing at machine: "+myKey+" at: "+myIPAdress+":"+myPort+".");
    while(true) {
      Socket connectionSocket = queryProcessingServer.accept();
      BufferedReader br = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
      OutputStream os = connectionSocket.getOutputStream();
      String request = br.readLine();
      switch(request) {
        case "SendPredecessor":
          os.write((predecessor + "\n").getBytes());
          os.flush();
          break;
        case "UpdatePredecessorIAmNew":
          String newPeerIP = connectionSocket.getRemoteSocketAddress().toString().substring(1);
          String[] temp = newPeerIP.split(":");
          successorIPAdress[2] = successorIPAdress[1]; successorPort[2] = successorPort[1];
          successorIPAdress[1] = successorIPAdress[0]; successorPort[1] = successorPort[0];
          successorIPAdress[0] = temp[0]; successorPort[0] = Integer.parseInt(temp[1]);
          int newPeerKey = ObtainSHA.SHA1(newPeerIP);
          ///////////////////////////////////////////////////////////////////////////////////////////////////
          break;
        default:
          System.out.println("Connection accepted but no such query.");
      }
      os.close();
      br.close();
      connectionSocket.close();
    }
  }
  private static void obtainAndUpdatePredecessor() throws Exception {
    Socket askSuccessorServer = new Socket(InetAddress.getByName(successorIPAdress[0]), successorPort[0]);
    System.out.println("XXXXXXXXXXXXX obtainAndUpdatePredecessor() XXXXXXXXXXXXX");
    BufferedReader br = new BufferedReader(new InputStreamReader(askSuccessorServer.getInputStream()));
    OutputStream os = askSuccessorServer.getOutputStream();
    os.write("SendPredecessor\n".getBytes());
    os.flush();
    predecessor = br.readLine();
    os.close(); br.close();

    Socket updatePredecessorServer = new Socket(InetAddress.getByName(successorIPAdress[0]), successorPort[0]);
    System.out.println("XXXXXXXXXXXXX obtainAndUpdatePredecessor() XXXXXXXXXXXXX");
    br = new BufferedReader(new InputStreamReader(updatePredecessorServer.getInputStream()));
    os = updatePredecessorServer.getOutputStream();
    os.write("UpdatePredecessorIAmNew\n".getBytes());
    os.flush();
    predecessor = br.readLine();
    os.close(); br.close();

  }
  private static void updateSuccessor() throws Exception {

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
    myIPAdress = InetAddress.getLocalHost().getHostAddress().toString();
    System.out.println("Server for this peer is running at: " + myIPAdress + ":" + myPort);
  }
}

class MenuThread extends Thread {
  private void display() {
    System.out.println("Menu:\n1. IP Address and ID\n2. IP address and ID of the successor and predecessor");
    System.out.print("3. The file key IDs it contains\n4. Finger table\nEnter choice:");
  }
  public void run() {
    Scanner sc = new Scanner(System.in);
    while(true) {
      display();
      int x = sc.nextInt();
      switch(x) {
        case 1:
          System.out.println("IP Address: " + Peer.myIPAdress + ", Port: " + Peer.myPort + "\nID: " + Peer.myKey);
          break;
        case 2:
          try {
            System.out.println("Address of successor: " + Peer.successorIPAdress[0] + ":" + Peer.successorPort[0]);
            System.out.println("ID of successor: " + ObtainSHA.SHA1(Peer.successorIPAdress[0] + ":" + Peer.successorPort[0]));
            System.out.println("Address of predecessor: " + Peer.predecessor);
            System.out.println("ID of predecessor: " + ObtainSHA.SHA1(Peer.predecessor));
          } catch(Exception ex) {
            ex.printStackTrace();
          }
          break;
        case 3:
          File[] listOfFiles = (new File("./files")).listFiles();
          for (int i = 0; i < listOfFiles.length; i++)
            System.out.println(listOfFiles[i].getName());
          break;
        case 4:
          for(RowInFingerTable i: Peer.fingerTable) {
            String y = i.IPAddress + ":" + i.port;
            try {
              System.out.println("[" + i.startInterval + ", " + i.endInterval+") -> " + ObtainSHA.SHA1(y) + "(" + y + ")");
            } catch(Exception ex) {
              ex.printStackTrace();
            }
          }
          break;
        default:
          System.out.println("XXXXXXXXXXXXX Invalid Menu Item XXXXXXXXXXXXX");
      }
    }
  }
}
