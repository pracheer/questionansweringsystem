import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Document {
	
	int qid;
	int rank;
	float score;
	
	// Tag, {sentence1:{word1, word2,..}, sentence2:{...}}
	HashMap<String, ArrayList<HashSet<String>>> tagMap;
	
	StopWords stopWords;
	
	public Document(int qid, int rank, float score) {
		this.qid = qid;
		this.rank = rank;
		this.score = score;
		tagMap = new HashMap<String, ArrayList<HashSet<String>>>();
		this.stopWords = new StopWords();
	}

	public void addString(String key, String str) {
		key = key.toLowerCase();
		ArrayList<HashSet<String>> sentenceList = new ArrayList<HashSet<String>>();
		String[] sentences = str.split("[.]");
		for (String sentence : sentences) {
			HashSet<String> keyWords = getKeyWords(sentence);
			sentenceList.add(keyWords);
		}
		tagMap.put(key, sentenceList);
	}
	
	public int getQid() {
		return qid;
	}
	
	public float getScore() {
		return score;
	}
	
	public int search(String question, ArrayList<String> ansWords) {
		int maxScore = -1;
		HashSet<String> solutionStr = null;
		
		Set<String> tagSet = tagMap.keySet();
		HashSet<String> qWords = getKeyWords(question);
		for (String tag : tagSet) {
			ArrayList<HashSet<String>> sentences = tagMap.get(tag);
			for (HashSet<String> sentence : sentences) {
				int score = getOverlapScore(sentence, qWords);
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

	private int getOverlapScore(HashSet<String> wordSet, HashSet<String> qWords) {
		int score = 0;
		for (String qWord : qWords) {
			if(wordSet.contains(qWord))
				++score;
		}
		return score;
	}
	
	private HashSet<String> getKeyWords(String question) {
		HashSet<String> words = new HashSet<String>();
		String[] qWords = question.split(" ");
		for (String qWord : qWords) {
			if(!stopWords.isStopWord(qWord))
				words.add(qWord);
		}
		return words;
	}

	public String getValue(String string) {
		tagMap.get(string.toLowerCase());
		return null;
	}
}
