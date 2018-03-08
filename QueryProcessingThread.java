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
      String request = br.readLine();
      // System.out.println("Query received: " + request);
      switch(request) {
        case "giveMyFiles":
          int yourKey = Integer.parseInt(br.readLine());
          File[] listOfFiles = (new File("./files")).listFiles();
          try {
            for (int i = 0; i < listOfFiles.length; i++) {
              int x = ObtainSHA.SHA1(listOfFiles[i].getName());
              if(yourKey == RowInFingerTable.clockwiseClosest(x, yourKey, Peer.myKey)) {
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
          if(Peer.checkFileInMyFingerTable(fileKey)) {
            os.write((Peer.sendFileAddress(fileKey) + "\n").getBytes());
            os.flush();
          } else {
            String Addr = Peer.sendFileAddress(fileKey);
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
          Peer.updateFingerTable(newPeerKey, temp[0], Peer.successorPort[0]);
          temp = Peer.predecessor.split(":");
          Socket updatePredecessorServer = new Socket(InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
          OutputStream os1 = updatePredecessorServer.getOutputStream();
          os1.write(("UpdatePredecessorIAmPredecessor\n" + 1 + "\n" + Peer.myIPAdress + ":" + Peer.myPort + "\n" + newPeerIP + "\n").getBytes());
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
          if(num == 1)
            Peer.successorIPAdress[2] = Peer.successorIPAdress[1]; Peer.successorPort[2] = Peer.successorPort[1];
          newPeerIP = br.readLine();
          temp = newPeerIP.split(":");
          newPeerKey = ObtainSHA.SHA1(newPeerIP);
          Peer.updateFingerTable(newPeerKey, temp[0], Integer.parseInt(temp[1]));
          if(num <= Math.round(Math.pow(2, Peer.m))) {
            temp = Peer.predecessor.split(":");
            updatePredecessorServer = new Socket(InetAddress.getByName(temp[0]), Integer.parseInt(temp[1]));
            os1 = updatePredecessorServer.getOutputStream();
            os1.write(("UpdatePredecessorIAmPredecessor\n" + (num + 1) + "\n").getBytes());
            os1.flush();
            if(num == 1) {
              os1.write((Peer.myIPAdress + ":" + Peer.myPort + "\n").getBytes());
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
  public void run() {
    try {
      queryProcessing();
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
}
