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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * 提取源码信息路径信息。
 * 
 * @param id_commitId_fileIds
 *            由所有id、commit_id和file_id构成的主键列表。
 * @param dictionary
 *            存放实际属性名称和属性代号名称对的map。key值为实际属性名称。
 * @param dictionary2
 *            存放属性代号名称和实际属性名称对的map。
 * @param currStrings
 *            当前出现过的属性。
 * @param bow
 *            用以提取源码信息路径信息的词向量类。
 * @param content
 *            实际得到的各实例，key为id_commitId_fileIds中的元素，value为对应的属性值。当key值为(-1,-1,-1)
 *            时对应的值为属性名称。
 * @param colMap
 *            content中属性及其索引的map。因为在持续更新实例中数据的过程中，某个实例的某个属性值可能需要改变
 *            则可根据此map快速对应到content中该属性的值，然后将其修改。
 * @param headmap
 *            content中的属性字段，即存放所有属性名称的map。
 * @author niu
 *
 */
public class Extraction3 extends Extraction {
	static List<List<Integer>> id_commitId_fileIds;
	Map<String, String> dictionary;
	Map<String, String> dictionary2;
	Set<String> currStrings;
	Bow bow;
	static Map<List<Integer>, StringBuffer> content;
	Map<String, Integer> colMap;
	static List<Integer> headmap;
	static String bowDir = "/home/yueyang/data/bow_csv/";
	static String dicDir = "/home/yueyang/data/dic_csv/";
	static String error_dir = "/home/yueyang/data/error/";
	static String recoverDir = "/home/yueyang/data/recover_info/";
	static String[] projects = {"ant", "eclipse", "camel", "itextpdf", "jEdit", "lucene","struts", "liferay","tomcat"};//, "voldemort"

	//static String project = "";
	static String projectHome = "c";
	
	public static void main(String[] args) throws Exception {
		
		for(int i = 0; i < projects.length; i++){
			System.out.println("Write " + projects[i] + " bow metrics start...");
			String resultPath = bowDir + projects[i] + "Bow.csv";
			String resultPath2 = dicDir + projects[i] + "Dic.csv";
			File resultFile = new File(resultPath);
			if(resultFile.exists()){
				System.out.println(bowDir + projects[i] + "Bow.csv" + " already exists!");
				continue;
			}
			Map<List<Integer>, StringBuffer> bow = null;
			Extraction3 extra = new Extraction3(projects[i], getCfFromTxt(recoverDir + projects[i] + "Recover.txt"), projectHome + projects[i] +"AllFiles/");
			
			try{
				extra.writeContent(extra.getContent(), resultPath);
				extra.writeDictionary(extra.getDictionary(), resultPath2);
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
			System.out.println("Write " + projects[i] + " bow metrics finished!");
		}
		/*
		System.out.println("Write " + project + " bow metrics start...");
		Map<List<Integer>, StringBuffer> bow = null;
		Extraction3 extra = new Extraction3(project, getCfFromTxt(recoverDir + project + "Recover.txt"), projectHome + project +"AllFiles/");
		bow = extra.getContent();
		writeContent(bow, project, bowDir);
		System.out.println("Write " + project + " bow metrics finished!");
		*/
	}

	/**
	 * 提取第三部分信息。
	 * 
	 * @param database
	 *            需要连接的数据库
	 * @param projectHome
	 * @param startId
	 * @param endId
	 * @throws SQLException
	 * @throws IOException
	 */
	
	public Extraction3(String database, String projectHome, int startId,
			int endId) throws SQLException, IOException {
		super(database);
		id_commitId_fileIds = new ArrayList<>();
		setICFfromDatabase(startId, endId);
		dictionary = new LinkedHashMap<>();
		dictionary2 = new LinkedHashMap<>();
		currStrings = new HashSet<>();
		content = new HashMap<>();
		colMap = new HashMap<>();

		// �����startId��endId��Ը�����е��ҡ�
		changeLogInfo();
		sourceInfoAll(projectHome);
		pathInfo();
	}

	public Extraction3(String database, List<List<Integer>> icf_id,
			String projectHome) throws SQLException, IOException {
		super(database);
		setCommitId_fileIds(icf_id);
		dictionary = new LinkedHashMap<>();
		dictionary2 = new LinkedHashMap<>();
		currStrings = new HashSet<>();
		content = new HashMap<>();
		colMap = new HashMap<>();
		headmap = new ArrayList<>();
		headmap.add(-1);
		headmap.add(-1);
		headmap.add(-1);
		StringBuffer head = new StringBuffer("Id,commitId,fileId,");
		content.put(headmap, head);
		for (List<Integer> list : id_commitId_fileIds) {
			if (list.get(0) != -1) {
				StringBuffer write = new StringBuffer(list.get(0) + ","
						+ list.get(1) + "," + list.get(2) + ",");
				content.put(list, write);
			}
		}

		// �����startId��endId��Ը�����е��ҡ�
		changeLogInfo();
		sourceInfoAll(projectHome);
		pathInfo();
	}

	/**
	 * 初始化实例集的keyset，实际上由于extraction1中包含的信息远多与实际想要分析的，
	 * 所以默认分析(start==-1||end==-1)时，应该以extraction2为基准分析。
	 * 
	 * @param start
	 *            若为-1，则以extraction2中的所有项为基准得到最终实例，如果指定特殊值则表明以extraction2中的子集为基准
	 * @param end
	 *            若为-1，则以extraction2中的所有项为基准得到最终实例，如果指定特殊值则表明以extraction2中的子集为基准
	 * @throws SQLException
	 * @throws IOException
	 */
	public void initial(int start, int end) throws SQLException, IOException {

	}

	/**
	 * 在content中针对指定的commit_id，更新属性s的值。
	 * 此函数主要针对pathinfo和changelog，因为path信息或者changelog信息只与commit_id有关，与file_id无关。
	 * 
	 * @param s
	 *            属性名称。如果当前属性集中已有该属性，则对文件中每个实例更新该属性的值， 否则，向属性集中添加该属性，并初始化各属性值。
	 * @param tent
	 *            当前已有的信息。
	 * @param commitId
	 *            需要更新的实例的commit_id。
	 * @param value
	 *            需要更新的值。
	 * @return 更新内容后的content。
	 */
	public Map<List<Integer>, StringBuffer> writeInfo(String s,
			Map<List<Integer>, StringBuffer> tent, int commitId, Integer value) {
		if (!currStrings.contains(s)) {
			// �����ǰ���Լ������������ԣ����½������ԡ�
			currStrings.add(s);
			String ColName = "s" + dictionary.size();
			dictionary.put(s, ColName);
			dictionary2.put(ColName, s);
			colMap.put(ColName, colMap.size() + 3);

			for (List<Integer> list : tent.keySet()) {
				if (list.get(0) == -1) {
					tent.put(headmap, tent.get(headmap).append(ColName + ","));
				} else if (list.get(1) == commitId) {
					tent.put(list, tent.get(list).append(value + ","));
				} else {
					tent.put(list, tent.get(list).append(0 + ","));
				}
			}
		} else {
			// 根据真实属性名获取content中的简要属性名。然后根据简要属性名快速得到该属性对应的列号。
			String column = dictionary.get(s);
			int index = colMap.get(column);
			for (List<Integer> list : tent.keySet()) {
				if (list.get(1) == commitId) {
					StringBuffer newbuffer = new StringBuffer();
					String[] arrayStrings = tent.get(list).toString()
							.split(",");

					for (int i = 0; i < index; i++) {
						newbuffer.append(arrayStrings[i] + ",");
					}
					int update = Integer.parseInt(arrayStrings[index]) + value;
					newbuffer.append(update + ",");
					for (int i = index + 1; i < arrayStrings.length; i++) {
						newbuffer.append(arrayStrings[i] + ",");
					}
					tent.put(list, newbuffer);
				}
			}

		}
		return tent;
	}

	/**
	 * 获取所有的changelog信息,并将其加入content。 注意：changelog信息只跟commit_id有关，与file_id无关。
	 * 
	 * @param csvFile
	 * @throws SQLException
	 * @throws IOException
	 */
	public void changeLogInfo() throws SQLException, IOException {
		bow = new Bow();
		// 获得所有不同的commit_id
		Set<Integer> commit_ids = new LinkedHashSet<>();
		for (List<Integer> list : id_commitId_fileIds) {
			if (!commit_ids.contains(list.get(1))) {
				commit_ids.add(list.get(1));
			}
		}
		// 对每个commit_id，获取其changelog 信息，并加入content。
		for (Integer commitId : commit_ids) {
			if (commitId != -1) {
				//System.out.println(commitId);
				sql = "select message from scmlog where id=" + commitId;
				resultSet = stmt.executeQuery(sql);
				resultSet.next();
				String message = resultSet.getString(1);
				Map<String, Integer> bp = bow.bow(message);
				for (String s : bp.keySet()) {
					content = writeInfo(s, content, commitId, bp.get(s));
				}
			}
		}

	}

	/**
	 * 获取源码和源码中改动的代码信息。
	 * 针对每个更改的(commit_id,file_id)对，其可能(如果某次更改类型为d，即删除了某个文件，那么就没有对应的文件)
	 * 对应一个java文件。 同时其对应于一个patch。需要根据脚本语言提前获得所有这些更改了的文件，并通过数据库获得所有的patch信息
	 * 然后使用此函数提取源码中的一些信息。
	 * 
	 * @param projectHome
	 *            包含所有需要提取信息的java源码的文件夹。
	 * @throws SQLException
	 * @throws IOException
	 */
	public  void sourceInfoAll(String projectHome) throws SQLException, IOException {
		for (List<Integer> list : id_commitId_fileIds) {
			sourceInfoEach(list,projectHome);
		}
	}

	private void sourceInfoEach(List<Integer> list, String projectHome) throws SQLException, IOException {
		if (list.get(1) != -1) {
			//System.out.println("extract from " + list.get(2) + "_"+ list.get(1) + "_" + list.get(0) + ".java");

			sql = "select patch from patches where commit_id="
					+ list.get(1) + " and file_id=" + list.get(2);
			bow = new Bow();
			// sql = "select patch from patches where id=2354";
			resultSet = stmt.executeQuery(sql);
			String patchString="";
			if (!resultSet.next()) {
				System.out.println("patches in commit_id=" + list.get(1)
						+ " and file_id=" + list.get(2) + " is empty!");
			}else {
				patchString= resultSet.getString(1);
			}
			
			StringBuffer sBuffer = new StringBuffer();
			if (!patchString.equals("") && patchString.indexOf("@") >= 0) {
				
					patchString = patchString.substring(patchString.indexOf("@"),
							patchString.length()).replaceAll("@@.*@@", "");
					for (String s : patchString.split("\\n{1,}")) {
						if (s.startsWith("+") || s.startsWith("-")) { 
							s = s.substring(1, s.length());    //д��̫����
							//s=s.replace("\\*", " ");
							//s=s.replace("*\\", " ");
							//s=s.replace("\\\\", " ");
							//s=s.replace("\"", " ");
							sBuffer.append(s + "\n");
						}
					}
				
			}
			
			
			String sourcePath = projectHome + "/" + list.get(2)
					+ "_" + list.get(1) + "_" + list.get(0) + ".java";
			BufferedReader bReader;
			try {
				bReader = new BufferedReader(new FileReader(sourcePath));
			} catch (FileNotFoundException e) {
				System.out.println("Not find the file " + projectHome + "/" + list.get(2) + "_"
						+ list.get(1) + "_" + list.get(0) + ".java");
				return;
			}
			String line;
			while ((line = bReader.readLine()) != null) {
				sBuffer.append(line + "\n");
			}
			bReader.close();
			Map<String, Integer> patch = null;
			
			try{
				patch = bow.bowP(sBuffer);
			}catch(Exception e){
			
				String error_file_path = error_dir + "/" + list.get(2)
				+ "_" + list.get(1) + "_" + list.get(0) + "_error.txt";
				File errorFile = new File(error_file_path);
				if(!errorFile.exists()){
					errorFile.createNewFile();
				}
				FileWriter fw = new FileWriter(error_file_path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(sBuffer.toString());
				bw.close();
				return;
			}
			
			for (String s : patch.keySet()) {
				content = writeInfo(s, content, list.get(1), list.get(2),
						patch.get(s));
			}
		}
	
		
	}

	/**
	 * 针对给定的commit_id,file_id对，将tent中s的值更新。
	 * 需要注意的是，这样的搭配导致extraction3提取的数据最后一个是逗号，
	 * 导致weka无法识别，这个问题在Merge类的merge123()方法中处理。
	 * 
	 * @param s
	 *            需要更新的属性
	 * @param tent
	 *            需要更新的包含实例的实例集。
	 * @param commitId
	 *            需要更新的实例对应的commit_id。
	 * @param fileId
	 *            需要更新的实例对应的file_id。
	 * @param value
	 *            需要更新的值。
	 * @return 新的实例集。
	 */
	public Map<List<Integer>, StringBuffer> writeInfo(String s,
			Map<List<Integer>, StringBuffer> tent, int commitId, int fileId,
			Integer value) {
		// boolean IsNewField = false;
		if (!currStrings.contains(s)) {
			// IsNewField = true;
			currStrings.add(s);
			String ColName = "s" + dictionary.size();
			dictionary.put(s, ColName);
			dictionary2.put(ColName, s);
			colMap.put(ColName, colMap.size() + 3);

			for (List<Integer> list : tent.keySet()) {
				if (list.get(1) == -1) {
					tent.get(headmap).append(ColName + ",");
				} else if (list.get(1) == commitId && list.get(2) == fileId) {
					tent.put(list, tent.get(list).append(value + ","));
				} else {
					tent.put(list, tent.get(list).append(0 + ","));
				}
			}
		} else {
			String column = dictionary.get(s);
			for (List<Integer> list : tent.keySet()) {
				if (list.get(1) == commitId && list.get(2) == fileId) {
					int index = colMap.get(column);
					StringBuffer newbuffer = new StringBuffer();
					String[] aStrings = tent.get(list).toString().split(",");
					for (int i = 0; i < index; i++) {
						newbuffer.append(aStrings[i] + ",");
					}
					int newValue = Integer.parseInt(aStrings[index] + value);
					newbuffer.append(newValue + ",");
					for (int i = index + 1; i < aStrings.length; i++) {
						newbuffer.append(aStrings[i] + ",");
					}
					tent.put(list, newbuffer);
				}
			}
		}
		return tent;
	}

	/**
	 * 获取path中的信息。
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public void pathInfo() throws SQLException, IOException {
		for (List<Integer> list : id_commitId_fileIds) {
			sql = "select current_file_path from actions where commit_id="
					+ list.get(1) + " and file_id=" + list.get(2);
			bow = new Bow();
			resultSet = stmt.executeQuery(sql);

			if (!resultSet.next()) {
				continue;
			}
			String path = resultSet.getString(1);
			Map<String, Integer> pathName = bow.bowPP(path);
			for (String s : pathName.keySet()) {
				content = writeInfo(s, content, list.get(1), list.get(2), // 两个函数可以整合为一个
						pathName.get(s));
			}
		}
	}

	/**
	 * ��ȡ��extraction3������.
	 * 
	 * @return
	 */
	public List<List<Integer>> getCommitId_fileIds() {
		return id_commitId_fileIds;
	}

	/**
	 * �������ݿ��е�extraction2����id_commitId_fileIds.
	 * 
	 * @param start
	 * @param end
	 * @throws SQLException
	 */
	public void setICFfromDatabase(int start, int end) throws SQLException {
		headmap = new ArrayList<>();
		headmap.add(-1);
		headmap.add(-1);
		headmap.add(-1);
		StringBuffer head = new StringBuffer("id,commit_id,file_id,");
		id_commitId_fileIds.add(headmap);
		content.put(headmap, head);

		if (start == -1 || end == -1) {
			sql = "select id,commit_id,file_id from extraction2";
		} else {
			sql = "select id,commit_id,file_id from extraction2 where id>="
					+ start + " and id<" + end;
		}

		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			List<Integer> temp = new ArrayList<>();
			temp.add(resultSet.getInt(1));
			temp.add(resultSet.getInt(2));
			temp.add(resultSet.getInt(3));
			id_commitId_fileIds.add(temp); // ��¼id,commit_id��file_id�Ա����á�
			StringBuffer write = new StringBuffer(resultSet.getInt(1) + ","
					+ resultSet.getInt(2) + "," + resultSet.getInt(3) + ",");
			content.put(temp, write);
		}

	}

	/**
	 * ���ݸ����Ĳ������ñ�extraction3������.
	 * 
	 * @param commitId_fileIds
	 */
	public void setCommitId_fileIds(List<List<Integer>> commitId_fileIds) {
		this.id_commitId_fileIds = commitId_fileIds;
	}

	/**
	 * ��ȡextraction3������.
	 * 
	 * @return
	 */
	public Map<List<Integer>, StringBuffer> getContent() {
		for (List<Integer> key : content.keySet()) {
			StringBuffer temp = content.get(key);
			content.put(key,
					new StringBuffer(temp.subSequence(0, temp.length() - 1)));
		}
		return content;
	}
	
	/*
	 * ���ı��ļ��ж�ȡ*/
	public static List<List<Integer>> getCfFromTxt(String path) throws NumberFormatException, IOException {
		List<List<Integer>> id_commitId_fileIds = new ArrayList<List<Integer>>() ;
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
			
			id_commitId_fileIds.add(ID_commitId_fileId);
		}
		return id_commitId_fileIds;
	}
	/**
	 * ���ñ�extraction3������.
	 * 
	 * @param content
	 */
	public void setContent(Map<List<Integer>, StringBuffer> content) {
		this.content = content;
	}

	/**
	 * ��ȡ�ı���������ֵ�.
	 * 
	 * @return
	 */
	public Map<String, String> getDictionary() {
		return dictionary2;
	}

	void writeContent(Map<List<Integer>, StringBuffer> cont, String path) throws Exception{
		System.out.println("Start write bow content into " + path + "...");
		File resultFile = new File(path);
		FileWriter fw = new FileWriter(resultFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
        
		try{
			bw.write(cont.get(headmap) + "\n");  
			
			for (List<Integer> list : id_commitId_fileIds) {
				bw.write((cont.get(list) + "\n"));  
				cont.remove(list);
			}
			bw.flush();
			bw.close();
			System.out.println("bow path:" + path + " finished!");
		}catch(Exception e){
			throw new Exception();
		}
	}
	
	@SuppressWarnings("unchecked")
	void writeDictionary(Map<String, String> dic, String path) throws Exception{
		System.out.println("Start write bow dictionary into " + path + "...");
		File resultFile = new File(path);
		FileWriter fw = new FileWriter(resultFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		Set mapSet = dic.entrySet();
		try{
			
			for (Object list : mapSet) {
				bw.write((Entry<String, String>)list + "\n");  
				//dic.remove(list);
			}
			bw.flush();
			bw.close();
			System.out.println("bow path:" + path + " finished!");
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception();
		}
	}
}