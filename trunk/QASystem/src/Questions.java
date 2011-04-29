import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;


public class Questions {

	HashMap<Integer, String> questions;
	
	public Questions() {
		questions = new HashMap<Integer, String>();
	}
	
	public void addQuestion(Integer qid, String question) {
		questions.put(qid, question);
	}
	
	public int getCount() {
		return questions.size();
	}
	
	public Set<Integer> getQids() {
		return questions.keySet();
	}
	
	public String getQuestion(int qid) {
		return questions.get(qid);
	}
	
	public static Questions parseQuesFile(File questionsFile) {
		try {
			
			Questions thisObj = new Questions();
			BufferedReader reader = new BufferedReader(new FileReader(questionsFile));
			String str;
			int qid = -1;
			boolean description = false;
			while((str=reader.readLine())!=null) {
				str = str.trim();
				if(str.isEmpty())
					continue;
				if(str.startsWith("<num>")) {
					String[] columns = str.split(" ");
					qid = Integer.parseInt(columns[2]);
					continue;
				}
				if(str.startsWith("<desc>")) {
					description = true;
					continue;
				}
				if(description) {
					thisObj.addQuestion(qid, str);
					description = false;
				}
			}
			
			return thisObj;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
