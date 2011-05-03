import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.TreeMap;

import org.xml.sax.helpers.DefaultHandler;

import com.thoughtworks.xstream.XStream;

public class Baseline extends DefaultHandler{

	public static Properties properties;
	private static File propertiesFile = new File("data/QA.properties");
	private static final String SEPARATOR = " ";
	private static final String TOP_DOCS = "top_docs.";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			properties = new Properties();
			properties.load(new FileReader(propertiesFile));
			File topDocsDir = new File(properties.getProperty("topDocsDir"));
			File tempDir = new File(properties.getProperty("tempDir"));
			File rawFileDir = new File(properties.getProperty("rawFileDir"));

			tempDir.mkdirs();
			rawFileDir.mkdirs();

			// to store the map once it has learnt it.
			File learntFile = new File(properties.getProperty("objectFile"));
			File questionsFile = new File(properties.getProperty("questionsFile"));
			File answersFile = new File(properties.getProperty("answersFile"));
			boolean learn = Boolean.parseBoolean(properties.getProperty("learn"));
			boolean overwrite = Boolean.parseBoolean(properties.getProperty("overwrite"));

			// consists of qid and list of all the documents arranged by ranks.
			HashMap<Integer, ArrayList<Document>> map;

			XStream xstream = new XStream();
			if (learn) {
				map = learn(overwrite, topDocsDir, tempDir, rawFileDir);
				if(!learntFile.exists())
					learntFile.createNewFile();
				xstream.toXML(map, new FileWriter(learntFile));
				System.err.println("Documents created in memory and saved to a file:"+learntFile);
			}
			else {
				System.out.println("opening learnt model from file:" + learntFile.getAbsolutePath());
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
				Collection<String> solutions = getAnswer(map, question);

				for (String string : solutions) {
					answer.write(question.getQid() + /*" "+score + */ " "+ string + "\n");
				}
			}
			answer.flush();
			answer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

/*
	private static void runPOS(HashMap<Integer, ArrayList<Document>> map) {
		Collection<ArrayList<Document>> docs = map.values();
		for(int c = 0; c < docs.size(); c++) {
			Iterator<ArrayList<Document>> iterator = docs.iterator();
			while(iterator.hasNext()) {
				ArrayList<Document> arrayList = iterator.next();
				Iterator<Document> docIter = arrayList.iterator();
				while(docIter.hasNext()) {
					Document document = docIter.next();
					runPOS(document);
				}
			}
		}
	}

	private static void runNER(HashMap<Integer, ArrayList<Document>> map) {
		Collection<ArrayList<Document>> docs = map.values();
		for(int c = 0; c < docs.size(); c++) {
			Iterator<ArrayList<Document>> iterator = docs.iterator();
			while(iterator.hasNext()) {
				ArrayList<Document> arrayList = iterator.next();
				Iterator<Document> docIter = arrayList.iterator();
				while(docIter.hasNext()) {
					Document document = docIter.next();
					runNER(document);
				}
			}
		}
	}

	private static void runNER(Document document) {
		System.out.println("processing " + document.getQid() +":"+document.getRank());
		String text = document.getText();
		String[] words = Util.getWords(text);
		HashMap<String,String[]> nes = OpenNLPWrapper.runAllNEs(words);
		document.addNEs(nes);
	}

	private static void runPOS(Document document) {
		System.out.println("processing " + document.getQid() +":"+document.getRank());
		String text = document.getText();
		String[] words = Util.getWords(text);
		HashMap<String,String[]> nes = OpenNLPWrapper.runAllNEs(words);
		document.addNEs(nes);
	}
*/
	private static Collection<String> getAnswer(HashMap<Integer, ArrayList<Document>> map, Question question) {
		System.out.println("Reading question:"+question.getQid());
		System.out.println(question.getString());

//		Collection<String> values = getBaselineAnswer(map, question);
		Collection<String> values = getNERAnswer(map, question);
		
		HashSet<String> answers = new HashSet<String>();
		int i = 5;
		for (String value : values) {
			if(--i>=0)
				answers.add(value);				
		}
		return answers;
	}

	private static Collection<String> getNERAnswer(HashMap<Integer, ArrayList<Document>> map,
			Question question) {
		
		QueType type = question.getType();
		ArrayList<OpenNLPWrapper.NEType> answerTypes = QAUtils.getAnswerType(type);
		ArrayList<Document> docs = map.get(question.getQid());
		// score, answers
		TreeMap<Float, String> solutions = new TreeMap<Float, String>(Collections.reverseOrder()); 
		for (Document doc : docs) {
			ArrayList<String> ansSent = new ArrayList<String>();
			float score = doc.searchSentences(question.getString(), ansSent);
			StringBuffer strBuf = new StringBuffer();
			int i = 0;
			HashSet<String> answers = new HashSet<String>();
			answers.addAll(ansSent);
			for(String answer: answers) {
				strBuf.append(answer + " ");
				if(((i%10==0&i>0) || i==answers.size()-1) && strBuf.length()>0) {
					solutions.put((int)doc.score*score*10000, TOP_DOCS+doc.getQid() + SEPARATOR + strBuf.toString().trim());
					strBuf.setLength(0);
				}
				i++;
			}
		}
		return solutions.values();
	}

	private static HashMap<Integer, ArrayList<Document>> learn(boolean overwrite, File topDocsDir, File outputDir, File rawFileDir) {
		HashMap<Integer, ArrayList<Document>> map;
		File[] files = topDocsDir.listFiles();
		map = new HashMap<Integer, ArrayList<Document>>();
		for (File inputFile : files) {
			File xmlFile = new File(outputDir, inputFile.getName()
					+ ".xml");
			if(!xmlFile.exists() || overwrite)
				XMLTrec.convertToXML(inputFile, xmlFile);
			ArrayList<Document> docs = XMLTrec.parse(xmlFile);
			int qid = docs.get(0).qid;
			map.put(qid, docs);

			// generate the rawfiles.
			rawFileDir.mkdirs();
			XMLTrec.dumpRawFile(xmlFile, rawFileDir, qid, overwrite);
		}
		return map;
	}

	private static Collection<String> getBaselineAnswer(
			HashMap<Integer, ArrayList<Document>> map, Question question) {
		ArrayList<Document> docs = map.get(question.getQid());
		// score, answers
		TreeMap<Float, String> solutions = new TreeMap<Float, String>(Collections.reverseOrder()); 
		for (Document doc : docs) {
			ArrayList<String> ansWords = new ArrayList<String>();
			float score = doc.search(question.getString(), ansWords);
			StringBuffer strBuf = new StringBuffer();
			int i = 0;
			HashSet<String> answers = new HashSet<String>();
			answers.addAll(ansWords);
			for(String answer: answers) {
				strBuf.append(answer + " ");
				if(((i%10==0&i>0) || i==answers.size()-1) && strBuf.length()>0) {
					solutions.put((int)doc.score*score*10000, TOP_DOCS+doc.getQid() + SEPARATOR + strBuf.toString().trim());
					strBuf.setLength(0);
				}
				i++;
			}
		}
		
		return solutions.values();
	}
}
