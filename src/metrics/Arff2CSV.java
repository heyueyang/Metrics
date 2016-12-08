package metrics;

import java.io.File;

import weka.core.Instances;

public class Arff2CSV {
	private static String selectedDirPath = "/home/yueyang/data/selected_arff/";
	private static String resultDirPath = "/home/yueyang/data/selected_csv/";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File Dir = new File(selectedDirPath);
		File[] files = Dir.listFiles();

		
    	for(int i = 0;i < files.length; i++){
    		String filaName = files[i].getName();
    		System.out.println("------- " + filaName + " -------");
    		File resDir = new File(resultDirPath);
    		if(!resDir.exists()){
    			resDir.mkdirs();
    		}
    		String resultPath = resDir + filaName.substring(0, filaName.lastIndexOf(".")) + ".csv";
    		Instances ins = FileUtil.ReadData(files[i].getAbsolutePath());
    		FileUtil.WriteArff2CSV(ins, resultPath);
    		
    		System.out.println("------- finished ! -------");
    	}
	}

}
