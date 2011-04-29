import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class StopWords {
  private static String stopWordsFileName_ = "data/stopwords.list";
  HashSet<String> stopWords_ = new HashSet<String>();

  public StopWords(){
    try {
		FileReader fr = new FileReader(stopWordsFileName_);
		BufferedReader in = new BufferedReader(fr);

		String sw;

		while (true) {
		  sw = in.readLine();
		  if (sw == null)
		    break;
		  stopWords_.add(sw);
		}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
  }

  public boolean isStopWord(String str) {
    return stopWords_.contains(str);
  }
}
