// author: Abhishek Agrawal
// dateCreated: 06/03/2018

import java.io.*;
import java.net.*;
import java.util.*;

public class RowInFingerTable {
  public int startInterval, endInterval, port, key;
  public String IPAddress;
  public RowInFingerTable(int startInterval, int endInterval, int port, String IPAddress) throws Exception {
    this.startInterval = startInterval;
    this.endInterval = endInterval;
    this.port = port;
    this.IPAddress = IPAddress;
    this.key = ObtainSHA.SHA1(IPAddress + ":" + port);
  }
  public static int clockwiseClosest(int from, int first, int second) {
    boolean f = false, s = false;
    if(from <= first && first < Math.round(Math.pow(2, Peer.m)))
      f = true;
    if(from <= second && second < Math.round(Math.pow(2, Peer.m)))
      s = true;
    if(f && s)
      return Math.min(first, second);
    else if(!f && !s)
      return Math.min(first, second);
    else
      return Math.max(first, second);
  }
  public static boolean insideInterval(int key, int closedStart, int openEnd) {
    if(closedStart < openEnd && closedStart <= key && key < openEnd)
      return true;
    else if(closedStart < openEnd)
      return false;
    else {
      int i = closedStart;
      while(i < Math.round(Math.pow(2, Peer.m))) {
        if(key == i)
          return true;
        i++;
      }
      i = 0;
      while(i < openEnd) {
        if(key == i)
          return true;
        i++;
      }
      return false;
    }
  }
}
