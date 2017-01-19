package metrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class Designate {

	/**
	 * @param args
	 */
	static String data_folder = "/home/yueyang/data/complexity_csv/";
	static String result_folder = "/home/yueyang/data/com_csv/";
	public static void main(String[] args) {
		// TODO Auto-generated method stub

    	File f = new File(result_folder);
    	if(!f.exists()) f.mkdirs();
  
    	File ff = new File(data_folder);//select_folder
    	File[] files = ff.listFiles();
    	for(int i = 0;i < files.length; i++){
    		try{
    			String filePath = files[i].getAbsolutePath();
    			System.out.println("--->" + filePath);
    			String result_path = result_folder + files[i].getName().substring(0, files[i].getName().lastIndexOf(".")) + ".csv";
    			System.out.println("====" + result_path);
    			
    			File file = new File(result_path);
    			if (file.exists()) {
    				continue;
    			}else{
    				file.createNewFile();
    			}
    				
    			FileWriter fw = new FileWriter(result_path);
    			BufferedWriter bw = new BufferedWriter(fw);
    			BufferedReader bReader = new BufferedReader(new FileReader(filePath));
    			//get the label list
        		LinkedList<String> thres = Designate.excute(filePath);
    			//write the head
    			String line = bReader.readLine();
    			if(line != null) bw.write(line + "\n");

    			int ind = 0;
    			while ((line = bReader.readLine()) != null) {
    				int j = 0;
    				bw.write(line.substring(0,line.lastIndexOf(",")+1) + thres.get(ind++) + "\n");
    				
    			}
    			bw.flush();
				bw.close();
    			
				
    		}catch(Exception e){
    			e.printStackTrace();
    			continue;
    		}
    	}
	}
	
	static LinkedList<String> excute(String path) throws IOException{
		LinkedList<String> list = new LinkedList<String>();
		try {	
			BufferedReader bReader = new BufferedReader(new FileReader(path));
			String line = bReader.readLine();
			String[] temp = line.split(",");
			int i = 0, change_ind = 0;
			//get the index of ChangLloc
			while(i < temp.length){
				if(temp[i].equals("changeloc")){
					change_ind = i;
					break;
				}
				i++;
			}
			//get threshold
			//System.out.println(change_ind);
			int thres = getThreshold(path, change_ind);
			System.out.println(thres);
			//get label for every instances
			while ((line = bReader.readLine()) != null) {
				//System.out.println(line.split(",")[change_ind] + "     " + (Integer.valueOf(line.split(",")[change_ind])>thres?"TRUE":"FALSE"));
				list.add(Integer.valueOf(line.split(",")[change_ind])>thres?"true":"false");
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;

	}
	
	static int getThreshold(String path, int ind) throws IOException{
		int thres = 0;
		LinkedList<Integer> list = new LinkedList<Integer>();
		try {	
			BufferedReader bReader = new BufferedReader(new FileReader(path));
			String line = bReader.readLine();
			//add the ChangeLoc into a List
			while ((line = bReader.readLine()) != null) {
				list.add(Integer.valueOf(line.split(",")[ind]));
			}
			//calculate the threshold
			thres = Calculate(list);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return thres;

	}
	
	private static int Calculate(LinkedList<Integer> list) throws IOException{
		int res = 0;
		//calculate the threshold between anomalies and nomal instances
		//(1)default: use 0 to be the threshold
		//(2)use box to find the threshold 
		//Arrays.sort(changeLoc);
		//res = Box(list);
		res = Pareto(list);
		return res;

	}
	
	private static int Box(LinkedList<Integer> list) throws IOException{
		int[] changeLoc = new int[list.size()];
		int i = 0;
		while(i<list.size()){
			changeLoc[i] = list.get(i++);
		}
		Arrays.sort(changeLoc);
		int bottomInd = changeLoc.length*1/4, topInd = changeLoc.length*3/4;
		
		int bottom = changeLoc[bottomInd], top = changeLoc[topInd];
		//System.out.println(changeLoc.length + ":" + bottomInd + "---" + topInd + "||" + bottom + "---" + top);
		return (int) (top + 1.5*(top - bottom));

	}
	
	private static int Pareto(LinkedList<Integer> list) throws IOException{
		int[] changeLoc = new int[list.size()];
		int i = 0;
		while(i<list.size()){
			changeLoc[i] = list.get(i++);
		}
		Arrays.sort(changeLoc);
		return changeLoc[(int) (changeLoc.length*0.8)];

	}


}
