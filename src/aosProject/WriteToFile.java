package aosProject;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.Arrays;

public class WriteToFile {
	private Path outputFile;
	private boolean createdFile; 
	
	public WriteToFile(String fileName, int nodeId) {
		File outFile = new File(fileName+String.valueOf(nodeId));
		if (outFile.exists()){
			createdFile = true;
			outputFile = Paths.get(fileName+String.valueOf(nodeId));
			return;
		}
		try {
			outFile.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		createdFile = true;
		outputFile = Paths.get(fileName+String.valueOf(nodeId));
		try {
			Files.write(outputFile, Arrays.asList("# OUTPUT FILE FOR NODE with ID"+String.valueOf(nodeId)), Charset.forName("UTF-8"),StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public boolean write(String line) {
		if (!createdFile) {
			return false;
		}
		try {
			Files.write(outputFile, Arrays.asList(line), Charset.forName("UTF-8"),StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
