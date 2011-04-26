import java.util.HashMap;


public class Document {
	
	int qid;
	int rank;
	float score;
	
	// Tag, contents map.
	HashMap<String, String> tagMap;
	
	public Document(int qid, int rank, float score) {
		this.qid = qid;
		this.rank = rank;
		this.score = score;
		tagMap = new HashMap<String, String>();
	}

	public void addString(String key, String str) {
		tagMap.put(key, str);
	}
}
