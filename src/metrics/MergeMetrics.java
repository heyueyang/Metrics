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
import java.util.Set;

public class MergeMetrics {
	
	static String project = "";
	static String resultDir = "/home/yueyang/data/final_metrics/";
	static String complexDir = "/home/yueyang/data/comlexity_csv/";
	static String networkDir = "/home/yueyang/data/network/";
	static String bowDir = "/home/yueyang/data/bow_new/";
	static String sourceCodeDir = "/home/yueyang/projects/recover_new/";
	static String recoverDir = "/home/yueyang/data/info_new/";
	Map<List<String>, StringBuffer> content;
	static Map<List<Integer>, String> label;
	List<Integer> attributes = null;
	static List<List<Integer>> commitId_fileIds;
	static List<List<Integer>> id_commitId_fileIds;
	static Map<List<Integer>, List<Integer>> cf2icf;
	static StringBuffer attrString;
	static List<String> netFileNames = new ArrayList<String>();
	static String netNullString;
	static String[] projects = {"ant",  "camel", "eclipse", "itextpdf", "jEdit", "liferay","lucene", "struts", "tomcat"};//, "voldemort"
	
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
		
		String resultPath = resultDir + project + ".csv";
		String complexPath = complexDir + project + "Metrics.csv" ;
		String networkPath = networkDir + project + "/Joined.csv";
		String bowPath = bowDir + project + "Bow.csv";
		String projectHome = sourceCodeDir + project + "AllFiles/";
		
		
		//Map<List<Integer>, StringBuffer> bow = readBowCSV(bowPath);
		if(new File(resultPath).exists()){
			System.out.println("File " + resultPath + " existed!");
		}else{
			Map<List<Integer>, String> complex = readComplex(complexPath);

			Map<List<Integer>, String> network = readNetwork(networkPath);
			
			Map<List<Integer>, StringBuffer> bow = readBowContent(projectHome);
			merge(resultPath, complex, network, bow);
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
	}


	private static void merge(String resultPath, Map<List<Integer>, String> complex, Map<List<Integer>, String> network, Map<List<Integer>, StringBuffer> bow) throws IOException{
		System.out.println("merge start...");
		File file = new File(resultPath);
		if (!file.exists()) {
		    file.createNewFile();
		   }
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		 
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(attrString + "," + "chane_prone" + "\n");
		int cnt = 0;
		for (List<Integer> list : id_commitId_fileIds) {
			
			//sBuffer.append(list.get(0) + "," + list.get(1) + "," + list.get(2) + ",");
			sBuffer.append(complex.get(list) + ",");
			//System.out.println(list.get(0) + "_" + list.get(1));
			
			/*if(netFileNames.contains(list.get(0) + "_" + list.get(1))){
				sBuffer.append(network.get(list) + ",");
				//System.out.println(network.get(list));
			}else{
				sBuffer.append(netNullString);
			}*/
			sBuffer.append(network.get(list) + ",");
			
			sBuffer.append(bow.get(list) + ",");
			sBuffer.append(label.get(list) + "\n");
			System.out.println(cnt++);
			
			if(cnt%100 == 0){
				System.out.println("*************************************************");
				bw.write(sBuffer.toString());
				bw.flush();
				//sBuffer.setLength(0);
				sBuffer = new StringBuffer();
			}
		}
		
		bw.write(sBuffer.toString());
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
			//System.out.println(line);
			//System.out.println(line.indexOf(record[0]+","));
			
			//System.out.println(ID_commitId_fileId.toString());
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
	
	private static Map<List<Integer>, String> readNetwork(String path) throws IOException{
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
		String head = bReader.readLine();
		attrString.append(head);

		String[] headArr = head.split(",");
		for(int i = 1; i< headArr.length; i++){
			netNullString += "0,";
		}
		while ((line = bReader.readLine()) != null) {
			//System.out.println(line);
			//System.out.println(line.substring(line.indexOf(",")+1));
			String[] record = line.split(",");
			//���network����Ԫcsv�ļ��ĵ�һ���ֶΣ��õ�commit_id��file_id��changeloc
			//String fileName = record[0].substring(record[0].lastIndexOf("\\") + 1,record[0].lastIndexOf("."));
			String filePath = record[0].substring(record[0].lastIndexOf(project) + project.length() + 1,record[0].lastIndexOf("."));
			//String[] fileId_commitIdArray = fileName.split("_");
			
			/*List<Integer> ID_commitId_fileId = new ArrayList<Integer>();
			ID_commitId_fileId.add(Integer.parseInt(fileId_commitIdArray[2]));
			ID_commitId_fileId.add(Integer.parseInt(fileId_commitIdArray[1]));
			ID_commitId_fileId.add(Integer.parseInt(fileId_commitIdArray[0]));*/
			List<Integer> ICF = Path2ICF.get(filePath);
			if(ICF != null){
				net.put(ICF, line.substring(line.indexOf(",")+1));
			}
			
			//netFileNames.add(filePath);
			//System.out.println(fileId_commitIdArray[0] + "_" + fileId_commitIdArray[1] + "*");
			//label.put(ID_fileId_commitId, record[record.length-1]);
			//ID_fileId_commitId.clear();
		}
		bReader.close();
		return net;
		
	}

	private static Map<List<Integer>, StringBuffer> readBowContent(String projectHome) throws SQLException, IOException{
		System.out.println("extract bow metrics start...");
		Map<List<Integer>, StringBuffer> bow = null;
		Extraction3 extra = new Extraction3(project, id_commitId_fileIds, projectHome);
		bow = extra.getContent();
		attrString.append("," + bow.get(extra.headmap));
		//System.out.println(bow.get(extra.headmap));
		//extra.writeContent(bow, project, bowDir);
		System.out.println("extract bow metrics finished!");
		return bow;
	}
	
private static Map<List<Integer>, StringBuffer> readBowCSV(String path) throws IOException{
		
		System.out.println("bow path:" + path);
		Map<List<Integer>, StringBuffer> bow = new HashMap<List<Integer>, StringBuffer>();
		BufferedReader bReader = null;
		//��ȡ���渴�Ӷȶ���Ԫ��csv�ļ�
		try {
			
			bReader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//���ж�ȡcsv�ļ������ļ�fileId_commitId_
		//StringBuffer sBuffer = new StringBuffer();
		String line;
		String fileInfo = "";
		String feature = "";
		
		String head = bReader.readLine();
		//System.out.println(head);
		attrString.append(head.substring(0, head.lastIndexOf(",")));
		while ((line = bReader.readLine()) != null) {
			String[] record = line.split(",");
			
			List<Integer> ID_commitId_fileId = new ArrayList<Integer>();
			ID_commitId_fileId.add(Integer.parseInt(record[0]));
			ID_commitId_fileId.add(Integer.parseInt(record[1]));
			ID_commitId_fileId.add(Integer.parseInt(record[2]));
			//System.out.println(line);
			//System.out.println(line.indexOf(record[0]+","));
			String temp1 = line.substring(line.indexOf(","));
			String temp2 = temp1.substring(temp1.indexOf(","));
			String temp3 = temp2.substring(temp2.indexOf(","));
			bow.put(ID_commitId_fileId, new StringBuffer(line));
		}
		bReader.close();
		
		return bow;
		
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

}
