package aosProject;
import java.io.*;
import java.util.*;

public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int toNode;
	private int fromNode;
	private Queue<Integer> sendList;
	private String type; 
	private String Content;
	private int value;
	
	public Message(int toNode, int fromNode, String type) {
		this.setToNode(toNode);
		this.setFromNode(fromNode);
		this.setValue(0);
		this.setContent("Dummy For now");
		this.setType("COMPLETE");
		this.setSendList(null);
	}
	
	public Message(Queue<Integer> sendList) {
		this.setToNode(Integer.MIN_VALUE);
		this.setFromNode(Integer.MIN_VALUE);
		this.setValue(0);
		this.setContent("Dummy For now");
		this.setType("TOKEN");
		this.setSendList(sendList);
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getToNode() {
		return toNode;
	}
	public void setToNode(int toNode) {
		this.toNode = toNode;
	}
	/**
	 * @return the fromNode
	 */
	public int getFromNode() {
		return fromNode;
	}

	/**
	 * @param fromNode the fromNode to set
	 */
	public void setFromNode(int fromNode) {
		this.fromNode = fromNode;
	}

	/**
	 * @return the sendList
	 */
	public Queue<Integer> getSendList() {
		return sendList;
	}

	/**
	 * @param sendList the sendList to set
	 */
	public void setSendList(Queue<Integer> sendList) {
		this.sendList = sendList;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return Content;
	}

	public void setContent(String content) {
		Content = content;
	}
}
