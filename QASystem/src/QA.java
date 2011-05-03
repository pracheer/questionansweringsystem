import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.TreeMap;

public class QA {

	private static File propertiesFile = new File("data/QA.properties");
	
	public static void main(String[] args) {
		try {
			Properties properties = new Properties();
			properties.load(new FileReader(propertiesFile));
			
			File questionsFile = new File(properties.getProperty("questionsFile"));
			File reconcileDir = new File(properties.getProperty("reconcileDir"));
			File answersFile = new File(properties.getProperty("answersFile"));
			
			BufferedWriter ansWriter = new BufferedWriter(new FileWriter(answersFile));
			
			System.err.println("Starting to parse Questions File:"+questionsFile);
			Questions questions = Questions.parseQuesFile(questionsFile);
			System.err.println("Questions File parsed.");
			
			Collection<Question> questions2 = questions.getQuestions();
			for (Question question : questions2) {
				
				int qid = question.getQid();
				
				TreeMap<Float, Tuple> nes = new TreeMap<Float, Tuple>(Collections.reverseOrder());

				for(int rank = 1; rank <= 50; rank++) {
					
					File docDir = new File(reconcileDir, qid+"_"+rank);
					
					if(!docDir.exists())
						break;
					
					File txtFile = new File(docDir, "raw.txt");
					BufferedReader reader = new BufferedReader(new FileReader(txtFile));
					StringBuffer buf = new StringBuffer();
					String line;
					float docScore = -1;
					while(null!=(line=reader.readLine())) {
						if(line.startsWith("Qid:")) {
							docScore = Float.parseFloat(line.substring(line.indexOf("Score: ")+7, line.length()));
						}
						buf.append(line+" ");
					}
					
					ArrayList<Tuple> answerSentences = new ArrayList<Tuple>();
					int maxScore = -1;
					ArrayList<Tuple> sentences = QAUtils.getSentences(docDir, buf.toString());
					for (Tuple sentence : sentences) {
						int score = QAUtils.getWordOverlap(question.getString(), sentence.str);
						if(score > maxScore) {
							maxScore = score;
							answerSentences.clear();
							sentence.score = docScore*score;
							answerSentences.add(sentence);
						}
						else if(score == maxScore) {
							sentence.score = docScore*score;
							answerSentences.add(sentence);
						}
					}
					
					for (Tuple sentence : sentences) {
						ArrayList<Tuple> entities;
						
						ArrayList<Question.NEType> neTypes = question.getNETypes();
						entities = QAUtils.getNE(docDir, buf.toString(), 
								sentence.startOffset, sentence.endOffset, neTypes);
						
						ArrayList<Question.NPSType> posTypes = question.getPosTypes();
						QAUtils.getNP(docDir, buf.toString(), 
								sentence.startOffset, sentence.endOffset, posTypes);
						
						for (Tuple tuple : entities) {
							tuple.score = sentence.score;
							nes.put(tuple.score, tuple);
						}
					}
				}
				
				QAUtils.writeAnswers(ansWriter, qid, nes.values());
			}
			ansWriter.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
