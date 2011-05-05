import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class Document {

	private static final int DEFAULT_SUR_LENGTH = 10;
	int qid;
	int rank;
	float score;

	// contains the entire contents of the document.
	StringBuffer text;
	
	// Had to make it this complex so that comparing later one becomes easier/faster.
	// {Tag:{startingIndex, number of sentences}}
	// taggedSentences => {Name:{0,2}}
	HashMap<String, TreeMap<Integer, Integer>> taggedSentences;
	private HashMap<String, String[]> nes;

	public Document(int qid, int rank, float score) {
		this.qid = qid;
		this.rank = rank;
		this.score = score;
		taggedSentences = new HashMap<String, TreeMap<Integer, Integer>>();
		text = new StringBuffer();
	}

	public void addString(String key, String str) {
		key = key.toLowerCase();
		str = str.toLowerCase().replaceAll("[\\n;-_]", " ");

		TreeMap<Integer,Integer> taggedList = taggedSentences.get(key);
		if(taggedList == null)
			taggedList = new TreeMap<Integer, Integer>();
		taggedList.put(text.length(), text.length()+str.length());
		taggedSentences.put(key, taggedList);
		text.append(str + " ");
	}

	public int getQid() {
		return qid;
	}

	public float getScore() {
		return score;
	}

	public int getRank() {
		return rank;
	}

	public float search(String question, ArrayList<String> ansWords) {
		float maxScore = -1;
		String solutionStr = null;
		HashSet<String> qWords = QAUtils.getKeyWordsSet(question);
		
		String str = text.toString();
		String[] sentences = QAUtils.getSentences(str);
		for (String sentence : sentences) {
			float score = QAUtils.getOverlapScore(sentence, qWords)/sentence.length()/*getSurroundLength(sentence)*/;
			if(maxScore < score) {
				solutionStr = sentence;
				maxScore = score;
			}
		}

		String[] words = QAUtils.getWords(solutionStr);
		for (String word : words) {
			if(!qWords.contains(word))
				ansWords.add(word);
		}
		return maxScore;
	}

	public float searchSentences(String question, ArrayList<String> ansSentences) {
		int maxScore = -1;
		HashSet<String> qWords = QAUtils.getKeyWordsSet(question);
		
		String str = text.toString();
		String[] sentences = QAUtils.getSentences(str);
		for (String sentence : sentences) {
			int score = QAUtils.getOverlapScore(sentence, qWords)/*getSurroundLength(sentence)*/;
			if(maxScore < score) {
				ansSentences.clear();
				ansSentences.add(sentence);
				maxScore = score;
			}
			else if(maxScore==score) {
				ansSentences.add(sentence);
			}
		}
		
		return maxScore;
	}

	public float searchNE(String question, ArrayList<String> ansWords, ArrayList<Question.NEType> answerTypes) {
		
		ArrayList<String> namedEntitiesList = new ArrayList<String>();
		for (Question.NEType neType : answerTypes) {
			namedEntitiesList.addAll(Arrays.asList(nes.get(neType.getValue())));
		}
		String[] namedEntities = namedEntitiesList.toArray(new String[1]);
		
		if(namedEntities==null || namedEntities.length==0)
			return 0;

		float maxScore = -1;
		String solutionStr = null;
		HashSet<String> qWords = QAUtils.getKeyWordsSet(question);
		
		String str = text.toString();
		String[] sentences = QAUtils.getSentences(str);
		for (String sentence : sentences) {
			
			System.out.println("sentence:"+sentence);
			String entity = null;
			boolean found = false;
			for (String namedEntity : namedEntities) {
				if(namedEntity == null)
					continue;
				System.out.println("namedEntity:"+namedEntity);
				if(sentence.contains(namedEntity)) {
					found = true;
					entity = namedEntity;
					break;
				}
			}		
			
			if(!found)
				continue;
			
			float score = QAUtils.getOverlapScore(sentence, qWords)/sentence.length()/*getSurroundLength(sentence)*/;
			if(maxScore < score) {
				solutionStr = entity;
				maxScore = score;
			}
		}

		ansWords.add(solutionStr);
		
		return maxScore;
	}

	private int getSurroundLength(String sentence) {
		String str = text.toString();
		int index = str.indexOf(sentence);
		Set<String> keySet = taggedSentences.keySet();
		for (String key : keySet) {
			TreeMap<Integer,Integer> treeMap = taggedSentences.get(key);
			Set<Integer> keySet2 = treeMap.keySet();
			for (Integer key2 : keySet2) {
				if(treeMap.get(key2)>index)
					return (treeMap.get(key2)-key2);
			}
		}
		return DEFAULT_SUR_LENGTH;
	}
	
	/**
	 * To get the string associated with a given tag
	 * @param tag
	 * @return all the strings of a given tag combined. 
	 */
	public String getValue(String tag) {
		StringBuffer buf = new StringBuffer();
		String str = text.toString();
		TreeMap<Integer,Integer> treeMap = taggedSentences.get(tag);
		Set<Integer> keys = treeMap.keySet();
		for (Integer start : keys) {
			Integer end = treeMap.get(start);
			buf.append(str.substring(start, end) + " ");
		}
		
		return buf.toString();
	}
	
	public String getText() {
		return text.toString();
	}

	public void addNEs(HashMap<String, String[]> nes) {
		this.nes = nes;
	}
	
	public String[] getNEs(String neType) {
		return nes.get(neType);
	}
}
