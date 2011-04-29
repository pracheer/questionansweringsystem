import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.xml.sax.helpers.DefaultHandler;

import com.thoughtworks.xstream.XStream;


public class Baseline extends DefaultHandler{

	private static final String SEPARATOR = "::";
	static File topDocsDir = new File("D:/study/natural language processing/Assignments/QASystem/DATA_201-399/topdocs_201-399/top_docs");
	static File outputDir = new File(topDocsDir.getParentFile() + File.separator + "output");
	static boolean learn = true;
	static File questionsFile = new File("D:/study/natural language processing/Assignments/QASystem/questions.txt");
	static File answersFile = new File("D:/study/natural language processing/Assignments/QASystem/output.txt");
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			XStream xstream = new XStream();
			
			// consists of qid and list of all the documents arranged by ranks.
			HashMap<Integer, ArrayList<Document>> map;
			
			// to store the map once it has learnt it.
			File learntFile = new File("/data/object");
			
			if (learn) {
				File[] files = topDocsDir.listFiles();
				map = new HashMap<Integer, ArrayList<Document>>();
				for (File inputFile : files) {
					File xmlFile = new File(outputDir, inputFile.getName()
							+ ".xml");
					XMLTrec.convertToXML(inputFile, xmlFile);
					ArrayList<Document> docs = XMLTrec.parse(xmlFile);
					map.put(docs.get(0).qid, docs);
				}
				xstream.toXML(map, new FileWriter(learntFile));
			}
			else
				map = (HashMap<Integer, ArrayList<Document>>)xstream.fromXML(new FileReader(learntFile));
			
			Questions questions = Questions.parseQuesFile(questionsFile);
			BufferedWriter answer = new BufferedWriter(new FileWriter(answersFile));
			Set<Integer> qids = questions.getQids();
			for (Integer qid : qids) {
				String question = questions.getQuestion(qid);
				ArrayList<Document> docs = map.get(qid);
				// score, answers
				TreeMap<Float, String> solutions = new TreeMap<Float, String>(); 
				for (Document doc : docs) {
					ArrayList<String> ansWords = new ArrayList<String>();
					int score = doc.search(question, ansWords);
					StringBuffer strBuf = new StringBuffer();
					for(int i = 0; i < ansWords.size(); i++) {
						if(i%10==0) {
							solutions.put(doc.score*score, doc.getValue("DOCNO") + SEPARATOR + strBuf.toString());
							strBuf = new StringBuffer();
						}
						strBuf.append(ansWords.get(i) + " ");
					}
				}
				
				Set<Float> scores = solutions.keySet();
				for (Float score : scores) {
					String string = solutions.get(score);
					answer.write(qid+" " + string);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
