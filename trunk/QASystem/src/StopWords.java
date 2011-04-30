import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class StopWords {
  private static String stopWordsFileName_ = "data/stopwords.list";
  public static HashSet<String> stopWords = new HashSet<String>();

  static {
    try {
		FileReader fr = new FileReader(stopWordsFileName_);
		BufferedReader in = new BufferedReader(fr);

		String sw;

		while (true) {
		  sw = in.readLine();
		  if (sw == null)
		    break;
		  stopWords.add(sw);
		}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
  }

  public static boolean isStopWord(String str) {
    return stopWords.contains(str);
  }
}
