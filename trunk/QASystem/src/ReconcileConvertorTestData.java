import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


public class ReconcileConvertorTestData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String topDocsDirStr = QA.properties.getProperty("testTopDocsDir");
			String reconcileDirStr = QA.properties.getProperty("testReconcileDir");
			
			File reconcileDir = new File(reconcileDirStr);
			if(!reconcileDir.exists())
				reconcileDir.mkdirs();
			
			File filelist = new File(reconcileDir, "test.filelist");
			BufferedWriter listWriter = new BufferedWriter(new FileWriter(filelist));
			
			File topDocsDir = new File(topDocsDirStr);
			File[] qidDirs = topDocsDir.listFiles();
			for (File qidDir : qidDirs) {
				File[] files = qidDir.listFiles();
				for (File file : files) {
					File oDir = new File(reconcileDir, qidDir.getName()+"_"+file.getName());
					if(!oDir.exists())
						oDir.mkdirs();
					QAUtils.cp(file, new File(oDir, "raw.txt"));
					listWriter.append(oDir.getName()+"\n");
				}
			}
			listWriter.flush();
			listWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
