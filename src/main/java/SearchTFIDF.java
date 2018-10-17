import javafx.util.Pair;

import java.io.*;
import java.util.*;
import org.json.*;

import javax.print.Doc;

public class SearchTFIDF {
    ArrayList<Document> DocumentsList;
    Keywords kw;
    private String getFile(String fileName) {

        StringBuilder result = new StringBuilder("");

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();

    }

    public String loadFromFile(String filename) {
//        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        String result = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = "";
            while ((line = br.readLine()) != null) {
                result += line + "\n";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String stem(String text) {
        String newText = "";
        for (String word : text.split(" ")) {
            Stemmer stemmer = new Stemmer();
            for(int i = 0; i < word.length(); i++) {
                stemmer.add(word.charAt(i));
            }
            stemmer.stem();
            newText += stemmer.toString() + " ";
        }
        return newText;
    }

    public Integer containgDocumentsCount(String[] documents, String keyword) {
        Integer result = 0;
        for (String document : documents) {
            result += Arrays.asList(document.split(" ")).contains(keyword) ? 1 : 0;
        }
        return result;
    }

    public HashMap<String, Double> getBagOfWords(String document, String[] keywordsArray) {
        HashMap<String, Double> bagOfWords = new HashMap<>();
        for (String word : document.split(" ")) {
            if( Arrays.asList(keywordsArray).contains(word)) {
                if(bagOfWords.getOrDefault(word, -1.0) == -1.0) {
                    bagOfWords.put(word, 1.0);
                }
                bagOfWords.replace(word, bagOfWords.get(word)+1);
            }
        }
        return bagOfWords;
    }

    public Double getMaxWordCount(HashMap<String, Double> bagOfWords) {
        Double maxWordsCount = .0;
        for (Map.Entry<String, Double> stringIntegerEntry : bagOfWords.entrySet()) {
            if(stringIntegerEntry.getValue() > maxWordsCount) {
                maxWordsCount = stringIntegerEntry.getValue();
            }
        }
        return maxWordsCount;
    }

    public HashMap<String, Double> getTF(HashMap<String, Double> bagOfWords) {
        HashMap<String, Double> TF = new HashMap<>(bagOfWords);
        for (Map.Entry<String, Double> stringIntegerEntry : TF.entrySet()) {
            TF.replace(stringIntegerEntry.getKey(), stringIntegerEntry.getValue()/getMaxWordCount(bagOfWords));
        }
        return TF;
    }

    public SearchTFIDF(String documentsFilename, String keywordFilename) {
        kw = new Keywords(loadFromFile("./src/main/resources/" + keywordFilename));
        DocumentsList = new ArrayList<>();
        for (String content : loadFromFile("./src/main/resources/" + documentsFilename).split("\n\n")) {
            DocumentsList.add(new Document(content));
        }
        for (Document document : DocumentsList) {
            document.getTFIDF(kw, kw.getIDF(DocumentsList));
        }
        System.out.println("============== READY ==============");
    }

    public String search(String Q) {
        Document query = new Document(Q + "\n");
        ArrayList<Pair<Double, Document>> ranking = new ArrayList<>();

//        System.out.println("query: " + query.getStemmedText());
        HashMap<String, Double> qTFIDF = query.getTFIDF(kw, kw.getIDF(DocumentsList));
        for(Document document : DocumentsList) {
            Double sum = .0;
            Double sum2 = .0;
            Double sum3 = .0;

            HashMap<String, Double> documentTFIDF = document.getTFIDF(kw, kw.getIDF(DocumentsList));
            for (String keyword : query.getStemmedText().split(" ")) {
                Double a = qTFIDF.getOrDefault(keyword, .0);
                Double b = documentTFIDF.getOrDefault(keyword, .0);
                sum += a * b;
                sum2 += Math.pow(a,2);
                sum3 += Math.pow(a,2);
            }
            ranking.add(new Pair<>(sum / Math.sqrt(sum2) / Math.sqrt(sum3), document));
//            System.out.println(sum + " / " + Math.sqrt(sum2) + " / " + Math.sqrt(sum3) +" = " + sum / Math.sqrt(sum2) / Math.sqrt(sum3));
        }

        try {
            Collections.sort(ranking, new Comparator<Pair<Double, Document>>() {
                @Override
                public int compare(final Pair<Double, Document> o1, final Pair<Double, Document> o2) {
                    return (o1.getKey() - o2.getKey()) < 0 ? 1 : -1;
                }
            });
        } catch (IllegalArgumentException e) {
//            System.out.println("UPS");
        }

        JSONArray result = new JSONArray();
        for (Pair<Double, Document> doubleStringPair : ranking) {
            try {
                JSONObject obj = new JSONObject();
                Document document = doubleStringPair.getValue();
                obj.put("title", document.getTitle());
                obj.put("value", doubleStringPair.getKey());
                obj.put("content", document.getContent());
                obj.put("stemmed", document.getStemmedText());
                if(doubleStringPair.getKey() > 0 ) {
                    result.put(obj);
                }
            } catch (JSONException e) {
//                System.out.println("ERROR");
            }
        }

        return result.toString();
    }
}
