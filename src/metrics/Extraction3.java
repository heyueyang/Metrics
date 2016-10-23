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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ��ȡԴ����Ϣ·����Ϣ��
 * 
 * @param id_commitId_fileIds
 *            ������id��commit_id��file_id���ɵ������б�
 * @param dictionary
 *            ���ʵ���������ƺ����Դ������ƶԵ�map��keyֵΪʵ���������ơ�
 * @param dictionary2
 *            ������Դ������ƺ�ʵ���������ƶԵ�map��
 * @param currStrings
 *            ��ǰ���ֹ������ԡ�
 * @param bow
 *            ������ȡԴ����Ϣ·����Ϣ�Ĵ������ࡣ
 * @param content
 *            ʵ�ʵõ��ĸ�ʵ����keyΪid_commitId_fileIds�е�Ԫ�أ�valueΪ��Ӧ������ֵ����keyֵΪ(-1,-1,-1)
 *            ʱ��Ӧ��ֵΪ�������ơ�
 * @param colMap
 *            content�����Լ���������map����Ϊ�ڳ�������ʵ�������ݵĹ����У�ĳ��ʵ����ĳ������ֵ������Ҫ�ı�
 *            ��ɸ��ݴ�map���ٶ�Ӧ��content�и����Ե�ֵ��Ȼ�����޸ġ�
 * @param headmap
 *            content�е������ֶΣ�����������������Ƶ�map��
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
	static String bowDir = "/home/yueyang/data/bow m0/";
	static String error_dir = "/home/yueyang/data/error/";
	static String recoverDir = "/home/yueyang/data/info_new/";
	static String[] projects = {"itextpdf"};//"ant", "camel", "eclipse", 
	//, "jEdit", "lucene", "tomcat", "voldemort"
	static String project = "itextpdf";
	//
	static String projectHome = "/home/yueyang/project/recover_new/";
	
	public static void main(String[] args) throws Exception {
		
		for(int i = 0; i < projects.length; i++){
			System.out.println("Write " + projects[i] + " bow metrics start...");
			File resultFile = new File(bowDir + projects[i] + "Bow.csv");
			if(resultFile.exists()){
				continue;
			}
			Map<List<Integer>, StringBuffer> bow = null;
			Extraction3 extra = new Extraction3(projects[i], getCfFromTxt(recoverDir + projects[i] + "Recover.txt"), projectHome + projects[i] +"AllFiles/");
			List<Integer> list = new ArrayList<Integer>();
			list.add(12);
			list.add(3233);
			list.add(5329);
			extra.sourceInfoEach(list,projectHome + projects[i] +"AllFiles/");
			//bow = extra.getContent();
			//writeContent(bow, projects[i], bowDir);
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
	 * ��ȡ����������Ϣ��
	 * 
	 * @param database
	 *            ��Ҫ���ӵ����ݿ�
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
		dictionary = new HashMap<>();
		dictionary2 = new HashMap<>();
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
		dictionary = new HashMap<>();
		dictionary2 = new HashMap<>();
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
	 * ��ʼ��ʵ������keyset��ʵ��������extraction1�а�������ϢԶ����ʵ����Ҫ�����ģ�
	 * ����Ĭ�Ϸ���(start==-1||end==-1)ʱ��Ӧ����extraction2Ϊ��׼������
	 * 
	 * @param start
	 *            ��Ϊ-1������extraction2�е�������Ϊ��׼�õ�����ʵ�������ָ������ֵ�������extraction2�е��Ӽ�Ϊ��׼
	 * @param end
	 *            ��Ϊ-1������extraction2�е�������Ϊ��׼�õ�����ʵ�������ָ������ֵ�������extraction2�е��Ӽ�Ϊ��׼
	 * @throws SQLException
	 * @throws IOException
	 */
	public void initial(int start, int end) throws SQLException, IOException {

	}

	/**
	 * ��content�����ָ����commit_id����������s��ֵ��
	 * �˺�����Ҫ���pathinfo��changelog����Ϊpath��Ϣ����changelog��Ϣֻ��commit_id�йأ���file_id�޹ء�
	 * 
	 * @param s
	 *            �������ơ������ǰ���Լ������и����ԣ�����ļ���ÿ��ʵ�����¸����Ե�ֵ�� ���������Լ�����Ӹ����ԣ�����ʼ��������ֵ��
	 * @param tent
	 *            ��ǰ���е���Ϣ��
	 * @param commitId
	 *            ��Ҫ���µ�ʵ����commit_id��
	 * @param value
	 *            ��Ҫ���µ�ֵ��
	 * @return �������ݺ��content��
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
			// ������ʵ��������ȡcontent�еļ�Ҫ��������Ȼ����ݼ�Ҫ���������ٵõ������Զ�Ӧ���кš�
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
	 * ��ȡ���е�changelog��Ϣ,���������content�� ע�⣺changelog��Ϣֻ��commit_id�йأ���file_id�޹ء�
	 * 
	 * @param csvFile
	 * @throws SQLException
	 * @throws IOException
	 */
	public void changeLogInfo() throws SQLException, IOException {
		bow = new Bow();
		// ������в�ͬ��commit_id
		Set<Integer> commit_ids = new LinkedHashSet<>();
		for (List<Integer> list : id_commitId_fileIds) {
			if (!commit_ids.contains(list.get(1))) {
				commit_ids.add(list.get(1));
			}
		}
		// ��ÿ��commit_id����ȡ��changelog ��Ϣ��������content��
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
	 * ��ȡԴ���Դ���иĶ��Ĵ�����Ϣ��
	 * ���ÿ�����ĵ�(commit_id,file_id)�ԣ������(���ĳ�θ�������Ϊd����ɾ����ĳ���ļ�����ô��û�ж�Ӧ���ļ�)
	 * ��Ӧһ��java�ļ��� ͬʱ���Ӧ��һ��patch����Ҫ���ݽű�������ǰ���������Щ�����˵��ļ�����ͨ�����ݿ������е�patch��Ϣ
	 * Ȼ��ʹ�ô˺�����ȡԴ���е�һЩ��Ϣ��
	 * 
	 * @param projectHome
	 *            ����������Ҫ��ȡ��Ϣ��javaԴ����ļ��С�
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
			System.out.println("extract from " + list.get(2) + "_"
					+ list.get(1) + "_" + list.get(0) + ".java");

			sql = "select patch from patches where commit_id="
					+ list.get(1) + " and file_id=" + list.get(2);
			bow = new Bow();
			// sql = "select patch from patches where id=2354";
			resultSet = stmt.executeQuery(sql);
			String patchString="";
			if (!resultSet.next()) {
				System.out.println("patches in commit_id=" + list.get(1)
						+ " and file_id" + list.get(2) + " is empty!");
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
			File sourceFile = new File(projectHome + "/" + list.get(2)
					+ "_" + list.get(1) + "_" + list.get(0) + ".java");
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
	 * ��Ը�����commit_id,file_id�ԣ���tent��s��ֵ���¡�
	 * ��Ҫע����ǣ������Ĵ��䵼��extraction3��ȡ���������һ���Ƕ��ţ�
	 * ����weka�޷�ʶ�����������Merge���merge123()�����д���
	 * 
	 * @param s
	 *            ��Ҫ���µ�����
	 * @param tent
	 *            ��Ҫ���µİ���ʵ����ʵ������
	 * @param commitId
	 *            ��Ҫ���µ�ʵ����Ӧ��commit_id��
	 * @param fileId
	 *            ��Ҫ���µ�ʵ����Ӧ��file_id��
	 * @param value
	 *            ��Ҫ���µ�ֵ��
	 * @return �µ�ʵ������
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
	 * ��ȡpath�е���Ϣ��
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
				content = writeInfo(s, content, list.get(1), list.get(2), // ����������������Ϊһ��
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
	/*
	 * ��content�����csv�ļ���*
	 */
	static void writeContent(Map<List<Integer>, StringBuffer> cont, String project, String dir) throws SQLException, IOException{

		File resultFile = new File(dir + project + "Bow.csv");
		FileWriter fw = new FileWriter(resultFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(cont.get(headmap) + "\n");
		for (List<Integer> list : id_commitId_fileIds) {
			//sBuffer.append(list.get(1) + "," + list.get(2) + ",");
			sBuffer.append(cont.get(list) + "\n");
		}
		bw.write(sBuffer.toString());
		bw.close();

	}
}