import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.ParsedDocument;
import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.ScoredExtentResult;


public class Baseline {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			IndexEnvironment indexEnv = new IndexEnvironment();
			File file = new File("D:\\study\\natural language processing\\Assignments\\QASystem\\DATA_201-399\\topdocs_201-399\\top_docs\\top_docs.201");
			BufferedReader reader = new BufferedReader(new FileReader(file)); 
			String str;
			while((str=reader.readLine())!=null) {
				indexEnv.addString("201", str, null);
			}
			
			QueryEnvironment qEnv = new QueryEnvironment();
			qEnv.addIndex(indexEnv.toString());
			
			ScoredExtentResult[] results = qEnv.runQuery("What was the name of the first Russian astronaut to do a spacewalk?", 10);
			
			ParsedDocument[] documents = qEnv.documents(results);
			for (ParsedDocument doc : documents) {
				
				System.out.println(doc.text);
				System.out.println(doc.content);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
