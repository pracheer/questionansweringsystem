import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

			if(!neFile.exists()) {
				System.err.println(neFile+" not found. so skipping it.");
				return tuples;
			}
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
		String[] s1Words = QAUtils.getWords(sent1.toLowerCase());
		String[] s1Stems = getStems(s1Words);
		HashSet<String> s1WordsSet = new HashSet<String>(Arrays.asList(s1Stems));

		String[] s2Words = QAUtils.getWords(sent2.toLowerCase());
		String[] s2Stems = getStems(s2Words);
		HashSet<String> s2WordsSet = new HashSet<String>(Arrays.asList(s2Stems));

		int score = 0;
		for (String s1Word : s1WordsSet) {
			/*			Removing stop words is not helping at all. so removing it.
			if(StopWords.isStopWord(s1Word))
				continue;
			 */			if(s2WordsSet.contains(s1Word))
				 score++;
		}

		return score;
	}

	/*
	 * Implement Porter Stemmer to get the roots.
	 */
	private static String[] getStems(String[] words) {
		boolean useStemmer = 
			Boolean.parseBoolean(QA.properties.getProperty("usestemmer"));

		if(!useStemmer)
			return words;

		String[] stemmedList = new String[words.length];
		if(null==words)
			return stemmedList;

		Stemmer stemmer = new Stemmer();
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if(null==word || word.isEmpty())
				continue;
			char[] charArray = word.toCharArray();
			stemmer.add(charArray, charArray.length);
			stemmer.stem();
			String string = stemmer.toString();
			stemmedList[i] = string;
		}
		return stemmedList;
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
			ArrayList<String> answers = new ArrayList<String>();
			ArrayList<Integer> ansSize = new ArrayList<Integer>();
			
//			StringBuffer buf = new StringBuffer();
//			int totalCount = 0;
//			int i = 0;

			// used to check for repetition.
			HashSet<String> entities = new HashSet<String>();

			for (Tuple tuple : tuples) {
				if(entities.contains(tuple.str))
					continue;
				entities.add(tuple.str);
				boolean added = false;
				int tupleWords = countWords(tuple.str);
				for(int s = 0; s < ansSize.size(); s++) {
					Integer integer = ansSize.get(s);
					if(tupleWords + integer <= 10) {
						ansSize.add(s, tupleWords+integer);
						ansSize.remove(s+1);
						String string = answers.get(s);
						answers.add(s, string+" " + tuple.str);
						answers.remove(s+1);
						added = true;
						break;
					}
				}
				
				if(!added) {
					if(answers.size()>=5) 
						break;
					
					answers.add(tuple.str);
					ansSize.add(tupleWords);
				}
				
//				if(i +  tupleWords > 10) {
//					++totalCount;
//					answers.add(buf.toString());
//					ansSize.add(i);
//					
//					buf.setLength(0);
//
//					if(totalCount >= 5)
//						return;
//
//					i = tupleWords;
//					buf.append(tuple.str + " ");
//				}
//				else {
//					i += tupleWords;
//					buf.append(tuple.str +" ");
//				}
			}

//			if(buf.length() > 0)
//				answers.add(buf.toString());
			
			for (String answer : answers) {
				ansWriter.write(qid + " top_docs."+qid+" " + answer +"\n");
			}

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
			if(!npsFile.exists()) {
				System.err.println(npsFile+" not found. so skipping it.");
				return tuples;
			}
			BufferedReader npsReader = new BufferedReader(new FileReader(npsFile));

			File posTagFile = new File(docDir + File.separator + Constants.ANNOTATIONS_DIR, Constants.POS_FILE);
			if(!posTagFile.exists()) {
				System.err.println(posTagFile+" not found. so skipping it.");
				return tuples;
			}
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
								String[] posOffsets = posColumns[1].split(",");
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

	public static ArrayList<String> getKeyWords(String question) {
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

	public static HashSet<String> getKeyWordsSet(String question) {
		HashSet<String> words = new HashSet<String>();
		ArrayList<String> keyWords = getKeyWords(question);
		for (String keyWord : keyWords) {
			words.add(keyWord);
		}
		return words;
	}

	public static int getOverlapScore(String sentence, HashSet<String> qWords) {
		int score = 0;
		String[] ArrayList = QAUtils.getWords(sentence);
		for (String word: ArrayList) {
			if(word.isEmpty())
				continue;
			if(qWords.contains(word))
				++score;
		}
		return score;
	}

	public static void cp(File inFile, File outputFile)
	{
		try {
			FileReader r = new FileReader(inFile);
			FileWriter w = new FileWriter(outputFile);
			write(w, r);
			r.close();
			w.flush();
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void write(Writer writer, Reader r)
	{
		try {
			BufferedWriter bw = new BufferedWriter(writer);
			BufferedReader br = new BufferedReader(r);
			for (int data = br.read(); data != -1; data = br.read()) {
				bw.write(data);
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static HashMap<String,HashSet<String>> getCoreferingWords(String string, File corefFile) {
		HashMap<Integer, HashSet<String>> corefs = new HashMap<Integer, HashSet<String>>();
		HashMap<String, HashSet<String>> corefWords = new HashMap<String, HashSet<String>>();
		try {
			BufferedReader corefReader = new BufferedReader(new FileReader(corefFile));
			String line;
			while(null!=(line=corefReader.readLine())) {
				String[] columns = line.split("\t");
				int indexOf = columns[4].indexOf("CorefID");
				int corefID = Integer.parseInt(
						columns[4].substring(indexOf+9, 
								columns[4].indexOf(" ", indexOf)-1));
//				System.out.println(corefID);
				String[] split = columns[1].split(",");

				HashSet<String> list;
				if(corefs.containsKey(corefID))
					list = corefs.get(corefID);
				else
					list = new HashSet<String>();

				int corefStart = Integer.parseInt(split[0]);
				int corefEnd = Integer.parseInt(split[1]); 
				String word = string.substring(corefStart, corefEnd);
				list.add(word);

				corefs.put(corefID, list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Set<Integer> keySet = corefs.keySet();
		for (Integer corefID : keySet) {
			HashSet<String> arrayList = corefs.get(corefID);
			for (String word : arrayList) {
				corefWords.put(word, arrayList);
				System.out.println(word+":"+arrayList);
			}
		}

		return corefWords;
	}

	public static ArrayList<Tuple> getCorefWords(File docDir, String string,
			HashSet<String> queSet) {
		
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		
		File corefFile = new File(
				docDir+File.separator + Constants.ANNOTATIONS_DIR, Constants.COREF_FILE);

		if(!corefFile.exists()) 
			return tuples;
		
		HashMap<String,HashSet<String>> coreferingWords = QAUtils.getCoreferingWords(string, corefFile);

		Set<String> words = coreferingWords.keySet();
		for (String word : words) {
			if(StopWords.isStopWord(word))
				continue;
			if(queSet.contains(word)) {
				HashSet<String> arrayList = coreferingWords.get(word);
				for (String string2 : arrayList) {
					tuples.add(new Tuple(string2, -1, -1, -1));
				}
			}
		}
		return tuples;
	}
}