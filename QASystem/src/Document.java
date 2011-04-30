import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Document {

	int qid;
	int rank;
	float score;

	// Had to make it this complex so that comparing later one becomes easier/faster.
	HashMap<String, ArrayList<ArrayList<String>>> tagMap;

	public Document(int qid, int rank, float score) {
		this.qid = qid;
		this.rank = rank;
		this.score = score;
		tagMap = new HashMap<String, ArrayList<ArrayList<String>>>();
	}

	public void addString(String key, String str) {
		key = key.toLowerCase();
		str = str.toLowerCase();
		ArrayList<ArrayList<String>> sentenceList = new ArrayList<ArrayList<String>>();
		String[] sentences = str.split("[.]");
		for (String sentence : sentences) {
			ArrayList<String> keyWords = getKeyWords(sentence);
			if(keyWords.size() == 0)
				continue;
			sentenceList.add(keyWords);
		}
		if(sentenceList.size() > 0)
			tagMap.put(key, sentenceList);
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
	
	public HashMap<String, ArrayList<ArrayList<String>>> getTagMap() {
		return tagMap;
	}
	
	public float search(String question, ArrayList<String> ansWords) {
		float maxScore = -1;
		ArrayList<String> solutionStr = null;

		Set<String> tagSet = tagMap.keySet();
		HashSet<String> qWords = getKeyWordsSet(question);
		for (String tag : tagSet) {
			ArrayList<ArrayList<String>> sentences = tagMap.get(tag);
			for (ArrayList<String> sentence : sentences) {
				float score = getOverlapScore(sentence, qWords)/sentence.size();
				if(maxScore < score) {
					solutionStr = sentence;
					maxScore = score;
				}
			}
		}

		for (String word : solutionStr) {
			if(!qWords.contains(word))
				ansWords.add(word);
		}
		return maxScore;
	}

	private float getOverlapScore(ArrayList<String> ArrayList, HashSet<String> qWords) {
		int score = 0;
		for (String word: ArrayList) {
			if(qWords.contains(word))
				++score;
		}
		return score;
	}

	private ArrayList<String> getKeyWords(String question) {
		ArrayList<String> words = new ArrayList<String>();
		String[] qWords = question.split("[,\\s]");
		for (String qWord : qWords) {
			if(!StopWords.isStopWord(qWord)) { 
				if(qWord.endsWith("?"))
					qWord = qWord.substring(0, qWord.length()-1);
				if(!qWord.isEmpty())
					words.add(qWord);
			}
		}
		return words;
	}

	private HashSet<String> getKeyWordsSet(String question) {
		HashSet<String> words = new HashSet<String>();
		String[] qWords = question.split("[,\\s]");
		for (String qWord : qWords) {
			if(!StopWords.isStopWord(qWord)) { 
				if(qWord.endsWith("?"))
					qWord = qWord.substring(0, qWord.length()-1);
				if(!qWord.isEmpty())
					words.add(qWord);
			}
		}
		return words;
	}

	public String getValue(String string) {
		ArrayList<ArrayList<String>> arrayList = tagMap.get(string.toLowerCase());
		StringBuffer buf = new StringBuffer();
		for (ArrayList<String> list : arrayList) {
			for (String string2 : list) {
				buf.append(string2 + " ");
			}
		}
		return buf.toString().trim();
	}
}
