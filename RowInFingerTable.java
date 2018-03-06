// author: Abhishek Agrawal
// dateCreated: 06/03/2018

import java.io.*;
import java.net.*;
import java.util.*;

public class RowInFingerTable {
  public int startInterval, endInterval, port;
  public String IPAddress;
  public RowInFingerTable(int startInterval, int endInterval, int port, String IPAddress) {
    this.startInterval = startInterval;
    this.endInterval = endInterval;
    this.port = port;
    this.IPAddress = IPAddress;
  }
}
