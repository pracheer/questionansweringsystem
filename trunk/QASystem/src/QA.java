import java.io.BufferedReader;
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
import java.util.Set;
import java.util.TreeMap;

public class QA {

	private static File propertiesFile = new File("data/QA.properties");
	public static Properties properties = new Properties();
	static {
	try {
		properties.load(new FileReader(propertiesFile));
	} catch (Exception e) {
		e.printStackTrace();
	}
	}
	public static void main(String[] args) {
		try {

			long startTime = System.currentTimeMillis();

			boolean categorizeQues = 
				Boolean.parseBoolean(QA.properties.getProperty("categorizeQuestions"));

			int numDocs = 
				Integer.parseInt(QA.properties.getProperty("numDocs"));

			File questionsDir = new File(properties.getProperty("questionsDir"));
			String[] queFiles = properties.getProperty("questionsFile").split(";");
			File answersDir = new File(properties.getProperty("answersDir"));
			String[] ansFiles = properties.getProperty("answersFile").split(";");
			if(ansFiles.length!=queFiles.length) {
				System.err.println("Number mismatch quesFiles and ansFiles.. exiting.");
				return;
			}

			for(int q = 0; q < queFiles.length; q++) {
				File questionsFile = new File(questionsDir, queFiles[q]);
				System.out.println("Processing question File:"+(q+1)+":"+queFiles[q]);

				System.out.println("Starting to parse Questions File:"+questionsFile);
				Questions questions = Questions.parseQuesFile(questionsFile, categorizeQues);
				System.out.println("Questions File parsed.");

				if(categorizeQues)
					return;

				File reconcileDir = new File(properties.getProperty("reconcileDir"));

				File answersFile = new File(answersDir, ansFiles[q]);
				BufferedWriter ansWriter = new BufferedWriter(new FileWriter(answersFile));

				Collection<Question> questions2 = questions.getQuestions();
				for (Question question : questions2) {

					int qid = question.getQid();
					if(qid%50==0)
						System.out.println("finding answer for question:"+qid);

					// consists of string as candidate answer and its tuple
					HashMap<String, Tuple> tupleScoreMap = new HashMap<String, Tuple>();

					HashSet<String> nesSet = new HashSet<String>();
					
					HashMap<String,String[]> queNes = OpenNLPWrapper.runAllNEs(
							QAUtils.getWords(question.getString()));

					Set<String> keySet = queNes.keySet();
					for (String key : keySet) {
						String[] strings = queNes.get(key);
						for (String str : strings) {
							nesSet.add(str);
						}
					}
					
					
					for(int rank = 1; rank <= numDocs; rank++) {

						File docDir = new File(reconcileDir, qid+"_"+rank);

						if(!docDir.exists())
							break;

						File txtFile = new File(docDir, "raw.txt");
						BufferedReader reader = new BufferedReader(new FileReader(txtFile));
						StringBuffer buf = new StringBuffer();
						String line;
						float docScore = 100/rank;
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
							if(sentence==null || sentence.str.isEmpty())
								continue;
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

						for (Tuple sentence : answerSentences) {
							ArrayList<Tuple> entities;

							ArrayList<Question.NEType> neTypes = question.getNETypes();
							entities = QAUtils.getNE(docDir, buf.toString(), 
									sentence.startOffset, sentence.endOffset, neTypes);

							updateTupleScores(tupleScoreMap, sentence, entities, false, true);
							
							ArrayList<Question.NPSType> posTypes = question.getPosTypes();
							entities = QAUtils.getNP(docDir, buf.toString(), 
									sentence.startOffset, sentence.endOffset, posTypes);

							updateTupleScores(tupleScoreMap, sentence, entities, false, false);
						}
						
						float corefScore = docScore;
						
						ArrayList<Tuple> entities = QAUtils.getCorefWords(docDir, buf.toString(), nesSet);
						Tuple docTuple = new Tuple(buf.toString(), 0, buf.length(), corefScore);
						updateTupleScores(tupleScoreMap, docTuple, entities, true, false);
					}
					
					// contains all possible answers sorted by decreasing scores.
					TreeMap<Float, Tuple> nes = sortTupleByScores(tupleScoreMap);
					QAUtils.writeAnswers(ansWriter, qid, nes.values());
				}
				ansWriter.close();
				System.out.println("answers written to file:"+answersFile);
			}

			long endTime = System.currentTimeMillis();
			System.out.println("Total time taken:"+(endTime-startTime)/60000+" minutes.");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static TreeMap<Float,Tuple> sortTupleByScores(HashMap<String, Tuple> tupleScoreMap) {
		TreeMap<Float, Tuple> nes = new TreeMap<Float, Tuple>(Collections.reverseOrder());
		Set<String> tupleStr = tupleScoreMap.keySet();
		for (String string : tupleStr) {
			Tuple tuple = tupleScoreMap.get(string);
			nes.put(tuple.score, tuple);
		}
		return nes;
	}

	private static void updateTupleScores(HashMap<String, Tuple> tupleScoreMap,
			Tuple sentence, ArrayList<Tuple> entities, boolean addScore, boolean namedEntity) {
		for (Tuple tuple : entities) {
			float score = sentence.score;
			
			// giving more weightage to named Entities.
			if(namedEntity)
				score *= 1.2;
			
			if(!tupleScoreMap.containsKey(tuple.str)) {
				tuple.score = score;
				tupleScoreMap.put(tuple.str, tuple);
			}
			else {
				Tuple oldTuple = tupleScoreMap.get(tuple.str);
				if(addScore)
					tuple.score = oldTuple.score+score;
				else
					tuple.score = /*oldTuple.score/2+*/score;
				tupleScoreMap.put(tuple.str, tuple);
			}
		}
	}

}
