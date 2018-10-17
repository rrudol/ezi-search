import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Keywords {
    private List<String> list;
    public Keywords(String text) {
        list = Arrays.asList( text.split("\n") );
    }

    private HashMap<String, Double> IDF;
    public List<String> getList() {
        return list;
    }
    private boolean CACHEING = true;

    public List<String> getStemmedList() {
        String text = "";
        for (String s : getList()) {
            text += s.toLowerCase() + " ";
        }
        String newText = "";
        for (String word : text.split(" ")) {
            Stemmer stemmer = new Stemmer();
            for(int i = 0; i < word.length(); i++) {
                stemmer.add(word.charAt(i));
            }
            stemmer.stem();
            newText += stemmer.toString() + "\n";
        }
        return Arrays.asList( newText.split("\n") );
    }

    public Integer containgDocumentsCount(ArrayList<Document> documents, String keyword) {
        Integer result = 0;
        for (Document document : documents) {
            result += document.getStemmedText().contains(keyword) ? 1 : 0;
        }
        return result;
    }

    private HashMap<String, Double> calculateIDF(ArrayList<Document> documents) {
        HashMap<String, Double> IDF = new HashMap<>();
        for (String keyWord : getStemmedList()) {
            Integer c = containgDocumentsCount( documents, keyWord);
            IDF.put(keyWord, c != 0 ? Math.log( documents.size() / c) : 0 );
        }
        return IDF;
    }

    public HashMap<String, Double> getIDF(ArrayList<Document> documents) {
        if(CACHEING && IDF == null) {
            IDF = calculateIDF(documents);
        }
        return IDF;
    }
}
