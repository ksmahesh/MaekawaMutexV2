package aosProject;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFileReader {
	private int numberOfNodes;
	private ArrayList<NodeInfo> nodeInfo;
	public ArrayList<NodeInfo> getNodeInfo() {
		return nodeInfo;
	}

	public ArrayList<MessageInfo> getMessageInfo() {
		return messageInfo;
	}

	private ArrayList<MessageInfo> messageInfo;
	public ConfigFileReader (String fileName) {
		try {
			nodeInfo = new ArrayList<NodeInfo>();
			messageInfo = new ArrayList<MessageInfo>();
			Scanner in = new Scanner(new File (fileName));
			while (in.hasNextLine()) {
				String temp = in.nextLine();
				if (temp.matches( "^\\s*#.*" )||temp.matches( "^\\s*$" )) {
					continue;
				} else if (temp.length()==0) {
					continue;
				} else if (temp.matches(".*,.*")){
					int temp_id = Integer.MIN_VALUE;
					ArrayList<Integer> list = new ArrayList<Integer>();
					System.out.println("found the paths of messages to be sent");
					String[] process = temp.split("\\s+");
					for (int i = 0; i<process.length; i++) {
						if (process[i].length()==0) {
							continue;
						} else if (temp_id == Integer.MIN_VALUE) {
							temp_id = Integer.parseInt(process[i]);
						} else {
							String regexGetList = "\\(.*\\)";
							Pattern getList = Pattern.compile(regexGetList);
							Matcher gotList = getList.matcher(temp);
							String sendList = gotList.find() ? gotList.group() : "";
							if (sendList.length()==0) {
								System.out.println("Did not find the send list");
							}
							//lets get rid of parenthesis
							sendList=sendList.replace("(","");
							sendList=sendList.replace(")","");
							// and now lets split
							String[] temp_split = sendList.split(",");
							for (String s: temp_split) {
								list.add(Integer.parseInt(s.trim()));
							}
							break;
						}
					}
					if (temp_id == Integer.MIN_VALUE || list.size()==0) {
						System.out.println("unable to process the send list");
					} else {
						int[] ret = new int[list.size()];
						  for(int i = 0;i < ret.length;i++)
						    ret[i] = list.get(i);
						messageInfo.add(new MessageInfo(temp_id,ret));
						System.out.println("Added to messageInfo List with sender id "+temp_id);
					}
				} else {
					String[] process = temp.split("\\s+");
					if (process.length == 1) {
						//found number of nodes 
						setNumberOfNodes(Integer.parseInt(process[0]));
					} else {
						int temp_id = Integer.MIN_VALUE;
						int temp_portid = Integer.MIN_VALUE;
						String temp_hostname="";
						for (int i = 0; i<process.length; i++) {
							if (process[i].length()==0) {
								continue;
							} else if (process[i].matches("^[a-zA-Z].*")) {
								temp_hostname = process[i];
							} else if (temp_id == Integer.MIN_VALUE) {
								temp_id = Integer.parseInt(process[i]);
							} else {
								temp_portid = Integer.parseInt(process[i]);
							}
						}
						if (temp_id == Integer.MIN_VALUE || temp_portid==Integer.MIN_VALUE || temp_hostname.isEmpty()) {
							System.out.println("Error reading the config file");
							return;
						} else {
							nodeInfo.add(new NodeInfo(temp_id, temp_hostname, temp_portid));
							System.out.println("Addded to the NodeInfo Array");
						}
						}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ConfigFileReader x = new ConfigFileReader("config.txt");
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}
}
