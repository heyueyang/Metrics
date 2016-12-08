package metrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MergeMetrics {
	
	
	static String resultDir = "/home/yueyang/data/final_metrics/";
	static String complexDir = "/home/yueyang/data/com_csv/";
	static String networkDir = "/home/yueyang/data/network_csv/";
	static String bowDir = "/home/yueyang/data/bow_csv/";
	static String othersDir = "/home/yueyang/data/others_csv/";
	static String sourceCodeDir = "/home/yueyang/recover_projects/";
	static String recoverDir = "/home/yueyang/data/recover_info/";
	
	String project = "";
	String complexPath = "" ;
	String networkPath = "" ;
	String bowPath = "" ;
	String othersPath = "";
	String resultPath = "" ;
	String projectHome = "" ;
	
	Map<List<String>, StringBuffer> content;
	static Map<List<Integer>, String> label;
	List<Integer> attributes = null;
	static List<List<Integer>> commitId_fileIds;
	static List<List<Integer>> id_commitId_fileIds;
	static Map<List<Integer>, List<Integer>> cf2icf;
	static StringBuffer attrString;
	static List<String> netFileNames = new ArrayList<String>();
	static String netNullString;
	static String[] projects = {"ant" ,"camel","eclipse",  "itextpdf", "jEdit", "liferay","lucene", "struts", "tomcat", "voldemort"};// ,
	
	public static void main(String[] args) throws Exception {
		for(int i = 0; i < projects.length; i++){
			System.out.println("Merge " + projects[i] + " metrics start...");
			try{
				MergeMetrics mer = new MergeMetrics(projects[i]);
				mer.excute();
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
			System.out.println("Merge " + projects[i] + " metrics finished!");
		}
		//MergeMetrics mer = new MergeMetrics("eclipse");
		//mer.excute();
	}
	public void excute() throws Exception {		
		
		if(new File(resultPath).exists()){
			System.out.println("File " + resultPath + " existed!");
		}else{
			Map<List<Integer>, String> complex = readComplex(complexPath);

			Map<List<Integer>, String> network = readNetwork(networkPath);//null;//
			
			Map<List<Integer>, String> others = readOthers(othersPath);//null;//
			
			Map<List<Integer>, StringBuffer> bow = readBowFromCSV(bowPath);//null;//

			merge(resultPath, complex, network, bow, others);
		}
	}
	
	
	public MergeMetrics(String pro) {
		this.project = pro;
		label = new HashMap<List<Integer>, String>();
		commitId_fileIds = new ArrayList<List<Integer>>();
		id_commitId_fileIds = new ArrayList<List<Integer>>();
		attrString = new StringBuffer();
		cf2icf = new HashMap<List<Integer>, List<Integer>>();
		netNullString="";
		
		complexPath = complexDir + project + ".csv" ;
		networkPath = networkDir + project + "Net.csv";
		bowPath = bowDir + project + "Bow.csv";
		othersPath = othersDir + project + "Others.csv";
		resultPath = resultDir + project + ".csv";
		projectHome = sourceCodeDir + project + "AllFiles/";
	}


	private static void merge(String resultPath, Map<List<Integer>, String> complex, Map<List<Integer>, String> network, Map<List<Integer>, StringBuffer> bow, Map<List<Integer>, String> others) throws IOException{
		System.out.println("merge start...");
		File file = new File(resultPath);
		if (!file.exists()) {
		    file.createNewFile();
		   }
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		 
		//StringBuffer sBuffer = new StringBuffer();
		bw.write(attrString + "," + "change_prone" + "\n");
		int cnt = 0;
		for (List<Integer> list : id_commitId_fileIds) {
			
			bw.write(complex.get(list) + ",");
			//System.out.println(list.get(0) + "_" + list.get(1));
			
			if(network.get(list) != null){
				bw.write(network.get(list) + ",");
			}else{
				bw.write(netNullString);
			}
			
			bw.write(others.get(list) + ",");
			bw.write(bow.get(list) + ",");
			bw.write(label.get(list) + "\n");
			
			/*if(++cnt%100 == 0){
				//System.out.println("*************************************************");
				bw.write(sBuffer.toString());
				bw.flush();
				//sBuffer.setLength(0);
				sBuffer = new StringBuffer();
			}*/
		}
		
		bw.flush();
		bw.close();
		System.out.println("result path:" + resultPath + " finished!");
	}
	
	private static Map<List<Integer>, String> readComplex(String path) throws IOException{
		
		System.out.println("complexity path:" + path);
		Map<List<Integer>, String> complex = new HashMap<List<Integer>, String>();
		BufferedReader bReader = null;
		try {
			
			bReader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//StringBuffer sBuffer = new StringBuffer();
		String line;

		String head = bReader.readLine();
		//System.out.println(head);
		attrString.append(head.substring(0, head.lastIndexOf(",")));
		while ((line = bReader.readLine()) != null) {
			String[] record = line.split(",");
			List<Integer> ID_commitId_fileId = new ArrayList<Integer>();
			ID_commitId_fileId.add(Integer.parseInt(record[record.length-2]));
			ID_commitId_fileId.add(Integer.parseInt(record[2]));
			ID_commitId_fileId.add(Integer.parseInt(record[1]));

			id_commitId_fileIds.add(ID_commitId_fileId);
			
			/*List<Integer> commitId_fileId = new ArrayList<Integer>();
			commitId_fileId.add(Integer.parseInt(record[2]));
			commitId_fileId.add(Integer.parseInt(record[1]));
			commitId_fileIds.add(commitId_fileId);*/
			
			complex.put(ID_commitId_fileId, line.substring(0, line.lastIndexOf(",")));
			//System.out.println(fileId_commitId.toString()+" 0");
			label.put(ID_commitId_fileId, record[record.length-1]);
			
			//cf2icf.put(commitId_fileId, ID_commitId_fileId);
		}
		bReader.close();
		
		return complex;
		
	}
	
	private Map<List<Integer>, String> readNetwork(String path) throws IOException{
		System.out.println("network path:" + path);
		Map<String,List<Integer>> Path2ICF = getPath2ICFfromTxt(recoverDir + project + "Recover.txt");
		Map<List<Integer>, String> net = new HashMap<List<Integer>, String>();
		BufferedReader bReader = null;
		try {
			bReader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//StringBuffer sBuffer = new StringBuffer();
		String line;
		String head = bReader.readLine().substring(1);
		attrString.append("," + head);

		String[] headArr = head.split(",");
		for(int i = 0; i< headArr.length; i++){
			netNullString += "0,";
		}
		while ((line = bReader.readLine()) != null) {
			String[] record = line.split(",");
			String tempfilePath = (record[0].substring(record[0].indexOf(project) + project.length() + 1));
			String filePath = tempfilePath.replaceAll("\\\\", "/");
			
			List<Integer> ICF = Path2ICF.get(filePath);
			if(ICF != null){
				//System.out.println(filePath + "\n" + ICF.get(0) + "\n" + ICF.get(1)+ "\n" + ICF.get(2));
				net.put(ICF, line.substring(line.indexOf(",")+1));
				//System.out.println(net.get(ICF));
			}
			
		}
		//writeMap(net,"/home/yueyang/data/network_csv/" + project + "NetMap.txt");
		bReader.close();
		return net;
		
	}

	private Map<List<Integer>, StringBuffer> readBowContent(String projectHome) throws Exception{
		System.out.println("extract bow metrics start...");
		Map<List<Integer>, StringBuffer> bow = null;
		Extraction3 extra = new Extraction3(project, id_commitId_fileIds, projectHome);
		bow = extra.getContent();
		attrString.append("," + bow.get(extra.headmap));
		//System.out.println(bow.get(extra.headmap));
		//extra.writeContent(bow, bowDir + project + "Bow.txt");
		System.out.println("extract bow metrics finished!");
		return bow;
	}
	
private Map<List<Integer>, StringBuffer> readBowFromCSV(String path) throws Exception{
		
		if(!new File(path).exists()){
			System.out.println("bow path:" + path + "not exists!");
			Map<List<Integer>, StringBuffer> bow = readBowContent(this.projectHome);
			writeMap(bow,path);
			return bow;
		}
		
		System.out.println("read bow from path:" + path + "...");
		Map<List<Integer>, StringBuffer> bow = new HashMap<List<Integer>, StringBuffer>();
		BufferedReader bReader = null;

		try {
			
			bReader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String line;
		String head = bReader.readLine();
		String[] record = head.split(",");
		//System.out.println(head);
		attrString.append("," + head.substring(new String(record[0] + "," + record[1] + "," + record[2] + ",").length()));
		while ((line = bReader.readLine()) != null) {
			record = line.split(",");
			
			List<Integer> ID_commitId_fileId = new ArrayList<Integer>();
			ID_commitId_fileId.add(Integer.parseInt(record[0]));
			ID_commitId_fileId.add(Integer.parseInt(record[1]));
			ID_commitId_fileId.add(Integer.parseInt(record[2]));
			bow.put(ID_commitId_fileId, new StringBuffer(line.substring(new String(record[0] + "," + record[1] + "," + record[2] + ",").length())));
		}
		bReader.close();
		System.out.println("read bow from path:" + path + "finished!");
		return bow;
		
	}

private static Map<List<Integer>, String> readOthers(String path) throws IOException{
	
	System.out.println("others metrics path:" + path);
	Map<List<Integer>, String> complex = new HashMap<List<Integer>, String>();
	BufferedReader bReader = null;
	try {
		
		bReader = new BufferedReader(new FileReader(path));
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	String line;
	String head = bReader.readLine();
	String[] record = head.split(",");
	//System.out.println(head.substring(new String(record[0] + "," + record[1] + "," + record[2] + ",").length()));
	attrString.append("," + head.substring(new String(record[0] + "," + record[1] + "," + record[2] + ",").length()));
	while ((line = bReader.readLine()) != null) {
		record = line.split(",");
		List<Integer> ID_commitId_fileId = new ArrayList<Integer>();
		ID_commitId_fileId.add(Integer.parseInt(record[2]));
		ID_commitId_fileId.add(Integer.parseInt(record[1]));
		ID_commitId_fileId.add(Integer.parseInt(record[0]));

		
		complex.put(ID_commitId_fileId, line.substring(new String(record[0]+ "," + record[1] + ","+ record[2] + ",").length()));
	}
	bReader.close();
	
	return complex;
	
}


public static Map<String,List<Integer>> getPath2ICFfromTxt(String path) throws NumberFormatException, IOException {
	Map<String,List<Integer>> Path2ICF = new HashMap<String,List<Integer>>() ;
	System.out.println(path);
	File file = new File(path);
	BufferedReader bReader =  new BufferedReader(new FileReader(path));
	String line = null;
	while ((line = bReader.readLine()) != null) {
		String[] record = line.split("\t");
		
		List<Integer> ID_commitId_fileId = new ArrayList<Integer>();
		ID_commitId_fileId.add(Integer.parseInt(record[4]));
		ID_commitId_fileId.add(Integer.parseInt(record[1]));
		ID_commitId_fileId.add(Integer.parseInt(record[0]));
		Path2ICF.put(record[2],ID_commitId_fileId);
		
	}
	return Path2ICF;
}

public void writeMap( Map<List<Integer>, StringBuffer> bow, String path) throws IOException{
	File file = new File(path);
	if (!file.exists()) {
	    file.createNewFile();
	   }
	FileWriter fw = new FileWriter(file.getAbsoluteFile());
	BufferedWriter bw = new BufferedWriter(fw);
	 
	StringBuffer sBuffer = new StringBuffer();
	int cnt = 0;
	Set<List<Integer>> keys = bow.keySet();
	for (List<Integer> list : keys) {
		//sBuffer.append(map.get((List<Integer>)list) + "\n");
		//System.out.println(cnt++);
		/*if(++cnt%100 == 0){
			bw.write(sBuffer.toString());
			bw.flush();
			sBuffer = new StringBuffer();
		}*/
		//System.out.println(bow.get((List<Integer>)list).toString());
		bw.write( bow.get((List<Integer>)list).toString() + "\n");
	}
	
	bw.write(sBuffer.toString());
	bw.flush();
	bw.close();
	System.out.println("result path:" + resultPath + " finished!");
}

}
