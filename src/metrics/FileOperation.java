package metrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FileOperation {
	private static SQLConnect connect = null;
	private static String InfoDir = "/home/yueyang/data/recover_info/"; 
	private static String OtherDir = "/home/yueyang/data/others_csv/"; 
	private static String project = null;
	private static String MasterBranchId = "0";
	
	public FileOperation(String pro) {
		project = pro;
		connect = new SQLConnect(pro);
	}
	public void CompleteExcute(String start_tag) throws InterruptedException{

		try {
			if(start_tag.isEmpty()){
				System.out.println("-------" + project + "(" + start_tag + ") : Invalid Tag!-------");
				return;
			}else{
				MasterBranchId = getMasterBranch()[0];
				getFileCommitInfo(start_tag, project);
			}
			//getAllFiles(FileDir+project+"/");
			//getAllPreFile(FileDir+project+"Files/");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getCommitFromTag(String tag) throws SQLException,
	IOException {

		//String sql = "select commit_id from scmlog where scmlog.message=" + tag;
		String sql = "select commit_id from tags inner join tag_revisions on tags.id=tag_revisions.tag_id where tags.name=" + tag;
		ResultSet CommitOfTag = connect.Excute(sql);
		return CommitOfTag.getString(1);
		
	}
	
	public String[] getMasterBranch() throws SQLException,
	IOException {
		String[] MaxBranch = new String[2];
		String NameOfBranch = "select actions.branch_id,branches.name,count(distinct commit_id) cnt from branches inner join  actions on actions.branch_id=branches.id "
				+ " group by actions.branch_id"
				+ " order by cnt desc";
		ResultSet branch = connect.Excute(NameOfBranch);
		int MaxCnt = 0;
		while(branch.next()){
			if(branch.getString(2).equals("master")){
				MaxBranch[0] = branch.getString(1);
				MaxBranch[1] = branch.getString(2);
				break;
			}else if(branch.getInt(3)>MaxCnt){
				MaxCnt = branch.getInt(3);
				MaxBranch[0] = branch.getString(1);
				MaxBranch[1] = branch.getString(2);
			}
			
		}
		return MaxBranch;
	}
	
	
	public void getFileCommitInfo(String startTag, String pro) throws SQLException,
	IOException {
		
		String recoverInfoPath = InfoDir + pro + "Recover.txt";
		File recoverInfoFile = new File(recoverInfoPath);
		System.out.println("Start get recover information into " + recoverInfoFile.getPath() + "...");
		
		File dir = new  File(InfoDir);
		if(!dir.exists()){
			dir.mkdirs();
		}
		if (recoverInfoFile.exists()) {
			return;
			//recoverInfoFile.delete();
			//System.out.println("create file " + recoverInfoFile.getPath()+recoverInfoFile.createNewFile());
		}
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(recoverInfoFile));
		try {
			
			//String startCommitId = getCommitFromTag(startTag);
			String startCommitId = startTag;
			//找到startCommitId之后的所有类型为“M”的action
			String ChangedFileSQL = " select actions.file_id,actions.commit_id,patch"//commit_date,
						+ " from (patches inner join actions on patches.file_id=actions.file_id and patches.commit_id=actions.commit_id) "
						+ " inner join scmlog on scmlog.id=actions.commit_id "
						+ " where actions.type='M' and actions.commit_id>" + startCommitId //]+ " and actions.commit_id<" + endCommitId
						+ " and RIGHT(actions.current_file_path,4)='java'"
						+ " and actions.branch_id=" + MasterBranchId;
				
				System.out.println(ChangedFileSQL);
				ResultSet ChangedFile = connect.Excute(ChangedFileSQL);//CommitsOfFileSQL
			//将action按照file_id分组，并计算每一个file到当前为止的更改行数
				HashMap<String,Integer> FileChange = new  HashMap<String,Integer>();
				while(ChangedFile.next()){
					String FileId = ChangedFile.getString(1);
					if(FileChange.containsKey(FileId)){
						int ChangeLoc = FileChange.get(FileId);
						//FileChange.remove(FileId);
						FileChange.put(FileId, ChangeLoc + CalChangeLine(ChangedFile.getString(3)));
					}else{
						FileChange.put(FileId, CalChangeLine(ChangedFile.getString(3)));
					}
					//System.out.println("---" + FileId + ":" + FileChange.get(FileId));
					
				}
				
				/*
				String maxDateSQL = "select actions.file_id,max(scmlog.commit_date) date "
						+ "from actions inner join scmlog on scmlog.id=actions.commit_id "
						+ " where actions.commit_id<=" + startCommitId
						+ " and RIGHT(current_file_path,4)='java' and branch_id=" + MasterBranchId
						+ " group by file_id ";
				
				System.out.println(maxDateSQL);
				ResultSet maxDate = connect.Excute(maxDateSQL);
				Map<String, String> maxDateMap = new HashMap<String, String>();
				String str = "";
				while(maxDate.next() && !maxDate.getString(1).equals("D")){
					maxDateMap.put(maxDate.getString(1), maxDate.getString(2)); 
				}*/
				
				//out of memory
				/*String RecentCommitSQl =  "select a.file_id,a.commit_id,a.current_file_path,a.rev,a.commit_date,a.type "
						+ "from "
						+ "(select actions.file_id,actions.commit_id,actions.current_file_path,scmlog.rev,scmlog.commit_date,actions.branch_id,actions.type  "
							+ " from actions inner join scmlog on scmlog.id=actions.commit_id"
							+ " where actions.commit_id<=" + startCommitId
							+ " and RIGHT(current_file_path,4)='java'"
							+ " and branch_id=" + MasterBranchId
							+ ") a"
							+ " where a.commit_date="
								+ " (select max(scmlog.commit_date)"
								+ "from actions inner join scmlog on scmlog.id=actions.commit_id "
								+ " where actions.commit_id<=" + startCommitId
								+ " and branch_id=" + MasterBranchId
								+ " and actions.file_id = a.file_id)"
							+ " and a.commit_id="
								+ " (select max(commit_id) from actions"
								+ " where commit_"
								+ " and actions.file_id = a.file_id)";*/
				
				
				String RecentCommitSQl = "select a.file_id,a.commit_id,a.current_file_path,a.rev,a.commit_date,a.type,a.branch_id "
						+ "from "
						+ "(select actions.file_id,actions.commit_id,actions.current_file_path,scmlog.rev,scmlog.commit_date,actions.branch_id,actions.type  "
							+ " from actions inner join scmlog on scmlog.id=actions.commit_id"
							+ " where actions.commit_id<=" + startCommitId
							+ " and RIGHT(current_file_path,4)='java'"
							+ " and branch_id=" + MasterBranchId
							+ ") a"
						+ " where not exists "
							+ " (select actions.id"
							+ " from actions inner join scmlog on scmlog.id=actions.commit_id  "
							+ " where actions.commit_id<=" + startCommitId
							+ " and branch_id=" + MasterBranchId
							+ "	and actions.file_id=a.file_id"
							+ " and a.commit_date<scmlog.commit_date)"
						+ " and not exists "
							+ " (select actions.id"
							+ " from actions inner join scmlog on scmlog.id=actions.commit_id  "
							+ " where actions.commit_id<=" + startCommitId
							+ " and branch_id=" + MasterBranchId
							+ "	and actions.file_id=a.file_id"
							+ " and a.commit_date=scmlog.commit_date"
							+ " and a.commit_id<actions.commit_id)";
				
				
				Map<String,String> OtherMetrics = new HashMap<String,String>();
				System.out.println(RecentCommitSQl);
				ResultSet RecentCommit = connect.Excute(RecentCommitSQl);
				String str = "";
				int totalCnt = 0, changeCnt = 0;
				while(RecentCommit.next()){
					//remove the unchanged files
					//if(!RecentCommit.getString(6).equals("D"))// 
					if(!RecentCommit.getString(6).equals("D") && (!RecentCommit.getString(3).contains("test") && !RecentCommit.getString(3).contains("Test")))// 
					{
						totalCnt++;
						//if(FileChange.containsKey(RecentCommit.getString(1)) == true)
						{
							changeCnt++;
							int changeLoc = (FileChange.containsKey(RecentCommit.getString(1)))?FileChange.get(RecentCommit.getString(1)):0;
							str = RecentCommit.getString(1) + "\t" + RecentCommit.getString(2)+ "\t" + RecentCommit.getString(3) + "\t" + RecentCommit.getString(4) + "\t" 
						+ changeLoc + "\t" + RecentCommit.getString(5) + "\t" + RecentCommit.getString(6)+ "\t" + RecentCommit.getString(7)+ "\n";
							bWriter.append(str);
							OtherMetrics.put(RecentCommit.getString(1), RecentCommit.getString(1) + "," + RecentCommit.getString(2)+ "," + changeLoc);
						}
					}
					
				}
			bWriter.flush();
			bWriter.close();
			System.out.println("writed into" + recoverInfoPath);
			System.out.println(totalCnt + "\t" + changeCnt);
			getCLocBefore(OtherMetrics, startCommitId, OtherDir + pro + "Others.csv");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getAllFiles(String path, String start_commit) throws SQLException,
	IOException, InterruptedException {
		   
			File AllFiles = new File(path);
			ArrayList<String> AllFilesList = findFiles(path,".java");
			//tempFile.getAbsolutePath().substring(tempFile.getAbsolutePath().indexOf(baseDirName.length()))
			File recoverInfoFile = new File(InfoDir + "recoverInfo1.txt");
			System.out.println("Start to recover files into " + recoverInfoFile.getPath() + "...");
			
			if (!recoverInfoFile.exists()) {
				System.out.println("create file " + recoverInfoFile.getPath()+recoverInfoFile.createNewFile());
			}else{
				return;
			}
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(recoverInfoFile));
			String FilePathStr = "(";
			for(int i = 0; i< AllFilesList.size(); i++){
				String AbsPath = AllFilesList.get(i);
				FilePathStr += AbsPath.substring(AbsPath.indexOf(path.length())) + ",";
			}
			FilePathStr = FilePathStr.substring(0, FilePathStr.length()-1) + ")";
			String sql = "select actions.file_id,actions.commit_id,actions.current_file_path,scmlog.rev,patch"
					+ " from patches inner join actions on patches.file_id=actions.file_id and patches.commit_id=actions.commit_id"
					+ " where commit_id=" + start_commit + " and current_file_path in " + FilePathStr;
			System.out.println(sql);
			ResultSet CommitOfFile = connect.Excute(sql);
			String str = "";
			while(CommitOfFile.next()){
				str = CommitOfFile.getString(1) + "\t" + CommitOfFile.getString(2)+ "\t" + CommitOfFile.getString(3) + "\t" + CommitOfFile.getString(4) + "\t" 
			+ (CommitOfFile.getString(6)=="1"?CalChangeLine(CommitOfFile.getString(5)):0) + "\n";
				bWriter.append(str);
			}
			//System.out.println(str);
			
		
		bWriter.flush();
		bWriter.close();
		System.out.println("writed into"+InfoDir + "recoverInfo.txt");

		
	}
public static ArrayList<String> findFiles(String baseDirName, String targetFileName) {     
		ArrayList<String> fileList = new ArrayList<String>();
        File baseDir = new File(baseDirName);       // 创建一个File对象  
        if (!baseDir.exists() || !baseDir.isDirectory()) {  // 判断目录是否存在  
            System.out.println("文件查找失败：" + baseDirName + "不是一个目录！");  
        }  
        String tempName = null;     
        //判断目录是否存在     
        File tempFile;  
        File[] files = baseDir.listFiles();  
        for (int i = 0; i < files.length; i++) {  
            tempFile = files[i];  
            if(tempFile.isDirectory()){  
                findFiles(tempFile.getAbsolutePath(), targetFileName);  
            }else if(tempFile.isFile()){  
                tempName = tempFile.getName();  
                if(tempName.contains(targetFileName)){  
                    // 匹配成功，将文件名添加到结果集  
                    fileList.add(tempFile.getAbsolutePath());  
                }  
            }  
        }
		return fileList;  
    }     
	public void getAllPreFile(String dictory) throws SQLException,
	IOException {
		File AllFile = new File(dictory);
		System.out.println("Dir:"+dictory);
		if (!AllFile.isDirectory()) {
			System.out.println("当前目录不是文件夹!");
			return;
		}
		String[] cFiles = AllFile.list();
		int cnt = 0;
		for (String file : cFiles) {
			getPreFile(dictory, file);
			cnt++;
		}
		System.out.println("Totally recovered " + cnt + "prefiles!");
		
	}
	
	public static void getPreFile(String dictory, String string) throws SQLException,
	IOException {
		File curFile = new File(dictory + "/" + string);
		BufferedReader bReader = new BufferedReader(new FileReader(curFile));
		
		String FileName = string.substring(0, string.lastIndexOf("."));
		int file_id = Integer.parseInt(FileName.split("_")[0]);
		int commit_id = Integer.parseInt(FileName.split("_")[1]);
		int change_loc = Integer.parseInt(FileName.split("_")[2]);
		
		String preDirStr = new File(dictory).getParent() + "/" + new File(dictory).getName() + "Pre/";
		File preDir = new File(preDirStr);
		if(!preDir.exists()){
			preDir.mkdirs();
			System.out.println(preDir.getPath()+" created!");
		}
		
		File preFile = new File(preDirStr + file_id + "_" + commit_id + "_" + change_loc
				+ "_pre.java");
		if (!preFile.exists()) {
			preFile.createNewFile();
		}
		
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(preFile));
		int readIndex = 0;
		//System.out.print(curFile);
		String sql = "select patch from patches where commit_id=" + commit_id
				+ " and file_id=" + file_id;
		ResultSet resultSet = connect.Excute(sql);
		String patch = null;
		while (resultSet.next()) {
			patch = resultSet.getString(1);
		}
		if (patch == null) {
			System.out.println("the patch of " + curFile + " is null");
			String line=null;
			while ((line=bReader.readLine())!=null) {
				bWriter.append(line+"\n");
			}
			bReader.close();
			bWriter.flush();
			bWriter.close();
			return;
		}
		
		String[] lines = patch.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith("---") || lines[i].startsWith("+++")) {
				continue;
			}
			if (lines[i].startsWith("@@")) {
				String lineIndex = (String) lines[i].subSequence(
						lines[i].indexOf("+") + 1, lines[i].lastIndexOf("@@"));
		
				int index = Integer.parseInt(lineIndex.split(",")[0].trim());
				int shiftP = Integer.parseInt(lineIndex.split(",")[1].trim());
				int shiftF = shiftP;
		
				while (readIndex < index - 1) {
					String line = bReader.readLine();
					bWriter.append(line + "\n");
					readIndex++;
				}
				i++;
				while (i < lines.length && (!lines[i].startsWith("@@"))) {
					if (lines[i].startsWith("-")) {
						bWriter.append(lines[i].substring(1, lines[i].length())
								+ "\n");
					} else if (lines[i].startsWith("+")) {
		
					} else {
						bWriter.append(lines[i] + "\n");
					}
					i++;
				}
				readIndex = readIndex + shiftF;
				for (int j = 0; j < shiftF; j++) {
					bReader.readLine();
				}
				i = i -1;
			}
		}

		String nextLineString = null;
		while ((nextLineString = bReader.readLine()) != null) {
			bWriter.append(nextLineString + "\n");
		}
		bReader.close();
		bWriter.flush();
		bWriter.close();
		//System.out.println("--->" + preFile);
	}
	
	private static int CalChangeLine(String str){
		if(str == null){
			return -1;
		}
		int cnt = 0;
		String[] data = str.split("\n"); 
		for(int i = 3 ;i < data.length; i++){
			//System.out.println(data[i]); 
			
			if(data[i].startsWith("+")||data[i].startsWith("-")){//(substring(0, 1)=="+")||(data[i].substring(0, 1)=="-"))
				//System.out.println(data[i].substring(0, 1)+":+"); 
				cnt++;
				
			}
		}
		
		return cnt;
		
	}
	
	public static String[] GetField(String field, String table, String append) {
		// TODO Auto-generated method stub
		ResultSet result = null;
		String sql = "select "+field+" from "+table+" "+append;
		ArrayList<String> res = new ArrayList<String>();
		 try {
			result = connect.Excute(sql);
			res.add(result.getString(0));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return (String[]) res.toArray();

	}
	

	
	public String getRevisionInfo(String projectName, String commitId) throws SQLException,
	IOException {
		
		String str = "";
		try {
			
			//String TagID2NameSQl = "select id from tags where name=" + commitName;
			//ResultSet TagId = connect.Excute(TagID2NameSQl);
			String RecentCommitSQl = "select rev from scmlog where id=" + commitId;
			//System.out.println(RecentCommitSQl);
			String[] branch = getMasterBranch();
			ResultSet commit = connect.Excute(RecentCommitSQl);
			while(commit.next()){
				str +=  commitId + "\t" + commit.getString(1) + "\t" + branch[0] + "\t" + branch[1] + "\n";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
	
	public void  getCLocBefore(Map<String, String> otherMetrics, String startCommitId, String path) throws SQLException,
	IOException {
		String recoverInfoPath = path;
		File recoverInfoFile = new File(recoverInfoPath);
		System.out.println("Start get recover information into " + recoverInfoFile.getPath() + "...");
		
		File dir = new  File(InfoDir);
		if(!dir.exists()){
			dir.mkdirs();
		}
		if (recoverInfoFile.exists()) {
			return;
			//recoverInfoFile.delete();
			//System.out.println("create file " + recoverInfoFile.getPath()+recoverInfoFile.createNewFile());
		}
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(recoverInfoFile));
		bWriter.append("fileId" + "," + "CommitId" + "," + "ChangeLoc" + "," + "ChangeLocBefore" + "," + "ChangeCntBefore" + "," + "bugCntBefore" + "\n");
		//HashMap<String,Integer> CLocBeforeMap = new HashMap<String,Integer>();
		for(String fileId : otherMetrics.keySet()){
			//System.out.println(fileId);
			String ChangedFileSQL = " select actions.file_id,patch,is_bug_fix"//commit_date,
						+ " from (patches inner join actions on patches.file_id=actions.file_id and patches.commit_id=actions.commit_id) "
						+ " inner join scmlog on scmlog.id=actions.commit_id "
						+ " where actions.type='M' and actions.commit_id<=" + startCommitId //]+ " and actions.commit_id<" + endCommitId
						+ " and RIGHT(actions.current_file_path,4)='java'"
						+ " and actions.file_id=" + fileId
						+ " and actions.branch_id=" + MasterBranchId;
				
				//System.out.println(ChangedFileSQL);
				ResultSet ChangedFile = connect.Excute(ChangedFileSQL);//CommitsOfFileSQL
				//CLocBeforeMap.put(fileId, ChangedFile.getInt(2));
				int changeLoc = 0;
				int changeCnt = 0;
				int bugCnt = 0;
				while(ChangedFile.next()){
					changeLoc += CalChangeLine(ChangedFile.getString(2));
					changeCnt++;
					bugCnt += ChangedFile.getString(3).equals("1")?1:0;
				}
				bWriter.append(otherMetrics.get(fileId) + "," + changeLoc + "," + changeCnt + "," + bugCnt + "\n");
		}
		bWriter.flush();
		bWriter.close();
	}

}
