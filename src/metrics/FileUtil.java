package metrics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class FileUtil {
	
	/*public static void exportFile(double[][] res,double[] m,String file,String[] head, char[] threshold) throws Exception {  
		try { 
	
			WritableWorkbook wbook = Workbook.createWorkbook(new File(file)); //����excel�ļ�  
			WritableSheet wsheet = wbook.createSheet("result", 0); //����������  
			jxl.write.Label content = null;
			for(int j = 0;j<res[0].length; j++){
				if(j<head.length){
				content = new jxl.write.Label(j, 0, head[j]);
				}else{
					content = new jxl.write.Label(j, 0, "");
				}
				
				wsheet.addCell(content);
			}
			for(int i = 0; i<res.length; i++)
			{//��
				for(int j = 0;j<res[i].length; j++)
				{//��
					content = new jxl.write.Label(j, i+1, String.valueOf(res[i][j])); 
					wsheet.addCell(content);
				}

			} 
			content = new jxl.write.Label(0, (res.length+1), "threshold"); 
			wsheet.addCell(content);
			content = new jxl.write.Label(1, (res.length+1), String.valueOf(threshold)); 
			wsheet.addCell(content);

			for(int j = 0;j<m.length; j++){		
				content = new jxl.write.Label(j, (res.length+2) , String.valueOf(m[j]));		
				wsheet.addCell(content);
			}
			wbook.write(); //д���ļ�  
			wbook.close();  
		} catch (Exception e) {  
			throw new Exception("�����ļ�����");  
		}  
	}
	*/
	public static void exportObjFile(ArrayList<double[]> array,String file,String[] head) throws Exception {  
		Object[] objects = array.toArray();
		WritableWorkbook wbook = Workbook.createWorkbook(new File(file)); //����excel�ļ�  
		WritableSheet wsheet = wbook.createSheet("result", 0); //����������  
		jxl.write.Label content = null;
		for(int j = 0;j<head.length; j++){
			if(j<head.length){
			content = new jxl.write.Label(j, 0, head[j]);
			}else{
				content = new jxl.write.Label(j, 0, "");
			}
			
			wsheet.addCell(content);
		}
		double[] temp =  new double[5];
		for(int i = 0; i<objects.length; i++)
		{//��
			temp = (double[])objects[i];
			for(int j = 0; j<temp.length; j++){
				//System.out.println(j);
				content = new jxl.write.Label(j, i+1, String.valueOf(temp[j])); 
				//content = new jxl.write.Label(j, i+1, objects[i].toString()); 
				wsheet.addCell(content);
			}
		} /**/
	try { 
		wbook.write(); //д���ļ�  
		wbook.close();  
	} catch (Exception e) {  
		throw new Exception("�����ļ�����");  
	}  
}
	//�����ά����д��xls�ļ�
	public static void outFile(String path, String[][] res, String[] head) throws IOException, WriteException, WriteException{

		WritableWorkbook wbook = Workbook.createWorkbook(new File(path)); //����excel�ļ�  
		WritableSheet wsheet = wbook.createSheet("result", 0); //����������  
		//String[] temp = {"Evaluation","Search","AttrNum","accuracy","sensitive","precision","time"};
		jxl.write.Label content = null;

		for(int j = 0; j < head.length; j++){
			content = new jxl.write.Label(j, 0, head[j]);
			wsheet.addCell(content);
		}
		for(int i = 0; i < res.length; i++)
		{//��
			
			for(int j = 0; j < res[0].length; j++){
				content = new jxl.write.Label(j, i+1, res[i][j]);
				wsheet.addCell(content);
			}
		}
		
		/*
		 * content = new jxl.write.Label(res[0].length, 1, eval);
		 */
		/*
		wsheet.addCell(content);
		content = new jxl.write.Label(res[0].length+1, 1, search);
		wsheet.addCell(content);
		content = new jxl.write.Label(res[0].length+2, 1, String.valueOf(anomaly));
		wsheet.addCell(content);
		content = new jxl.write.Label(res[0].length+3, 1, String.valueOf(ind));
		wsheet.addCell(content);
		content = new jxl.write.Label(res[0].length+2, 2, String.valueOf(valueCnt[0]));
		wsheet.addCell(content);
		content = new jxl.write.Label(res[0].length+3, 2, String.valueOf(valueCnt[1]));
		wsheet.addCell(content);
		*/
		wbook.write(); //д���ļ�  
		wbook.close(); 		

	}
	
	public static void exportFile(String[][] res,String file,String project, int sh, String[] head) throws Exception {  
		try { 
			File resultFile = new File(file);
			WritableWorkbook wbook = null;
			WritableSheet wsheet = null;
			if(!resultFile.exists()){
				wbook = Workbook.createWorkbook(resultFile); //����excel�ļ�  
				wsheet = wbook.createSheet("result",0); //����������
			}else{
			
				Workbook wb = Workbook.getWorkbook(resultFile);
				wbook = Workbook.createWorkbook(resultFile,wb); //����excel�ļ�  
				wsheet = wbook.getSheet(0); //����������  
			}
			
			//WritableSheet wsheet = wbook.createSheet("result",0); //����������  

			
			int row = wsheet.getRows();
			System.out.println("======Rows:"+row+"======");
			
			jxl.write.Label content = null;
			if(row<=0){
				/*for(int j = 0;j<res[0].length; j++){
						if(j<head.length){
					content = new jxl.write.Label(j, 0, head[j]);
					}else{
						content = new jxl.write.Label(j, 0, "");
					}
					wsheet.addCell(content);
				}*/
				for(int j = 0; j < head.length; j++){
					content = new jxl.write.Label(j, 0, head[j]);
					wsheet.addCell(content);
				}
			}
			
			row = wsheet.getRows();
			content = new jxl.write.Label(0, row, project); 
			wsheet.addCell(content);
			
			for(int i = 0; i<res.length; i++)
			{//��
				for(int j = 0;j<res[i].length; j++)
				{//��
					content = new jxl.write.Label(j, row+i+1, String.valueOf(res[i][j])); 
					wsheet.addCell(content);
				}
			} 

			wbook.write(); //д���ļ�  
			wbook.close();  
		} catch (Exception e) {  
			System.out.println(e.toString());
			throw new Exception();  
		}  
		
	}
	
	public static Instances ReadData(String filePath){
	    System.out.println("======Read data from " + filePath + "======");
		if(!new File(filePath).exists()){
			System.out.println("======This File doesn't exist!" + "======");
			return null;
		}
		Instances ResultIns = null;
		//��������ѡ��
		File fData = new File(filePath);	
		ArffLoader loader = new ArffLoader();
		try {
			loader.setFile(fData);
			ResultIns = loader.getDataSet();
			System.out.println("=========Data Information=========");
		    System.out.println("======AttrNum:"+ResultIns.numAttributes()+"======");
		    System.out.println("======InstancesNum:"+ResultIns.numInstances()+"======");
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return ResultIns;
}
	
	public static Instances ReadDataCSV(String filePath){
	    System.out.println("======Read data from " + filePath + "======");
		if(!new File(filePath).exists()){
			System.out.println("======This File doesn't exist!" + "======");
			return null;
		}
		Instances ResultIns = null;
		//��������ѡ��
		File fData = new File(filePath);	
		CSVLoader loader = new CSVLoader();
		try {
			loader.setSource(fData);
			ResultIns = loader.getDataSet();
			System.out.println("=========Data Information=========");
		    System.out.println("======AttrNum:"+ResultIns.numAttributes()+"======");
		    System.out.println("======InstancesNum:"+ResultIns.numInstances()+"======");
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return ResultIns;
}
	public static boolean WriteData(Instances ins,String filePath){
	    
		if(new File(filePath).exists()){
			System.out.println("======" + filePath + "already exist!======");
			return false;
		}
		ArffSaver saver = new ArffSaver(); 
	    saver.setInstances(ins);  
	    try {
			saver.setFile(new File(filePath));
			saver.writeBatch(); 
		    System.out.println("==Arff File Writed:"+filePath+"==");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}  
	    
	    return true;

		
}
public static boolean WriteArff2CSV(Instances ins,String filePath){
	    
		if(new File(filePath).exists()){
			System.out.println("======" + filePath + "already exist!======");
			return false;
		}
		weka.core.converters.CSVSaver saver = new CSVSaver();
	    saver.setInstances(ins);  
	    try {
			saver.setFile(new File(filePath));
			saver.writeBatch(); 
		    System.out.println("==CSV File Writed:"+filePath+"==");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}  
	    
	    return true;

		
}
	public static String getFileName(String filePath){
		return filePath.substring(filePath.lastIndexOf("\\")+1, filePath.lastIndexOf("."));//��ȡ�ļ���
	}
	
	public static int anomalyIndex(Instances ins){
		int valueCnt[] = {0,0};
	    int temp = 0;
	    int ind = 0;
	    valueCnt[0] = 0;
	    valueCnt[1] = 0;
	    for(int j=0;j < ins.numInstances(); j++){
	    		temp = (int) ins.instance(j).classValue();
	    		valueCnt[temp]++;
	    	}
	   	//ind = (valueCnt[0] > valueCnt[1]) ? valueCnt[1] : valueCnt[0];
	    ind = (valueCnt[0] > valueCnt[1]) ? 1 : 0;
	   	//System.out.println("======anomaly cnt="+valueCnt[0]+"======"+valueCnt[1]);
		return ind;
	 } 

	public static void Write2TempArff(List<Integer> fileList, Map<Integer, List<Integer>> data) throws IOException{
		String outPath = "E:\\dataset\\change7.0\\asso\\" + "test.arff";
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(outPath));
		//StringBuffer sb = new StringBuffer();
		bWriter.append("@relation files" + "\n");
		
		for(int i = 0 ; i < fileList.size(); i++){
			String file_id = String.valueOf(fileList.get(i));
			bWriter.append("@attribute '" + file_id + "' { t}" + "\n");
		}
		bWriter.append("@data" + "\n");
		Set<Integer> commitIds = data.keySet();
		int[] cnt = new int[fileList.size()];
		int n = 0;
		for(Integer commit_id : commitIds){
			System.out.print(n++ + ":" + "\t");
			List<Integer> list = data.get(commit_id);
			for(int j = 0 ; j < list.size(); j++){
				System.out.print(list.get(j) + "\t");
			}
			System.out.println();
		
			StringBuffer temp = new StringBuffer();
			for(int j = 0 ; j < fileList.size(); j++){
				if(list.contains(fileList.get(j))){
					temp.append("t"+",");
					cnt[j]++;
				}else{
					temp.append("?"+",");
				}
			}
			bWriter.append(temp.subSequence(0, temp.length()-1)+"\n");
		}
			
			bWriter.flush();
			bWriter.close();
			System.out.println("writed into" + outPath);
	
			//for(int j = 0 ; j < fileList.size(); j++){
			//	System.out.println(j+ "," + fileList.get(j) + "," + cnt[j]);
			//}
	}

}
