package aosProject;

import java.io.IOException;
import java.net.*;

public class Connections {
	private int toNodeId; 
	private Socket toSocket; 
	
	public Connections(NodeInfo nodeInfo) {
		this.setToNodeId(nodeInfo.getId());
		try {
			this.toSocket = new Socket (nodeInfo.getHostname(), nodeInfo.getPortId());
			this.toSocket.setSoTimeout(0);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getToNodeId() {
		return toNodeId;
	}

	public void setToNodeId(int toNodeId) {
		this.toNodeId = toNodeId;
	}

	public Socket getToSocket() {
		return toSocket;
	}

	public void setToSocket(Socket toSocket) {
		this.toSocket = toSocket;
	}
}
