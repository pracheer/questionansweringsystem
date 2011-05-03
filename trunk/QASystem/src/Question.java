import java.util.ArrayList;

public class Question {
	private int qid;
	private String question;

	ArrayList<NEType>  answerTypes;
	ArrayList<NPSType> posTypes;

	public Question(int qid, String question) {
		super();
		this.qid = qid;
		this.question = question.toLowerCase();
		answerTypes = new ArrayList<Question.NEType>();
		posTypes = new ArrayList<Question.NPSType>();
		setType();
	}

	private void setType() {
		if(question.contains("who")) {
			answerTypes.add(NEType.PERSON);
			// TODO: BUG : "who is abraham lincoln"
			String[] qWords = QAUtils.getWords(question);
		}
		else if(question.contains("where")) {
			answerTypes.add(NEType.LOCN);
		}
		else if(question.contains("when")){
			answerTypes.add(NEType.DATE);
		}
		else if(question.contains("what"))
			for(NEType neType : NEType.values()) {
				answerTypes.add(neType);
			}
		else if (question.contains("which"))
			for(NEType neType : NEType.values()) {
				answerTypes.add(neType);
			} 
		else if (question.contains("how")) {
			if(question.contains("how much")) {
				posTypes.add(NPSType.NUM);
			}
			else
				for(NEType neType : NEType.values()) {
					answerTypes.add(neType);
				}
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
