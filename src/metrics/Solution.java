package metrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solution {
	public static void main(String[] args) throws IOException{
		fuck();
	}

	public static void fuck() throws IOException {
		File fa=new File("/mnt/hgfs/vmware share/data/arff_selected_best/");
		File[] ch=fa.listFiles();
		Set<String> attrbutes=new LinkedHashSet<>();
		Map<String,Set<String>> array=new HashMap<String,Set<String>>();
		for (File file : ch) {
			System.out.println("=========="+file.getName()+"==========");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			Set<String> Mset=new HashSet<>();
			Set<String> content=new HashSet<>();
			while ((line = br.readLine()) != null) {
				if (line.startsWith("@data")) {
					break;
				}
				if (line.startsWith("@attribute")) {
					if (line.split(" ")[1].startsWith("s")) {
						Mset.add(line.split(" ")[1]);
					}else {
						content.add(line.split(" ")[1]);
						attrbutes.add(line.split(" ")[1]);
					}
				}
			}
			br.close();
			String arffName=file.getName().substring(0, file.getName().indexOf("_"));
			String textName=arffName + "Dic.csv";
			File textFile=new File("/mnt/hgfs/vmware share/data/dic_csv/"+textName);
			br=new BufferedReader(new FileReader(textFile));
			while ((line=br.readLine())!=null) {
				String num=line.substring(0,line.indexOf("="));
				String word=line.substring(line.indexOf("=")+1);
	
				if (Mset.contains(num)) {
					content.add(word);
					attrbutes.add(word);
				}
			}
			array.put(arffName,content);
			br.close();
		}

		Map<String,StringBuilder> con=new HashMap<String,StringBuilder>();
		Map<String, Integer> attrFreq = new HashMap<String, Integer>();
		for (String file : array.keySet()) {
			Set<String> set = array.get(file);
			StringBuilder stringBuilder=new StringBuilder();

			for (String string : attrbutes) {
				if (set.contains(string)) {
					if(!attrFreq.containsKey(string)){
						attrFreq.put(string, 1);
					}else{
						attrFreq.put(string, attrFreq.get(string)+1);
					}
					stringBuilder.append("1,");
				}else {
					stringBuilder.append("0,");
				}
				
			}
			
			con.put(file,stringBuilder);
		}
		StringBuilder freqBuilder=new StringBuilder();
		for (String string: attrbutes) {
			freqBuilder.append(attrFreq.get(string)+",");
		}
		con.put("Frequency", freqBuilder);
		
		File csvFile=new File("/mnt/hgfs/vmware share/data/attr_selected.csv");
		if (!csvFile.exists()) {
			csvFile.createNewFile();
		}
		BufferedWriter bWriter=new BufferedWriter(new FileWriter(csvFile));
		StringBuilder title=new StringBuilder();
		title.append("Attribute"+",");
		for (String string: attrbutes) {
			title.append(string+",");
		}
		bWriter.append(title+"\n");
		
		for (String file : con.keySet()) {
			StringBuilder stringBuilder=con.get(file);
			bWriter.append(file+","+stringBuilder+"\n");
		}
		bWriter.flush();
		bWriter.close();
	}

}
