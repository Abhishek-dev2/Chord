// author: Abhishek Agrawal
// dateCreated: 08/03/2018

import java.util.*;
import java.io.*;
import java.net.*;

public class QueryProcessingThread extends Thread {
  private static void queryProcessing() throws Exception {
    ServerSocket queryProcessingServer = new ServerSocket(Peer.myPort, 0, InetAddress.getByName(Peer.myIPAdress));
    // System.out.println("[MAIN THREAD] Opened port for queryProcessing at machine: "+myKey+" at: "+myIPAdress+":"+myPort+".");
    while(true) {
      Socket connectionSocket = queryProcessingServer.accept();
      BufferedReader br = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
      OutputStream os = connectionSocket.getOutputStream();
      Thread T = new ClientThread(connectionSocket, br, os);
      T.start();
    }
  }
  public void run() {
    try {
      queryProcessing();
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
}
class ClientThread extends Thread {
  private Socket connectionSocket = null;
  private BufferedReader br;
  private OutputStream os;
  public ClientThread(Socket connectionSocket, BufferedReader br, OutputStream os) {
    this.connectionSocket = connectionSocket;
    this.br = br;
    this.os = os;
    // System.out.println("************** NEW CLIENT THREAD IS CREATED ****************");
  }
  public void run() {
    try {
      String request = br.readLine();
      // System.out.println("Query received: " + request);
      switch(request) {
        case "areYouThere":
          // System.out.println("************* YesIAmPresent *************");
          break;
        case "giveMyFiles":
          int yourKey = Integer.parseInt(br.readLine());
          File[] listOfFiles = (new File("./files")).listFiles();
          for (int i = 0; i < listOfFiles.length; i++) {
            int x = ObtainSHA.SHA1(listOfFiles[i].getName());
            if(yourKey == RowInFingerTable.clockwiseClosest(x, yourKey, Peer.myKey)) {
              os.write((listOfFiles[i].getName() + "\n").getBytes());
              os.flush();
              BufferedReader fileBR = new BufferedReader(new FileReader(listOfFiles[i]));
              String line = null;
              while ((line = fileBR.readLine()) != null)
                os.write((line + "\n").getBytes());
              os.write("*#*#*#*EOF*#*#*#*\n".getBytes());
              os.flush();
              listOfFiles[i].delete();
            }
          }
          os.write("#*#\n".getBytes());
          os.flush();
          break;
        case "SendFileAddress":
          int fileKey = Integer.parseInt(br.readLine());
          if(checkFileInMyFingerTable(fileKey)) {
            os.write((sendFileAddress(fileKey) + "\n").getBytes());
            os.flush();
          }
          else {
            String Addr = sendFileAddress(fileKey);
            // System.out.println("######################## " + Addr + " ###########################");
            String[] temp = Addr.split(":");
            Addr = SearchFile.returnFileAddress(fileKey, temp[0], Integer.parseInt(temp[1]));
            os.write((Addr + "\n").getBytes());
            os.flush();
          }
          break;
        case "SendFirstSuccessor":
          os.write((Peer.successorIPAdress[0] + ":" + Peer.successorPort[0] + "\n").getBytes());
          os.flush();
          break;
        case "SendPredecessor":
          os.write((Peer.predecessor + "\n").getBytes());
          os.flush();
          break;
        case "updateSuccessorIAmNew":
          Peer.predecessor = br.readLine();
          break;
        case "UpdatePredecessorIAmNew":
          String newPeerIP = br.readLine();
          String[] temp = newPeerIP.split(":");
          Peer.successorIPAdress[2] = Peer.successorIPAdress[1]; Peer.successorPort[2] = Peer.successorPort[1];
          Peer.successorIPAdress[1] = Peer.successorIPAdress[0]; Peer.successorPort[1] = Peer.successorPort[0];
          Peer.successorIPAdress[0] = temp[0]; Peer.successorPort[0] = Integer.parseInt(temp[1]);
          int newPeerKey = ObtainSHA.SHA1(newPeerIP);
          updateFingerTable(newPeerKey, temp[0], Peer.successorPort[0]);
          temp = Peer.predecessor.split(":");
          Socket updatePredecessorServer = new Socket(InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
          OutputStream os1 = updatePredecessorServer.getOutputStream();
          os1.write(("UpdatePredecessorIAmPredecessor\n" + 1 + "\n" + newPeerIP + "\n" + newPeerIP + "\n").getBytes());
          os1.flush();
          os1.close(); updatePredecessorServer.close();
          break;
        case "UpdatePredecessorIAmPredecessor":
          int num = Integer.parseInt(br.readLine());
          if(num == 1 || num == 2) {
            String mySuccessorIP = br.readLine();
            temp = mySuccessorIP.split(":");
            Peer.successorIPAdress[2] = Peer.successorIPAdress[1]; Peer.successorPort[2] = Peer.successorPort[1];
            Peer.successorIPAdress[num] = temp[0]; Peer.successorPort[num] = Integer.parseInt(temp[1]);
          }
          newPeerIP = br.readLine();
          temp = newPeerIP.split(":");
          newPeerKey = ObtainSHA.SHA1(newPeerIP);
          updateFingerTable(newPeerKey, temp[0], Integer.parseInt(temp[1]));
          if(num <= Math.round(Math.pow(2, Peer.m))) {
            temp = Peer.predecessor.split(":");
            updatePredecessorServer = new Socket(InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
            os1 = updatePredecessorServer.getOutputStream();
            os1.write(("UpdatePredecessorIAmPredecessor\n" + (num + 1) + "\n").getBytes());
            os1.flush();
            if(num == 1) {
              os1.write((newPeerIP + "\n").getBytes());
              os1.flush();
            }
            os1.write((newPeerIP + "\n").getBytes());
            os1.flush();
            os1.close(); updatePredecessorServer.close();
          }
          break;
          ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        case "UpdatePredecessorMySuccessorIsDead":
          String deadPeerReplacementSuccessorIP = br.readLine(); //
          temp = deadPeerReplacementSuccessorIP.split(":");
          String deadPeerIP = Peer.successorIPAdress[1] + ":" + Peer.successorPort[1];
          for(int i = 0;i < Peer.m;i++) {
            int k = ObtainSHA.SHA1(Peer.successorIPAdress[2] + ":" + Peer.successorPort[2]);
            if(Peer.fingerTable[i].key == ObtainSHA.SHA1(Peer.successorIPAdress[1] + ":" + Peer.successorPort[1])) {
              Peer.fingerTable[i].IPAddress = Peer.successorIPAdress[2];
              Peer.fingerTable[i].port = Peer.successorPort[2];
              Peer.fingerTable[i].key = k;
            }
          }
          Peer.successorIPAdress[1] = Peer.successorIPAdress[2]; Peer.successorPort[1] = Peer.successorPort[2];
          Peer.successorIPAdress[2] = temp[0]; Peer.successorPort[2] = Integer.parseInt(temp[1]);
          temp = Peer.predecessor.split(":");
          updatePredecessorServer = new Socket(InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
          os1 = updatePredecessorServer.getOutputStream();
          String deadPeerSuccessorIP = Peer.successorIPAdress[1] + ":" + Peer.successorPort[1];
          os1.write(("UpdatePredecessorPredecessorMySuccessorIsDead\n" + 1 + "\n" + deadPeerIP + "\n" + deadPeerSuccessorIP + "\n" + Peer.successorIPAdress[1] + ":" + Peer.successorPort[1] + "\n").getBytes());
          os1.flush();
          os1.close(); updatePredecessorServer.close();
          break;
        case "UpdatePredecessorPredecessorMySuccessorIsDead":
          num = Integer.parseInt(br.readLine());
          deadPeerIP = br.readLine();
          String deadPeerReplacementIP = br.readLine();
          String replacementSuccessor2 = br.readLine();
          temp = replacementSuccessor2.split(":");
          Peer.successorIPAdress[2] = temp[0]; Peer.successorPort[2] = Integer.parseInt(temp[1]);
          for(int i = 0;i < Peer.m;i++) {
            int k = ObtainSHA.SHA1(deadPeerReplacementIP);
            temp = deadPeerReplacementIP.split(":");
            if(Peer.fingerTable[i].key == ObtainSHA.SHA1(deadPeerIP)) {
              Peer.fingerTable[i].IPAddress = temp[0];
              Peer.fingerTable[i].port = Integer.parseInt(temp[1]);
              Peer.fingerTable[i].key = k;
            }
          }
          if(num <= Math.round(Math.pow(2, Peer.m))) {
            temp = Peer.predecessor.split(":");
            updatePredecessorServer = new Socket(InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
            os1 = updatePredecessorServer.getOutputStream();
            os1.write(("UpdatePredecessorPredecessorMySuccessorIsDead\n" + (num + 1) + "\n" + deadPeerIP + "\n" + deadPeerReplacementIP + "\n" + Peer.successorIPAdress[1] + ":" + Peer.successorPort[1] + "\n").getBytes());
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
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  private static String sendFileAddress(int fileKey) throws Exception {
    int start, end;
    if(fileKey == Peer.myKey)
      return (Peer.myIPAdress + ":" + Peer.myPort);
    for(int i = 0;i < Peer.m;i++) {
      start = Peer.fingerTable[i].startInterval;
      end = Peer.fingerTable[i].endInterval;
      if(RowInFingerTable.insideInterval(fileKey, start, end))
        return (Peer.fingerTable[i].IPAddress + ":" + Peer.fingerTable[i].port);
    }
    return "";
  }
  private static void updateFingerTable(int key, String IPAddress, int port) {
    int start, end;
    for(int i = 0;i < Peer.m;i++) {
      start = Peer.fingerTable[i].startInterval;
      end = Peer.fingerTable[i].endInterval;
      if(RowInFingerTable.clockwiseClosest(start, key, Peer.fingerTable[i].key) == key) {
        Peer.fingerTable[i].key = key;
        Peer.fingerTable[i].IPAddress = IPAddress;
        Peer.fingerTable[i].port = port;
      }
    }
  }
  private static void updateFingerTableAfterDelete(int key, String IPAddress, int port) {
    int start, end;
    for(int i = 0;i < Peer.m;i++) {
      start = Peer.fingerTable[i].startInterval;
      end = Peer.fingerTable[i].endInterval;
      if(RowInFingerTable.clockwiseClosest(start, key, Peer.fingerTable[i].key) == key) {
        Peer.fingerTable[i].key = key;
        Peer.fingerTable[i].IPAddress = IPAddress;
        Peer.fingerTable[i].port = port;
      }
    }
  }
  private static boolean checkFileInMyFingerTable(int fileKey) throws Exception {
    int start, end;
    for(int i = 0;i < Peer.m;i++) {
      start = Peer.fingerTable[i].startInterval;
      end = Peer.fingerTable[i].endInterval;
      if(RowInFingerTable.insideInterval(fileKey, start, end))
        if(RowInFingerTable.insideInterval(fileKey, start, Peer.fingerTable[i].key))
          return true;
    }
    return false;
  }
}
