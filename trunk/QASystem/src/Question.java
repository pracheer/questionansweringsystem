import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Question {
	private int qid;
	private String question;

	ArrayList<NEType>  answerTypes;
	ArrayList<NPSType> posTypes;

	static BufferedWriter whoWriter, whereWriter, 
					whenWriter, whatWriter, 
					whichWriter, howWriter, 
					otherWriter;
	
	private static boolean categorizeQuestions;
	
	static void createQueWriters() {
		try {
			categorizeQuestions = true;
			
			File questionFile = new File(QA.properties.getProperty("questionsFile"));
			File parentFile = questionFile.getParentFile();
			File whoFile = new File(parentFile, "whoQuestions.txt");
			File whereFile = new File(parentFile, "whereQuestions.txt");
			File whenFile = new File(parentFile, "whenQuestions.txt");
			File whatFile = new File(parentFile, "whatQuestions.txt");
			File whichFile = new File(parentFile, "whichQuestions.txt");
			File howFile = new File(parentFile, "howQuestions.txt");
			File otherFile = new File(parentFile, "otherQuestions.txt");
			
			whoWriter = new BufferedWriter(new FileWriter(whoFile));
			whereWriter = new BufferedWriter(new FileWriter(whereFile));
			whenWriter = new BufferedWriter(new FileWriter(whenFile));
			whatWriter = new BufferedWriter(new FileWriter(whatFile));
			whichWriter = new BufferedWriter(new FileWriter(whichFile));
			howWriter = new BufferedWriter(new FileWriter(howFile));
			otherWriter = new BufferedWriter(new FileWriter(otherFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Question(int qid, String question) {
		super();
		this.qid = qid;
		this.question = question.toLowerCase();
		answerTypes = new ArrayList<Question.NEType>();
		posTypes = new ArrayList<Question.NPSType>();
		setType();
	}

	private void setType() {
		try {
			BufferedWriter qWriter = null;
			if(question.contains("who")) {
				qWriter = whoWriter;
				// This is a fix for: BUG : "who is abraham lincoln"
				// -> Then the answer is not person.
				String[] qWords = QAUtils.getWords(question);
				String[] persons = OpenNLPWrapper.runNEFinder(qWords, OpenNLPWrapper.OpenNLPNEType.PERSON);
				int nameWordsCount = 0;
				for (String person : persons) {
					String[] words = QAUtils.getWords(person);
					nameWordsCount += words.length;
				}
				if((qWords.length-nameWordsCount)<=2) {
					for (NPSType npsType : NPSType.values()) {
						posTypes.add(npsType);
					}
					for (NEType neType : NEType.values()) {
						answerTypes.add(neType);
					}
				}
				else {
					answerTypes.add(NEType.PERSON);
				}
			}
			else if(question.contains("where")) {
				qWriter = whereWriter;
				answerTypes.add(NEType.LOCN);
			}
			else if(question.contains("when")) {
				qWriter = whenWriter;
				answerTypes.add(NEType.DATE);
			}
			else if(question.contains("what")) {
				qWriter = whatWriter;
				posTypes.add(NPSType.NUM);
				for(NEType neType : NEType.values()) {
					answerTypes.add(neType);
				}
			}
			else if (question.contains("which")) {
				qWriter = whichWriter;
				for(NEType neType : NEType.values()) {
					answerTypes.add(neType);
				} 
			}
			else if (question.contains("how")) {
				qWriter = howWriter;
//				if(question.contains("how much")) {
					posTypes.add(NPSType.NUM);
//				}
//				else
					for(NEType neType : NEType.values()) {
						answerTypes.add(neType);
					}
			}
			else {
				qWriter = otherWriter;
//				for (NPSType npsType : NPSType.values()) {
//					posTypes.add(npsType);
//				}
				for(NEType neType : NEType.values()) {
					answerTypes.add(neType);
				}
			}
			if(categorizeQuestions) {
				qWriter.append("<top>\n");
				qWriter.append("<num> Number: "+qid+"\n");
				qWriter.append("<desc> Description:\n");
				qWriter.append(question+"\n");
				qWriter.append("</top>\n\n");
				qWriter.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getQid() {
		return qid;
	}

	public String getString() {
		return question;
	}

	public ArrayList<NEType> getNETypes() {
		return answerTypes;
	}

	public ArrayList<NPSType> getPosTypes() {
		return posTypes;
	}

	enum NEType {
		TIME ("time"),
		PERCENT ("percent"),
		ORGN("organization"),
		MONEY("money"),
		LOCN("location"),
		DATE("date"),
		PERSON("person");

		private String value;
		private NEType(String value) {
			try {
				this.value = value;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public String getValue() {
			return value;
		}
	}

	enum NPSType {
		NPE("NPE", ""),
		NP("NP", ""),
		NNP("NNP", ""),
		NEWNP("newNP", ""),
		NUM("NP", "CD");

		private String value;
		private String contains;
		private NPSType(String value, String contains) {
			try {
				this.value = value;
				this.contains = contains;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public String getValue() {
			return value;
		}

		public String getContains() {
			return contains;
		}
	}
}
