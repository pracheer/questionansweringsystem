import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Document {

	int qid;
	int rank;
	float score;

	// Had to make it this complex so that comparing later one becomes easier/faster.
	// Tag, {sentence1:{word1, word2,..}, sentence2:{...}}
	HashMap<String, ArrayList<HashSet<String>>> tagMap;

	public Document(int qid, int rank, float score) {
		this.qid = qid;
		this.rank = rank;
		this.score = score;
		tagMap = new HashMap<String, ArrayList<HashSet<String>>>();
	}

	public void addString(String key, String str) {
		key = key.toLowerCase();
		str = str.toLowerCase();
		ArrayList<HashSet<String>> sentenceList = new ArrayList<HashSet<String>>();
		String[] sentences = str.split("[.]");
		for (String sentence : sentences) {
			HashSet<String> keyWords = getKeyWords(sentence);
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
	
	public float search(String question, ArrayList<String> ansWords) {
		float maxScore = -1;
		HashSet<String> solutionStr = null;

		Set<String> tagSet = tagMap.keySet();
		HashSet<String> qWords = getKeyWords(question);
		for (String tag : tagSet) {
			ArrayList<HashSet<String>> sentences = tagMap.get(tag);
			for (HashSet<String> sentence : sentences) {
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

	private float getOverlapScore(HashSet<String> wordSet, HashSet<String> qWords) {
		int score = 0;
		for (String qWord : qWords) {
			if(wordSet.contains(qWord))
				++score;
		}
		return score;
	}

	private HashSet<String> getKeyWords(String question) {
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
		ArrayList<HashSet<String>> arrayList = tagMap.get(string.toLowerCase());
		StringBuffer buf = new StringBuffer();
		for (HashSet<String> hashSet : arrayList) {
			for (String string2 : hashSet) {
				buf.append(string2 + " ");
			}
		}
		return buf.toString().trim();
	}
}
