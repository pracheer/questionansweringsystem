import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;


public class ReconcileConvertor {
	
	
	public static void main(String[] args) {
		try {
			Properties properties = new Properties();
			properties.load(new FileReader(new File("data/QA.properties")));
			
			File reconcileDir = new File(properties.getProperty("reconcileDir"));
			reconcileDir.mkdirs();
			
			File topDocsDir = new File(properties.getProperty("topDocsDir"));
			File[] files = topDocsDir.listFiles();
			BufferedWriter writer = null; 
			
			BufferedWriter fileListWriter = new BufferedWriter(new FileWriter(new File(reconcileDir, "test.filelist")));
			
			for (File file : files) {
//				System.out.println("converting file:"+file);
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String str;
				int qid = -1;
				int rank = -1;
				float score = 0;
				while(null!=(str=reader.readLine())) {
					if(str.startsWith("Qid:")) {
						String[] split = str.split("\t");
						for (String string : split) {
							String[] split2 = string.split(": ");
							if(split2[0].equalsIgnoreCase("Qid"))
								qid = Integer.parseInt(split2[1]);
							else if(split2[0].equalsIgnoreCase("Rank"))
								rank = Integer.parseInt(split2[1]);
							else if(split2[0].equalsIgnoreCase("Score"))
								score = Float.parseFloat(split2[1]);
						}
						if(null!=writer) {
							writer.flush();
							writer.close();
						}
						File file2 = new File(reconcileDir + File.separator + qid+"_"+rank);
						file2.mkdirs();
						fileListWriter.append(file2.getName() + "\n");
						writer = new BufferedWriter(new FileWriter(new File(file2, "raw.txt")));
					}
					str = str.replaceAll("<(.*?)>", " ").trim();
					if(str.isEmpty())
						continue;
					writer.write(str + "\n");
				}
				
			}
			writer.flush();
			writer.close();
			fileListWriter.flush();
			fileListWriter.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
