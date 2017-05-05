package aosProject;

import java.util.*;

public class MessageInfo {
	private int startNodeId; 
	private int[] sendList;
	
	public Message convertToMessage() {
		Queue<Integer> temp = new LinkedList<Integer>();
		for (int i=0; i< this.getSendList().length; i++){
			temp.add(this.sendList[i]);
		}
		temp.add(this.startNodeId);
		return (new Message(temp));
	}
	public MessageInfo(int startNodeId, int[] sendList){
		this.setStartNodeId(startNodeId);
		this.setSendList(sendList);
	}

	public int getStartNodeId() {
		return startNodeId;
	}

	public void setStartNodeId(int startNodeId) {
		this.startNodeId = startNodeId;
	}

	public int[] getSendList() {
		return sendList;
	}

	public void setSendList(int[] sendList) {
		this.sendList = sendList;
	}
}
