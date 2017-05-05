package aosProject;
import java.io.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Node implements Runnable{
	public final Runnable startListening;
	public final Runnable startSending;
	private int id;
	private int totalNumberOfNodes;
	private int label;
	private HashMap<Integer,Socket> connection;
	private ServerSocket serverSocket;
	private boolean upStatus;
	private Node createdByNode;
	private WriteToFile outputFile;
	private ArrayList<Connections> connectionList;
	private BlockingQueue<Message> msgQ;
	private ConfigFileReader conf;
	private int numberOfCompleteMessagesReceived;
	public int getNumberOfCompleteMessagesReceived() {
		return numberOfCompleteMessagesReceived;
	}
	public void setNumberOfCompleteMessagesReceived(int numberOfCompleteMessagesReceived) {
		this.numberOfCompleteMessagesReceived = numberOfCompleteMessagesReceived;
	}
	public int getNumberOfMessagesAtOrigin() {
		return numberOfMessagesAtOrigin;
	}
	public void setNumberOfMessagesAtOrigin(int numberOfMessagesAtOrigin) {
		this.numberOfMessagesAtOrigin = numberOfMessagesAtOrigin;
	}
	public int getNumberOfCompletedMessagesAtOrigin() {
		return numberOfCompletedMessagesAtOrigin;
	}
	public void setNumberOfCompletedMessagesAtOrigin(int numberOfCompletedMessagesAtOrigin) {
		this.numberOfCompletedMessagesAtOrigin = numberOfCompletedMessagesAtOrigin;
	}
	private int numberOfMessagesAtOrigin;
	private int numberOfCompletedMessagesAtOrigin;
	public int getNodeId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isUpStatus() {
		return upStatus;
	}
	public void setUpStatus(boolean upStatus) {
		this.upStatus = upStatus;
	}
	public Node getCreatedByNode() {
		return createdByNode;
	}
	public void setCreatedByNode(Node createdByNode) {
		this.createdByNode = createdByNode;
	}
	public HashMap<Integer,Socket> getConnectionDeferred() {
		return connection;
	}
	public void setConnection(HashMap<Integer,Socket> connection) {
		this.connection = connection;
	}
	
	public int randomNumber(int startingRange, int endingRange) {
		Random random = new Random();
		int retVal = random.nextInt(endingRange)+startingRange;
			
		return retVal;
	}
	
	//started by the master
	public Node(int givenId) {
		ConfigFileReader myreader = new ConfigFileReader("/home/011/m/mx/mxk145330/aos/config/config.txt");
		//ConfigFileReader myreader = new ConfigFileReader("config.txt");
		this.conf = myreader;
		setTotalNumberOfNodes(myreader.getNodeInfo().size());
		connectionList = new ArrayList<Connections>();
		msgQ = new ArrayBlockingQueue<Message>(100);
		//find this nodes ID
		for (NodeInfo temp: myreader.getNodeInfo()) {
			try {
				InetAddress gotAddress;
				InetAddress localAddress;
				gotAddress = InetAddress.getByName(temp.getHostname());
				localAddress = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
				if (temp.getId()==givenId) {
					this.setId(temp.getId());
					temp.setAvailable(false);
					System.out.println("set the node's id to "+id);
					this.setOutputFile(new WriteToFile("outputFile", this.getNodeId()));
					this.getOutputFile().write("This hostname is "+temp.getHostname());
					// start listening on port of this server
					this.getOutputFile().write("Now starting this server");
					this.serverSocket = new ServerSocket(temp.getPortId());
					this.serverSocket.setSoTimeout(0); //listen forever
				}
			} catch (IOException e) {
				System.out.println("!!!!!!!SERVER START EXCEPTION!!!!!");
				// TODO Auto-generated catch block
				this.getOutputFile().write(e.toString());
			} 
		}
		this.label = this.randomNumber(1, 10);
		this.createdByNode = this;
		this.getOutputFile().write("Loading my message queue if i am the sender");

		this.numberOfMessagesAtOrigin = this.loadMsgQ(myreader);
		this.numberOfCompleteMessagesReceived = 0;
		this.numberOfCompletedMessagesAtOrigin = 0;
		this.startListening = new Runnable() {
			public void run(){
				Node.this.run();
			}
		};
		this.startSending = new Runnable() {
			public void run(){
				Node.this.sender();
			}
		};
		this.getOutputFile().write("*************NODE INITIALIZATION COMPLETED FOR NODE - "+this.getNodeId()+" caluclated label as - "+this.getLabel());
	}
//	public void startReceiving(){
//		Thread rcvThread = new Thread();
//		rcvThread.start();
//	}
//	public void startSending() {
//		Thread rcvHandler = new Thread ( 
//				new Runnable() {
//					public void run(){
//						sender();
//					}
//				}
//		);
//		rcvHandler.start();
//	}
	public static void main(String[] args) throws InterruptedException{
			
			Node thisNode = new Node(Integer.parseInt(args[0]));
			System.out.println("got parameter "+args[0]);
			ArrayList<Thread> listenerThreads =  new ArrayList<Thread>();
			thisNode.getOutputFile().write("THIS IS A TEST OUTPUT");
			for (int i =0; i<thisNode.getTotalNumberOfNodes(); i++) {
				Thread temp = new Thread(thisNode.startListening);
				listenerThreads.add(temp);
				temp.start();
			}
			Thread sendThread = new Thread(thisNode.startSending);
			sendThread.start();
			while (true) {
				try {
					if (thisNode.getNumberOfCompletedMessagesAtOrigin() == thisNode.getNumberOfMessagesAtOrigin() && 
										thisNode.getNumberOfCompleteMessagesReceived() == thisNode.getTotalNumberOfNodes()-1) {
						break;
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			thisNode.getOutputFile().write("number of completed messages at origin is "+thisNode.getNumberOfCompletedMessagesAtOrigin());
			thisNode.getOutputFile().write("number of  messages at origin is "+thisNode.getNumberOfMessagesAtOrigin());
			thisNode.getOutputFile().write("number of complete messages received is "+thisNode.getNumberOfCompleteMessagesReceived());
			thisNode.getOutputFile().write("number of total number of Nodes is "+thisNode.getTotalNumberOfNodes());
			sendThread.interrupt();
			sendThread.join();
			for (Thread temp: listenerThreads) {
				thisNode.getOutputFile().write("NOW KILLING THREAD  "+temp.getId());
				temp.interrupt();
				temp.join();
			}
	}
	public int loadMsgQ(ConfigFileReader confReader) {
		int i = 0;
		for (MessageInfo temp :confReader.getMessageInfo()) {
			if (temp.getStartNodeId() == this.getNodeId()) {
				i++;
				Message addMessage = temp.convertToMessage();
				this.getOutputFile().write("Message contains:"+addMessage.getSendList().size());
				this.msgQ.add(addMessage);
				this.getOutputFile().write("Completed loading message number"+i);
			}
		}
		this.getOutputFile().write("Total number of messages loaded"+i);
		return i;
	}
	public WriteToFile getOutputFile() {
		return outputFile;
	}
	public void setOutputFile(WriteToFile outputFile) {
		this.outputFile = outputFile;
	}
	public boolean ProcessMessage(Message msg) {
		//TODO : handle the case of complete message
		
		//remove element from head of the queue and add that as to Node
		//change from Node 
		
		this.outputFile.write("Processing message from node with id "+msg.getFromNode());
		if (msg.getType().equals("COMPLETE") && msg.getFromNode()==this.getNodeId()) {
			return false;
		}
		if (msg.getType().equals("COMPLETE")) {
			this.getOutputFile().write("^^^^^^^^^^^^^^^^^^Received a COMPLETE message^^^^^^^^^^^^^^^^^^");
			this.setNumberOfCompleteMessagesReceived(this.getNumberOfCompleteMessagesReceived()+1);
			return true;
		}
		msg.setValue(msg.getValue()+this.getLabel());
		if (msg.getSendList().isEmpty()) {
			this.outputFile.write("########MESSAGE COMPLETED THE ROUTE at Node - "+this.getNodeId());
			this.outputFile.write("########PROCESSED WITH VALUE - "+msg.getValue());
			this.setNumberOfCompletedMessagesAtOrigin(this.getNumberOfCompletedMessagesAtOrigin()+1);
			return true;
		} else {
			int nextNodeId = msg.getSendList().remove();
			this.outputFile.write("changed toNode to "+nextNodeId);
			msg.setToNode(nextNodeId);
			msg.setFromNode(this.getNodeId());
			this.outputFile.write("Message now has the value"+msg.getValue());
			return false;
		}
		
	}
	public Socket getConnection (int nodeId) throws IOException{
		for (Connections temp :this.connectionList) {
			if (temp.getToNodeId() == nodeId) {
				return temp.getToSocket();
			}
		}
		throw new IOException();
	}
	public void killConnections(ArrayList<Connections> connectionList) {
		for (Connections temp: connectionList) {
			temp.kill();
		}
	}
	public void sender() {
		this.getOutputFile().write("Sender Thread started successfully");
		this.getOutputFile().write("Going to sleep so the other nodes come up");
		try {
			Thread.sleep(this.getNodeId()*2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			this.outputFile.write(e.toString());
		}
		//initialize the connections
		for(NodeInfo temp:this.conf.getNodeInfo()) {
			this.getOutputFile().write("CREATING CONNECTIONS TO"+temp.getHostname());
			connectionList.add(new Connections(temp));
		}

		int noOfBroadcastMsgs= -999 ;
		while (true) {
			try {
				if (noOfBroadcastMsgs == 0 && this.getNumberOfCompleteMessagesReceived()==this.getTotalNumberOfNodes()-1){
					this.outputFile.write("SENDER THREAD EXITING NOW!");
					break;
				}
				this.outputFile.write("Ready to take messages from the queue");
				Message processMsg = msgQ.take();
				// change toNode and add random value to the message value
				//Thread.sleep(2000);
				if (this.getNumberOfCompletedMessagesAtOrigin()==this.getNumberOfMessagesAtOrigin() && noOfBroadcastMsgs<0) {
					//generate and broadcast complete messages
					this.getOutputFile().write("^^^^^^^^^^^^^^^^^^Now BROADCASTING a COMPLETE message^^^^^^^^^^^^^^^^^^");
					noOfBroadcastMsgs=0;
					for (NodeInfo temp: this.conf.getNodeInfo()) {
						if (temp.getId()==this.getNodeId()) {
							continue;
						} else {
							Message compMessage = new Message(temp.getId(),this.getNodeId(),"COMPLETE");
							this.msgQ.add(compMessage);
							noOfBroadcastMsgs++;
						}
					}
				}
				if (ProcessMessage(processMsg)){
					continue;
				}
				this.outputFile.write("Processing of message complete");
				if (processMsg.getType().equals("COMPLETE")) {
					noOfBroadcastMsgs--;
				}
				//now send this message to the toNode
				this.outputFile.write("found to node to be(AFTER PROCESSING) "+processMsg.getToNode());
				Socket sendToNode = getConnection(processMsg.getToNode());
				this.outputFile.write("Connection found. Now writing to the connection");
				ObjectOutputStream outStream = new ObjectOutputStream(sendToNode.getOutputStream());
				outStream.writeObject(processMsg);
				outStream.flush();
				this.outputFile.write("MessageSent successfully");
				//now kill yourself
			} catch (InterruptedIOException e) {
			      this.outputFile.write("Interrupted via InterruptedIOException");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (noOfBroadcastMsgs<0){
					this.outputFile.write("!!!!!!SENDER EXCEPTION!!!!!!");
					this.outputFile.write(e.toString());
				} else {//ignore
					
				}
			} catch (InterruptedException e) {
				System.out.println("Interrupted via InterruptedException "+this.getNodeId());
				e.printStackTrace();
				break;
			}
			
		}
		this.outputFile.write("!!!!!!Closing all connections!!!!!!");
		this.killConnections(this.connectionList);
	}
	public void run() {
		Socket server = null;
			try {
				this.outputFile.write("Waiting for client on port "+serverSocket.getLocalPort()+" ...");
				 server = this.serverSocket.accept();
				this.outputFile.write("Conected to "+server.getRemoteSocketAddress()+" ...");
				while (true) {
					ObjectInputStream in = new ObjectInputStream (server.getInputStream());
					Object receivedObj = in.readObject();
					Message receivedMsg = (Message)receivedObj;
					this.outputFile.write("Added message to the queue ...");
					this.msgQ.add(receivedMsg);	
				}
			} catch (InterruptedIOException e) {
			      Thread.currentThread().interrupt();
			      try {
					server.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			      this.outputFile.write("Interrupted via InterruptedIOException");
			} catch (IOException | ClassNotFoundException  e) {
				this.outputFile.write("!!!!!!RECEIVER EXCEPTION!!!!!!");
				System.out.println("closing down this server now. Node id: "+this.getNodeId());
				try {
					serverSocket.close();
				} catch (IOException e1) {
					System.out.println("ErrorClosingThisServer");
					e1.printStackTrace();
				}
				System.out.println("IO exception timed out ");
			} 
		
	}
	public int getTotalNumberOfNodes() {
		return totalNumberOfNodes;
	}
	public void setTotalNumberOfNodes(int totalNumberOfNodes) {
		this.totalNumberOfNodes = totalNumberOfNodes;
	}
	public int getLabel() {
		return label;
	}
	public void setLabel(int label) {
		this.label = label;
	}
}
