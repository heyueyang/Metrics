package metrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class DataCollect {
	private static SQLConnect connect = null;
	private static HashMap<String,String> start_tag = new HashMap<String,String>();
	//private static String end_commit = "0";
	private static String ProjectDirPath = "/home/yueyang/gitFile/";

	public static void main(String[] args) throws SQLException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		File Dir = new File(ProjectDirPath);
		File[] files = Dir.listFiles();
		/*start_tag.put("ant", "rel/1.7.0");
		start_tag.put("eclipse", "R4_4");
		start_tag.put("camel", "camel-1.6.3");
		start_tag.put("struts", "STRUTS_2_1_6");
		start_tag.put("liferay", "6.1.0-rc1");
		start_tag.put("tomcat", "TOMCAT_8_0_0_RC10");
		start_tag.put("voldemort", "release-1.9.5-cutoff");
		start_tag.put("jEdit", "1000");
		start_tag.put("itextpdf", "itextg-5.4.3");
		*/
		start_tag.put("ant", "12289");
		start_tag.put("eclipse", "25513");
		start_tag.put("camel", "27743");//7662
		start_tag.put("struts", "2513");
		start_tag.put("liferay", "24930");//17470
		start_tag.put("tomcat", "14026");//no tag in master,in trunk13813
		start_tag.put("voldemort", "3961");//3961
		start_tag.put("jEdit", "2837");//no tag in git svn2git
		start_tag.put("itextpdf", "3297");//no tag in master
		start_tag.put("lucene", "2594");// no tag in git svn2git
		
		//writeMasterBranchInfo("/home/yueyang/data/project_branch.txt");
		//writeRevisionInfo(start_tag, "/home/yueyang/data/commit_rev.txt");
		
    	for(int i = 0;i < files.length; i++){
    		if(!files[i].isDirectory()) continue;
    		System.out.println("------- " + files[i].getName()+ " -------");
    		FileOperation fo = new FileOperation(files[i].getName());
    		fo.CompleteExcute(start_tag.get(files[i].getName()));
    		
    		System.out.println("------- finished ! -------");
    	}
		
		//CalComplexity();


	}

	public static void GetPatches() {
		// TODO Auto-generated method stub
		ResultSet PatchesResult = null;
		String PatchesSQL = "select * from ";
		 try {
			PatchesResult = connect.Excute(PatchesSQL);
			while(PatchesResult.next()){
				String diff = PatchesResult.getString(2);
				//int line = CalChangeLine(diff);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public static void writeRevisionInfo(HashMap<String,String> startTag, String path) throws SQLException,
	IOException {
		
		File recoverInfoFile = new File(path);
		System.out.println("Start get revision information into " + recoverInfoFile.getPath() + "...");
		
		if (recoverInfoFile.exists()) {
			return;
			//recoverInfoFile.delete();
			//System.out.println("create file " + recoverInfoFile.getPath()+recoverInfoFile.createNewFile());
		}
		
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(recoverInfoFile));
		Object[] projectNames = startTag.keySet().toArray();
		for( int i =0; i < startTag.size(); i++){
			String projectName = (String)projectNames[i];
			FileOperation fo = new FileOperation(projectName);  		
			bWriter.append(fo.getRevisionInfo(projectName, startTag.get(projectName)));
		}
			bWriter.flush();
			bWriter.close();
			System.out.println("writed into" + path);

	}
	
	public static void writeMasterBranchInfo(String path) throws SQLException,
	IOException {
		
		File recoverInfoFile = new File(path);
		System.out.println("Start get master branch information into " + recoverInfoFile.getPath() + "...");
		
		if (recoverInfoFile.exists()) {
			return;
		}
		
		File Dir = new File(ProjectDirPath);
		File[] files = Dir.listFiles();
	
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(recoverInfoFile));
		
    	for(int i = 0;i < files.length; i++){
    		if(!files[i].isDirectory()) continue;
	
			String projectName = files[i].getName();
			FileOperation fo = new FileOperation(projectName);  
			String[] branch = fo.getMasterBranch();
			bWriter.append(projectName + "\t" + branch[0] + "\t" + branch[1] + "\n");
		}
			bWriter.flush();
			bWriter.close();
			System.out.println("writed into" + path);

	}
	
	

}