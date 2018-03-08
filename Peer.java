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
    turnServerOn();
    myKey = ObtainSHA.SHA1(myIPAdress + ":" + myPort);
    System.out.println("key: " + myKey);
    Thread t1 = new queryProcessingThread();
    t1.start();
    if(!firstNode) {
      System.out.println("\n=== Creating finger table ===");
      Thread.sleep(2000);
      createFingerTable();
      System.out.println("\n=== Finding three successors ===");
      Thread.sleep(2000);
      successorIPAdress[0] = fingerTable[0].IPAddress; successorPort[0] = fingerTable[0].port;
      String[] temp = SearchSuccessor.returnSuccessor(successorIPAdress[0], successorPort[0]).split(":");
      successorIPAdress[1] = temp[0]; successorPort[1] = Integer.parseInt(temp[1]);
      temp = SearchSuccessor.returnSuccessor(successorIPAdress[1], successorPort[1]).split(":");
      successorIPAdress[2] = temp[0]; successorPort[2] = Integer.parseInt(temp[1]);
      System.out.println("\n=== Updating predecessor of immediate successor ===");
      Thread.sleep(2000);
      updateSuccessor();
      System.out.println("\n=== Updating successors and finger table of predecessors ===");
      Thread.sleep(2000);
      updatePredecessors();
      System.out.println("\n=== Receiving my files from my successor ===");
      Thread.sleep(2000);
      getMyFiles();
    }
    Thread t2 = new MenuThread();
    t2.start();
  }
  public static void queryProcessing() throws Exception {
    ServerSocket queryProcessingServer = new ServerSocket(myPort, 0, InetAddress.getByName(myIPAdress));
    // System.out.println("[MAIN THREAD] Opened port for queryProcessing at machine: "+myKey+" at: "+myIPAdress+":"+myPort+".");
    while(true) {
      Socket connectionSocket = queryProcessingServer.accept();
      BufferedReader br = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
      OutputStream os = connectionSocket.getOutputStream();
      String request = br.readLine();
      System.out.println("Query received: " + request);
      switch(request) {
        case "giveMyFiles":
          int yourKey = Integer.parseInt(br.readLine());
          File[] listOfFiles = (new File("./files")).listFiles();
          try {
            for (int i = 0; i < listOfFiles.length; i++) {
              int x = ObtainSHA.SHA1(listOfFiles[i].getName());
              if(yourKey == RowInFingerTable.clockwiseClosest(x, yourKey, myKey)) {
                os.write((listOfFiles[i].getName() + "\n").getBytes());
                os.flush();
                listOfFiles[i].delete();
              }
            }
          } catch(Exception ex) {
            ex.printStackTrace();
          }
          os.write("#*#\n".getBytes());
          os.flush();
          break;
        case "SendFileAddress":
          int fileKey = Integer.parseInt(br.readLine());
          if(checkFileInMyFingerTable(fileKey)) {
            os.write((sendFileAddress(fileKey) + "\n").getBytes());
            os.flush();
          } else {
            String Addr = sendFileAddress(fileKey);
            // System.out.println("######################## " + Addr + " ###########################");
            String[] temp = Addr.split(":");
            Addr = SearchFile.returnFileAddress(fileKey, temp[0], Integer.parseInt(temp[1]));
            os.write((Addr + "\n").getBytes());
            os.flush();
          }
          break;
        case "SendFirstSuccessor":
          os.write((successorIPAdress[0] + ":" + successorPort[0] + "\n").getBytes());
          os.flush();
          break;
        case "SendPredecessor":
          os.write((predecessor + "\n").getBytes());
          os.flush();
          break;
        case "updateSuccessorIAmNew":
          predecessor = br.readLine();
          break;
        case "UpdatePredecessorIAmNew":
          String newPeerIP = br.readLine();
          String[] temp = newPeerIP.split(":");
          successorIPAdress[2] = successorIPAdress[1]; successorPort[2] = successorPort[1];
          successorIPAdress[1] = successorIPAdress[0]; successorPort[1] = successorPort[0];
          successorIPAdress[0] = temp[0]; successorPort[0] = Integer.parseInt(temp[1]);
          int newPeerKey = ObtainSHA.SHA1(newPeerIP);
          updateFingerTable(newPeerKey, temp[0], successorPort[0]);
          temp = predecessor.split(":");
          Socket updatePredecessorServer = new Socket(InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
          OutputStream os1 = updatePredecessorServer.getOutputStream();
          os1.write("UpdatePredecessorIAmPredecessor\n".getBytes());
          os1.flush();
          os1.write((1 + "\n").getBytes());
          os1.flush();
          os1.write((myIPAdress + ":" + myPort + "\n").getBytes());
          os1.flush();
          os1.write((newPeerIP + "\n").getBytes());
          os1.flush();
          os1.close(); updatePredecessorServer.close();
          break;
        case "UpdatePredecessorIAmPredecessor":
          int num = Integer.parseInt(br.readLine());
          if(num == 1) {
            String mySuccessorIP = br.readLine();
            temp = mySuccessorIP.split(":");
            successorIPAdress[2] = successorIPAdress[1]; successorPort[2] = successorPort[1];
            successorIPAdress[1] = temp[0]; successorPort[1] = Integer.parseInt(temp[1]);
          }
          else if(num == 2) {
            String mySuccessorIP = br.readLine();
            temp = mySuccessorIP.split(":");
            successorIPAdress[2] = temp[0]; successorPort[2] = Integer.parseInt(temp[1]);
          }
          newPeerIP = br.readLine();
          temp = newPeerIP.split(":");
          newPeerKey = ObtainSHA.SHA1(newPeerIP);
          updateFingerTable(newPeerKey, temp[0], Integer.parseInt(temp[1]));
          if(num <= m) {
            temp = predecessor.split(":");
            updatePredecessorServer = new Socket(InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
            os1 = updatePredecessorServer.getOutputStream();
            os1.write("UpdatePredecessorIAmPredecessor\n".getBytes());
            os1.flush();
            os1.write(((num + 1) + "\n").getBytes());
            os1.flush();
            if(num == 1) {
              os1.write((myIPAdress + ":" + myPort + "\n").getBytes());
              os1.flush();
            }
            os1.write((newPeerIP + "\n").getBytes());
            os1.flush();
            os1.close(); updatePredecessorServer.close();
          }
          break;
        default:
          System.out.println("Connection accepted but no such query.");
      }
      os.close();
      br.close();
      connectionSocket.close();
    }
  }
  private static void getMyFiles() throws Exception {
    Socket getMyFilesServer = new Socket(InetAddress.getByName(successorIPAdress[0]), successorPort[0]);
    System.out.println("XXXXXXXXXXXXX getMyFiles() XXXXXXXXXXXXX");
    BufferedReader br = new BufferedReader(new InputStreamReader(getMyFilesServer.getInputStream()));
    OutputStream os = getMyFilesServer.getOutputStream();
    os.write(("giveMyFiles\n").getBytes());
    os.flush();
    os.write((myKey + "\n").getBytes());
    os.flush();
    String fileName = br.readLine();
    while(!fileName.equals("#*#")) {
      File file = new File("./files/" + fileName);
      file.createNewFile();
      fileName = br.readLine();
    }
    os.close(); br.close(); getMyFilesServer.close();
  }
  private static boolean checkFileInMyFingerTable(int fileKey) throws Exception {
    int start, end;
    for(int i = 0;i < m;i++) {
      start = fingerTable[i].startInterval;
      end = fingerTable[i].endInterval;
      if(RowInFingerTable.insideInterval(fileKey, start, end))
        if(RowInFingerTable.insideInterval(fileKey, start, fingerTable[i].key))
          return true;
    }
    return false;
  }
  private static String sendFileAddress(int fileKey) throws Exception {
    int start, end;
    if(fileKey == myKey)
      return (myIPAdress + ":" + myPort);
    for(int i = 0;i < m;i++) {
      start = fingerTable[i].startInterval;
      end = fingerTable[i].endInterval;
      if(RowInFingerTable.insideInterval(fileKey, start, end))
        return (fingerTable[i].IPAddress + ":" + fingerTable[i].port);
    }
    return "";
  }
  private static void updatePredecessors() throws Exception {
    String[] temp = predecessor.split(":");
    Socket updatePredecessorServer = new Socket(InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
    System.out.println("XXXXXXXXXXXXX updatePredecessors() XXXXXXXXXXXXX");
    OutputStream os = updatePredecessorServer.getOutputStream();
    os.write("UpdatePredecessorIAmNew\n".getBytes());
    os.flush();
    os.write((myIPAdress + ":" + myPort + "\n").getBytes());
    os.flush();
    os.close(); updatePredecessorServer.close();
  }
  private static void updateSuccessor() throws Exception {
    Socket askSuccessorServer = new Socket(InetAddress.getByName(successorIPAdress[0]), successorPort[0]);
    System.out.println("XXXXXXXXXXXXX updateSuccessor() XXXXXXXXXXXXX");
    BufferedReader br = new BufferedReader(new InputStreamReader(askSuccessorServer.getInputStream()));
    OutputStream os = askSuccessorServer.getOutputStream();
    os.write("SendPredecessor\n".getBytes());
    os.flush();
    predecessor = br.readLine();
    os.close(); br.close(); askSuccessorServer.close();

    Socket updatePredecessorServer = new Socket(InetAddress.getByName(successorIPAdress[0]), successorPort[0]);
    System.out.println("XXXXXXXXXXXXX updateSuccessor() XXXXXXXXXXXXX");
    os = updatePredecessorServer.getOutputStream();
    os.write("updateSuccessorIAmNew\n".getBytes());
    os.flush();
    os.write((myIPAdress + ":" + myPort + "\n").getBytes());
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
  private static void updateFingerTable(int key, String IPAddress, int port) {
    int start, end;
    for(int i = 0;i < m;i++) {
      start = fingerTable[i].startInterval;
      end = fingerTable[i].endInterval;
      if(RowInFingerTable.clockwiseClosest(start, key, fingerTable[i].key) == key) {
        fingerTable[i].key = key;
        fingerTable[i].IPAddress = IPAddress;
        fingerTable[i].port = port;
      }
    }
  }
  private static void turnServerOn() throws Exception {
    Scanner sc = new Scanner(System.in);
    Random rand = new Random();
    System.out.print("Is this the first instance?(y/n) ");
    switch(sc.nextLine()) {
      case "y":
        firstNode = true;
        // myPort = rand.nextInt(65536);
        // if(myPort <= 1024)
        //   myPort += 1024;
        System.out.print("Enter my port: ");
        myPort = sc.nextInt();
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
        System.out.print("I need a IP Address and port of a peer(node) already running.\nPeer IP Address: ");
        peerIPAdress = sc.nextLine();
        System.out.print("Peer Port: ");
        peerPort = sc.nextInt();
        // myPort = rand.nextInt(65536);
        // if(myPort <= 1024)
        //   myPort += 1024;
        System.out.print("Enter my port: ");
        myPort = sc.nextInt();
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
    System.out.println("\nMenu:\n1. IP Address and ID\n2. IP address and ID of the successor and predecessor");
    System.out.print("3. The file key IDs it contains\n4. Finger table\nEnter choice: ");
  }
  public void run() {
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
class queryProcessingThread extends Thread {
  public void run() {
    try {
      Peer.queryProcessing();
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
}
