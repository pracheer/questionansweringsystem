import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Question {
	private int qid;
	private String question;

	ArrayList<NEType>  namedEntityTypes;
	ArrayList<NPSType> posTypes;

	static BufferedWriter whoWriter, whereWriter, 
	whenWriter, whatWriter, 
	whichWriter, whyWriter,
	nameWriter, howWriter, 
	otherWriter;

	private static boolean categorizeQuestions;

	static void createQueWriters() {
		try {
			categorizeQuestions = true;

			File parentDir = new File(QA.properties.getProperty("questionsDir"));
			File whoFile = new File(parentDir, "whoQuestions.txt");
			File whereFile = new File(parentDir, "whereQuestions.txt");
			File whenFile = new File(parentDir, "whenQuestions.txt");
			File whatFile = new File(parentDir, "whatQuestions.txt");
			File whichFile = new File(parentDir, "whichQuestions.txt");
			File whyFile = new File(parentDir, "whyQuestions.txt");
			File nameFile = new File(parentDir, "nameQuestions.txt");
			File howFile = new File(parentDir, "howQuestions.txt");
			File otherFile = new File(parentDir, "otherQuestions.txt");

			whoWriter = new BufferedWriter(new FileWriter(whoFile));
			whereWriter = new BufferedWriter(new FileWriter(whereFile));
			whenWriter = new BufferedWriter(new FileWriter(whenFile));
			whatWriter = new BufferedWriter(new FileWriter(whatFile));
			whichWriter = new BufferedWriter(new FileWriter(whichFile));
			whyWriter = new BufferedWriter(new FileWriter(whyFile));
			nameWriter = new BufferedWriter(new FileWriter(nameFile));
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
		namedEntityTypes = new ArrayList<Question.NEType>();
		posTypes = new ArrayList<Question.NPSType>();
		setType();
	}

	private void setType() {
		try {
			boolean cheating = Boolean.parseBoolean(QA.properties.getProperty("cheating"));
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
						namedEntityTypes.add(neType);
					}
				}
				else {
					namedEntityTypes.add(NEType.PERSON);
				}
			}
			else if(question.contains("where")) {
				qWriter = whereWriter;
				namedEntityTypes.add(NEType.LOCN);
			}
			else if(question.contains("when")) {
				qWriter = whenWriter;
				namedEntityTypes.add(NEType.DATE);
			}
			else if (question.contains("name")) {
				qWriter = nameWriter;
				posTypes.add(NPSType.NNP);
//				for (NPSType npsType : NPSType.values()) {
//					posTypes.add(npsType);
//				}
//				namedEntityTypes.add(NEType.PERSON);
			}
			else if(question.contains("what")) {
				qWriter = whatWriter;

				if(cheating && (question.contains("population") || 
						question.contains("number") || 
						question.contains("count"))) 
					posTypes.add(NPSType.NUM);	// no effect.
				else if(cheating && question.contains("time")){					
					namedEntityTypes.add(NEType.TIME);
				}
				else if(cheating && (question.contains("date")||question.contains("year"))) {
					namedEntityTypes.add(NEType.DATE);
				}
				else {
//				posTypes.add(NPSType.NNP);	// 0.039 61 not answered.
					posTypes.add(NPSType.NP);	// MRR 0.122 50 NA
//				posTypes.add(NPSType.NEWNP);	// MRR 0.07 57 NA

					posTypes.add(NPSType.NUM);	// no effect.
//					namedEntityTypes.add(NEType.TIME);
//				posTypes.add(NPSType.NPE);	// decreases MRR by 0.004 but 1 extra Q answered
//				namedEntityTypes.add(NEType.)
//				for (NPSType npsType : NPSType.values()) {
//					;
//				}
//				namedEntityTypes.add(NEType.PERSON);// no effect
//				namedEntityTypes.add(NEType.LOCN);// 0.076 58 NA
//				namedEntityTypes.add(NEType.)
					for(NEType neType : NEType.values()) {
						namedEntityTypes.add(neType);
					}
				}
			}
			else if (question.contains("which")) {
				qWriter = whichWriter;
				for(NEType neType : NEType.values()) {
					namedEntityTypes.add(neType);
				} 
			}
			else if (question.contains("how")) {
				qWriter = howWriter;
				//				if(question.contains("how much")) {
				if(cheating && question.contains("much") || question.contains("many") || 
						question.contains("long") || question.contains("far"))
					posTypes.add(NPSType.NUM);
				else {
					posTypes.add(NPSType.NUM);
					for(NEType neType : NEType.values()) {
						namedEntityTypes.add(neType);
					}
				}
			}
			else if (question.contains("why")) {
				qWriter = whyWriter;
				for (NPSType npsType : NPSType.values()) {
					posTypes.add(npsType);
				}
			}
			else {
				qWriter = otherWriter;
				for(NEType neType : NEType.values()) {
					namedEntityTypes.add(neType);
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
		return namedEntityTypes;
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

	/**
	 * all types of noun phrases possible in nps file.
	 * the second argument is optional and indicates if the noun phrase from nps file 
	 * should contain something from postag or not.
	 * E.g, numeric noun phrase  like "20 hexagons" which would be denoted as NP in nps file.
	 * To check if it has a number in it, we would need to check postag file and see if it contains
	 * CD (indicating number) in it.
	 * 
	 * @author Prac
	 *
	 */
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
