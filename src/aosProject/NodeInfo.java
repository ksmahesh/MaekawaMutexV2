package aosProject;

public class NodeInfo {
	private int id;
	private String hostname; 
	private int portId;
	private boolean isAvailable;
	
	public NodeInfo(int id, String hostname, int portId){
		this.setId(id);
		this.setHostname(hostname);
		this.setPortId(portId);
		this.setAvailable(true);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPortId() {
		return portId;
	}

	public void setPortId(int portId) {
		this.portId = portId;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
}
