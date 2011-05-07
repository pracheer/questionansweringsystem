import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;


public class OpenNLPWrapper {

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
	
	public static void getChunks(String sentence) {
		try {
			InputStream modelIn = new FileInputStream("models/en-token.bin");
			TokenizerModel model = new TokenizerModel(modelIn);
			Tokenizer tokenizer = new TokenizerME(model);
			POSModel posModel = new POSModel(new FileInputStream("models/en-pos-perceptron.bin"));
			POSTagger tagger = new POSTaggerME(posModel);
			String[] tokens = tokenizer.tokenize(sentence);
			String[] tags = tagger.tag(tokens);
			ChunkerModel chunkerModel = new ChunkerModel(new FileInputStream("models/en-chunker.bin"));
			Chunker chuncker = new ChunkerME(chunkerModel);
			String[] chunks = chuncker.chunk(tokens, tags);
			for (int i = 0; i < tokens.length; i++) {
			  System.out.println(String.format("%s %s %s", tokens[i], tags[i], chunks[i]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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

	public static void main(String[] args) {
		getChunks("Mr. Gupta is a very good boy and in good shape too");
	}
}
