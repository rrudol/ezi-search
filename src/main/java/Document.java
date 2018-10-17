import java.util.*;

public class Document {
    private String title;
    private String content;
    private boolean CACHING = true;
    private HashMap<String, Double> TFIDF;
    public Document(String body) {
        List<String> lines = Arrays.asList( body.split("\n") );
        title = lines.get(0);
        content = "";
        for (int i = 1; i < lines.size(); i++) {
            content += lines.get(i) + ( i == lines.size() -1 ? "" : '\n' );
        }
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getText() {
        return title + '\n' + content;
    }

    public String getStemmedText() {
        String text = getText().toLowerCase().replaceAll("[^a-zA-Z ]", "");
        String newText = "";
        for (String word : text.split(" ")) {
            Stemmer stemmer = new Stemmer();
            for(int i = 0; i < word.length(); i++) {
                stemmer.add(word.charAt(i));
            }
            stemmer.stem();
            newText += stemmer.toString() + " ";
        }
        if(newText.endsWith("\n") || newText.endsWith(",") || newText.endsWith(" "))
        {
            newText = newText.substring(0,newText.length() - 1);
        }
        return newText;
    }

    public HashMap<String, Double> getBagOfWords(Keywords keywords) {
        HashMap<String, Double> bagOfWords = new HashMap<>();
        for (String word : getStemmedText().split(" ")) {
            if( String.join(" ", keywords.getStemmedList()).contains(word)) {
                if(bagOfWords.getOrDefault(word, -1.0) == -1.0) {
                    bagOfWords.put(word, 1.0);
                } else {
                    bagOfWords.replace(word, bagOfWords.get(word)+1);
                }
            }
        }
        return bagOfWords;
    }

    public Double getMaxWordCount(Keywords keywords) {
        Double maxWordsCount = .0;
        for (Map.Entry<String, Double> stringIntegerEntry : getBagOfWords(keywords).entrySet()) {
            if(stringIntegerEntry.getValue() > maxWordsCount) {
                maxWordsCount = stringIntegerEntry.getValue();
            }
        }
        return maxWordsCount;
    }

    public HashMap<String, Double> getTF(Keywords keywords) {
        HashMap<String, Double> TF = new HashMap<>(getBagOfWords(keywords));
        for (Map.Entry<String, Double> stringIntegerEntry : TF.entrySet()) {
            TF.replace(stringIntegerEntry.getKey(), stringIntegerEntry.getValue()/getMaxWordCount(keywords));
        }
        return TF;
    }

    private HashMap<String, Double> calculateTFIDF(Keywords keywords, HashMap<String, Double> IDF) {
        HashMap<String, Double> TF = getTF(keywords);
        for (Map.Entry<String, Double> TFEntry : TF.entrySet()) {
            TF.replace( TFEntry.getKey(), TFEntry.getValue() * IDF.getOrDefault(TFEntry.getKey(), 0.0) );
        }
        return TF;
    }

    public HashMap<String, Double> getTFIDF(Keywords keywords, HashMap<String, Double> IDF) {
        if(CACHING && TFIDF == null) {
            TFIDF = calculateTFIDF(keywords, IDF);
        }
        return TFIDF;
    }
}
