import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class QAUtils {

	public static String[] getSentences(String str) {
		return str.split("[.]");
	}

	public static String[] getWords(String str) {
		ArrayList<String> words = new ArrayList<String>();
		String[] split = str.split("\\W");
		for (String string : split) {
			if(!string.isEmpty())
				words.add(string);
		}
		return words.toArray(new String[1]);
	}

	public static ArrayList<Tuple> getSentences(File docDir, String str) {
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		try {
			File sentFile = new File(docDir + File.separator + Constants.ANNOTATIONS_DIR, Constants.SENTENCE_FILE);
			BufferedReader sentReader = new BufferedReader(new FileReader(sentFile));
			String line;
			while(null!=(line=sentReader.readLine())) {
				String[] offsets = line.split("\t")[1].split(",");
				int startOffset = Integer.parseInt(offsets[0]);
				int endOffset = Integer.parseInt(offsets[1]);
				String entity = str.substring(startOffset, endOffset);
				tuples.add(new Tuple(entity, startOffset, endOffset, -1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tuples;
	}

	public static ArrayList<Tuple> getNE(File docDir, String str, 
			int startOffset, int endOffset, 
			ArrayList<Question.NEType> neTypes) {
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();

		if(neTypes == null || neTypes.size()==0)
			return tuples;

		try {
			File neFile = new File(docDir + File.separator + Constants.ANNOTATIONS_DIR, Constants.NE_FILE);
			BufferedReader reader = new BufferedReader(new FileReader(neFile));
			String line;
			while(null!=(line=reader.readLine())) {
				String[] columns = line.split("\t");
				boolean found = false;
				for (Question.NEType neType : neTypes) {
					if(columns[3].equalsIgnoreCase(neType.getValue())) {
						found = true;
						break;
					}
				}

				if(found) {
					String[] offsets = columns[1].split(",");
					int tupleStart = Integer.parseInt(offsets[0]);
					int tupleEnd = Integer.parseInt(offsets[1]);
					if(startOffset<=tupleStart && endOffset >= tupleEnd) {
						String entity = str.substring(tupleStart, tupleEnd);
						tuples.add(new Tuple(entity, tupleStart, tupleEnd, -1));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tuples;
	}

	public static int getWordOverlap(String sent1, String sent2) {
		String[] s1Words = QAUtils.getWords(sent1);
		HashSet<String> s1WordsSet = new HashSet<String>(Arrays.asList(s1Words)); 

		String[] s2Words = QAUtils.getWords(sent2);
		HashSet<String> s2WordsSet = new HashSet<String>(Arrays.asList(s2Words)); 

		int score = 0;
		for (String qWord : s1WordsSet) {
			if(s2WordsSet.contains(qWord))
				score++;
		}

		return score;
	}

	/**
	 * writes answers in 10 word chunks.
	 * @param ansWriter
	 * @param qid
	 * @param tuples
	 */
	public static void writeAnswers(BufferedWriter ansWriter, int qid,
			Collection<Tuple> tuples) {
		try {
			StringBuffer buf = new StringBuffer();
			int totalCount = 0;
			int i = 0;

			// used to check for repetition.
			HashSet<String> entities = new HashSet<String>();

			for (Tuple tuple : tuples) {
				if(entities.contains(tuple.str))
					continue;
				entities.add(tuple.str);
				int tupleWords = countWords(tuple.str);
				if(i +  tupleWords > 10) {
					++totalCount;
					ansWriter.write(qid + " top_docs."+qid+" " + buf.toString()+"\n");
					buf.setLength(0);

					if(totalCount >= 5)
						return;

					i = tupleWords;
					buf.append(tuple.str + " ");
				}
				else {
					i += tupleWords;
					buf.append(tuple.str +" ");
				}
			}

			if(buf.length() > 0)
				ansWriter.write(qid + " top_docs."+qid+" " + buf.toString()+"\n");

			ansWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int countWords(String str) {
		int wordCount = 0;
		String[] split = str.split("\\W");
		for (String string : split) {
			if(!string.isEmpty())
				wordCount++;
		}
		return wordCount;
	}

	public static ArrayList<Tuple> getNP(File docDir, String str,
			int startOffset, int endOffset, ArrayList<Question.NPSType> npsTypes) {
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		if(npsTypes == null || npsTypes.size() == 0)
			return tuples;

		try {
			File npsFile = new File(docDir + File.separator + Constants.ANNOTATIONS_DIR, Constants.NPS_FILE);
			BufferedReader npsReader = new BufferedReader(new FileReader(npsFile));

			File posTagFile = new File(docDir + File.separator + Constants.ANNOTATIONS_DIR, Constants.POS_FILE);
			BufferedReader posReader = new BufferedReader(new FileReader(posTagFile));

			String posLine;

			String line;
			while(null!=(line=npsReader.readLine())) {
				String[] columns = line.split("\t");
				Question.NPSType type = null;
				for(Question.NPSType npsType : npsTypes) {
					if(columns[3].equalsIgnoreCase(npsType.getValue())) {
						type = npsType;
						break;
					}
				}
				if(null != type) {
					String[] npsOffsets = columns[1].split(",");
					int npsStart = Integer.parseInt(npsOffsets[0]);
					int npsEnd = Integer.parseInt(npsOffsets[1]);

					if(endOffset <= npsStart) {
						// As we needed only tuples b/w startOffset and endOffset,
						// the remaining stuff won't be needed. So we can return.
						return tuples;
					}

					if(startOffset<=npsStart && endOffset >= npsEnd) {
						String posType = type.getContains();
						if(posType==null || posType.isEmpty()) {
							String entity = str.substring(npsStart, npsEnd);
							tuples.add(new Tuple(entity, npsStart, npsEnd, -1));
						}
						else {
							while(null!=(posLine=posReader.readLine())) {
								String[] posColumns = posLine.split("\t");
								String[] posOffsets = columns[1].split(",");
								int posStart = Integer.parseInt(posOffsets[0]);
								int posEnd = Integer.parseInt(posOffsets[1]);
								if(posStart >= npsEnd) {
									break;
								}
								if(posColumns[3].equalsIgnoreCase(posType)) {
									if(posStart >= npsStart && posEnd <= npsEnd) {
										String entity = str.substring(npsStart, npsEnd);
										tuples.add(new Tuple(entity, npsStart, npsEnd, -1));
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tuples;
	}
}

