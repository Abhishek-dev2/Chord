// author: Abhishek Agrawal
// dateCreated: 01/03/2018

import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {
  public static boolean firstNode = false;
  public static String myIPAdress, peerIPAdress;
  public static int myPort, myKey, m = 8, peerPort;
  public static RowInFingerTable[] fingerTable = new RowInFingerTable[m];
  public static String predecessor = "";
  public static String[] successorIPAdress = new String[3];
  public static int[] successorPort = new int[3];
  public static void main(String[] args) throws Exception {
    getPeerInfo();
    myKey = ObtainSHA.SHA1(myIPAdress + ":" + myPort);
    System.out.println("key: " + myKey);
    Thread t1 = new QueryProcessingThread();
    t1.start();
    if(!firstNode) {
      System.out.println("\n=== Creating finger table ===");
      createFingerTable();
      Thread.sleep(2000);
      System.out.println("\n=== Finding first successors ===");
      successorIPAdress[0] = fingerTable[0].IPAddress; successorPort[0] = fingerTable[0].port;
      System.out.println("\n=== Updating predecessor of immediate successor ===");
      updateSuccessor(true);
      Thread.sleep(2000);
      System.out.println("\n=== Updating successors and finger table of predecessors ===");
      updatePredecessors();
      Thread.sleep(2000);
      System.out.println("\n=== Finding other successors ===");
      String[] temp = SearchSuccessor.returnSuccessor(successorIPAdress[0], successorPort[0]).split(":");
      successorIPAdress[1] = temp[0]; successorPort[1] = Integer.parseInt(temp[1]);
      temp = SearchSuccessor.returnSuccessor(successorIPAdress[1], successorPort[1]).split(":");
      successorIPAdress[2] = temp[0]; successorPort[2] = Integer.parseInt(temp[1]);
      Thread.sleep(2000);
      System.out.println("\n=== Receiving my files from my successor ===");
      getMyFiles();
      Thread.sleep(2000);
    }
    Thread t2 = new StabilizationThread();
    t2.start();
    MainMenu.run();
  }
  private static void getMyFiles() throws Exception {
    Socket getMyFilesServer = new Socket(InetAddress.getByName(successorIPAdress[0]), successorPort[0]);
    // System.out.println("XXXXXXXXXXXXX getMyFiles() XXXXXXXXXXXXX");
    BufferedReader br = new BufferedReader(new InputStreamReader(getMyFilesServer.getInputStream()));
    OutputStream os = getMyFilesServer.getOutputStream();
    os.write(("giveMyFiles\n" + myKey + "\n").getBytes());
    os.flush();
    String fileName = br.readLine();
    while(!fileName.equals("#*#")) {
      File file = new File("./files/" + fileName);
      file.createNewFile();
      fileName = br.readLine();
    }
    os.close(); br.close(); getMyFilesServer.close();
  }
  public static void updatePredecessors() throws Exception {
    String[] temp = predecessor.split(":");
    Socket updatePredecessorServer = new Socket(InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
    // System.out.println("XXXXXXXXXXXXX updatePredecessors() XXXXXXXXXXXXX");
    OutputStream os = updatePredecessorServer.getOutputStream();
    os.write(("UpdatePredecessorIAmNew\n" + myIPAdress + ":" + myPort + "\n").getBytes());
    os.flush();
    os.close(); updatePredecessorServer.close();
  }
  public static void updatePredecessorsAfterDelete() throws Exception {
    String[] temp = predecessor.split(":");
    Socket updatePredecessorServer = new Socket(InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
    // System.out.println("XXXXXXXXXXXXX updatePredecessors() XXXXXXXXXXXXX");
    OutputStream os = updatePredecessorServer.getOutputStream();
    os.write(("UpdatePredecessorMySuccessorIsDead\n" + successorIPAdress[1] + ":" + successorPort[1] + "\n").getBytes());
    os.flush();
    os.close(); updatePredecessorServer.close();
  }
  public static void updateSuccessor(boolean beingCreated) throws Exception {
    OutputStream os = null;
    BufferedReader br = null;
    if(beingCreated) {
      Socket askSuccessorServer = new Socket(InetAddress.getByName(successorIPAdress[0]), successorPort[0]);
      // System.out.println("XXXXXXXXXXXXX updateSuccessor() XXXXXXXXXXXXX");
      br = new BufferedReader(new InputStreamReader(askSuccessorServer.getInputStream()));
      os = askSuccessorServer.getOutputStream();
      os.write("SendPredecessor\n".getBytes());
      os.flush();
      predecessor = br.readLine();
      os.close(); br.close(); askSuccessorServer.close();
    }

    Socket updatePredecessorServer = new Socket(InetAddress.getByName(successorIPAdress[0]), successorPort[0]);
    // System.out.println("XXXXXXXXXXXXX updateSuccessor() XXXXXXXXXXXXX");
    os = updatePredecessorServer.getOutputStream();
    os.write(("updateSuccessorIAmNew\n" + myIPAdress + ":" + myPort + "\n").getBytes());
    os.flush();
    os.close(); updatePredecessorServer.close();
  }
  private static void createFingerTable() throws Exception {
    int start = myKey + 1, end;
    start = (int)(start % Math.round(Math.pow(2, m)));
    for(int i = 0;i < m;i++) {
      String succ = SearchFile.returnFileAddress(start, peerIPAdress, peerPort);
      end = (int)((start + Math.round(Math.pow(2, i))) % Math.round(Math.pow(2, m)));
      String[] temp = succ.split(":");
      fingerTable[i] = new RowInFingerTable(start, end, Integer.parseInt(temp[1]), temp[0]);
      start = end;
    }
  }
  private static void getPeerInfo() throws Exception {
    Scanner sc = new Scanner(System.in);
    Random rand = new Random();
    System.out.print("Is this the first instance?(y/n) ");
    switch(sc.nextLine()) {
      case "y":
        firstNode = true;
        // myPort = rand.nextInt(65536);
        // if(myPort <= 1024)
        //   myPort += 1024;
        // System.out.print("Enter my port: ");
        // myPort = sc.nextInt();
        myPort = 1234;
        myIPAdress = InetAddress.getLocalHost().getHostAddress().toString();
        peerIPAdress = InetAddress.getLocalHost().getHostAddress().toString();
        peerPort = myPort;
        myKey = ObtainSHA.SHA1(myIPAdress + ":" + myPort);
        int start = myKey + 1, end;
        start = (int)(start % Math.round(Math.pow(2, m)));
        for(int i = 0;i < m;i++) {
          end = (int)((start + Math.round(Math.pow(2, i))) % Math.round(Math.pow(2, m)));
          fingerTable[i] = new RowInFingerTable(start, end, myPort, myIPAdress);
          start = end;
        }
        predecessor = myIPAdress + ":" + myPort;
        for(int i = 0;i < 3;i++) {
          successorIPAdress[i] = myIPAdress;
          successorPort[i] = myPort;
        }
        break;
      case "n":
        firstNode = false;
        // System.out.print("I need a IP Address and port of a peer(node) already running.\nPeer IP Address: ");
        // peerIPAdress = sc.nextLine();
        // System.out.print("Peer Port: ");
        // peerPort = sc.nextInt();
        peerIPAdress = InetAddress.getLocalHost().getHostAddress().toString();
        peerPort = 1234;
        // myPort = rand.nextInt(65536);
        // if(myPort <= 1024)
        //   myPort += 1024;
        myPort = 2345;
        // System.out.print("Enter my port: ");
        // myPort = sc.nextInt();
        break;
      default:
        System.out.println("BC");
        System.exit(0);
    }
    myIPAdress = InetAddress.getLocalHost().getHostAddress().toString();
    System.out.println("Server for this peer is running at: " + myIPAdress + ":" + myPort);
  }
}
