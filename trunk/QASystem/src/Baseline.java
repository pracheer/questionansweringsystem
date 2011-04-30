import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.xml.sax.helpers.DefaultHandler;

import com.thoughtworks.xstream.XStream;


public class Baseline extends DefaultHandler{

	private static final String SEPARATOR = " ";
	private static final String TOP_DOCS = "top_docs.";
	static File topDocsDir = new File("D:/study/natural language processing/Assignments/QASystem/DATA_201-399/topdocs_201-399/top_docs");
	static File outputDir = new File(topDocsDir.getParentFile() + File.separator + "output");
	static boolean learn = false;
	static File questionsFile = new File("D:/study/natural language processing/Assignments/QASystem/questions.txt");
	static File answersFile = new File("D:/study/natural language processing/Assignments/QASystem/output.txt");
	static boolean overwrite = false;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			XStream xstream = new XStream();
			
			// consists of qid and list of all the documents arranged by ranks.
			HashMap<Integer, ArrayList<Document>> map;
			
			// to store the map once it has learnt it.
			File learntFile = new File("data/object");
			
			if (learn) {
				map = learn();
				if(!learntFile.exists())
					learntFile.createNewFile();
				xstream.toXML(map, new FileWriter(learntFile));
				System.err.println("Documents created in memory and saved to a file:"+learntFile);
			}
			else {
				map = (HashMap<Integer, ArrayList<Document>>)xstream.fromXML(new FileReader(learntFile));
				System.err.println("Learnt object retrieved from memory");
			}
			
			System.out.println("Model trained/retrieved");
			
			System.err.println("Starting to parse Questions File:"+questionsFile);
			Questions questions = Questions.parseQuesFile(questionsFile);
			System.err.println("Questions File parsed.");
			
			System.err.println("Finding answers to questions.");
			BufferedWriter answer = new BufferedWriter(new FileWriter(answersFile));
			Collection<Question> quesList = questions.getQuestions();
			for (Question question : quesList) {
				TreeMap<Float, String> solutions = getAnswer(map, question);
				
				Set<Float> scores = solutions.keySet();
				for (Float score : scores) {
					String string = solutions.get(score);
					answer.write(question.getQid() + /*" "+score + */ " "+ string + "\n");
				}
			}
			answer.flush();
			answer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private static HashMap<Integer, ArrayList<Document>> learn() {
		HashMap<Integer, ArrayList<Document>> map;
		File[] files = topDocsDir.listFiles();
		map = new HashMap<Integer, ArrayList<Document>>();
		for (File inputFile : files) {
			File xmlFile = new File(outputDir, inputFile.getName()
					+ ".xml");
			if(!xmlFile.exists() || overwrite)
				XMLTrec.convertToXML(inputFile, xmlFile);
			ArrayList<Document> docs = XMLTrec.parse(xmlFile);
			map.put(docs.get(0).qid, docs);
		}
		return map;
	}
	private static TreeMap<Float, String> getAnswer(
			HashMap<Integer, ArrayList<Document>> map, Question question) {
		System.out.println("Reading question:"+question.getQid());
		System.out.println(question);
		ArrayList<Document> docs = map.get(question.getQid());
		// score, answers
		TreeMap<Float, String> solutions = new TreeMap<Float, String>(Collections.reverseOrder()); 
		for (Document doc : docs) {
			ArrayList<String> ansWords = new ArrayList<String>();
			float score = doc.search(question.getString(), ansWords);
			StringBuffer strBuf = new StringBuffer();
			for(int i = 0; i < ansWords.size(); i++) {
				strBuf.append(ansWords.get(i) + " ");
				if(((i%10==0&i>0) || i==ansWords.size()-1) && strBuf.length()>0) {
					solutions.put((int)doc.score*score*10000, TOP_DOCS+doc.getQid() + SEPARATOR + strBuf.toString().trim());
					strBuf.setLength(0);
				}
			}
		}
		return solutions;
	}

}
