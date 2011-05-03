import java.io.FileInputStream;
import java.util.HashMap;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;


public class OpenNLPWrapper {

	private static String[] getWordsfromSpans(String[] words, Span[] spans) {
		String[] ne = new String[spans.length];
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < spans.length; i++) {
			Span person = spans[i];
			str.setLength(0);
			for(int p = person.getStart(); p < person.getEnd(); p++) {
				str.append(words[p]);
				if(p != person.getEnd() - 1)
					str.append(" ");
			}
			ne[i] = str.toString();
		}
		
		return ne;
	}
	
	static NameFinderME getFinder(String modelStr) {
		try {
			FileInputStream modelIn = new FileInputStream(modelStr);
			TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
			return new NameFinderME(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
 	public static HashMap<String,String[]> runAllNEs(String[] words) {
		HashMap<String, String[]> ne = new HashMap<String, String[]>();
		for(OpenNLPNEType type: OpenNLPNEType.values()) {
			Span[] spans = type.getNameFinderME().find(words);
			ne.put(type.getValue(), getWordsfromSpans(words, spans));
		}
		return ne;
	}
	
	public static String[] runNEFinder(String[] words, OpenNLPNEType type) {
		Span[] spans = type.getNameFinderME().find(words);
		return getWordsfromSpans(words, spans);
	}
	
	enum OpenNLPNEType {
		TIME ("time","models/en-ner-time.bin"),
		PERCENT ("percent","models/en-ner-percentage.bin"),
		ORGN("organization","models/en-ner-organization.bin"),
		MONEY("money", "models/en-ner-money.bin"),
		LOCN("location","models/en-ner-location.bin"),
		DATE("date","models/en-ner-date.bin"),
		PERSON("person","models/en-ner-person.bin");
		
		private String value;
		private NameFinderME nameFinderME;
		private OpenNLPNEType(String value, String model) {
			try {
				FileInputStream modelIn = new FileInputStream(model);
				TokenNameFinderModel tnfModel = new TokenNameFinderModel(modelIn);
				this.nameFinderME = new NameFinderME(tnfModel);
				this.value = value;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public String getValue() {
			return value;
		}
		
		public NameFinderME getNameFinderME() {
			return nameFinderME;
		}
	}

}
