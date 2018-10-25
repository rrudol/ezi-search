import javafx.util.Pair;

import java.io.*;
import java.util.*;
import org.json.*;

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

    public String search(String Q, Boolean suggestion) {
        Document query = new Document(Q + "\n");
        ArrayList<Pair<Double, Document>> ranking = new ArrayList<>();

//        System.out.println("query: " + query.getStemmedText());
        HashMap<String, Double> qTFIDF = query.getTFIDF(kw, kw.getIDF(DocumentsList));

        for(Document document : DocumentsList) {
            Double sum = .0;
            Double sum2 = .0;
            Double sum3 = .0;

            HashMap<String, Double> documentTFIDF = document.getTFIDF(kw, kw.getIDF(DocumentsList));
            //for (String keyword : query.getStemmedText().split(" ")) {
            for (String keyword : kw.getStemmedList()) {
                Double a = qTFIDF.getOrDefault(keyword, .0);
                Double b = documentTFIDF.getOrDefault(keyword, .0);
                sum += a * b;
                sum2 += a*a;
                sum3 += b*b;

            }
            ranking.add(new Pair<>(sum / (Math.sqrt(sum2) * Math.sqrt(sum3)), document));
            System.out.println(sum + " / " + Math.sqrt(sum2) + " / " + Math.sqrt(sum3) +" = " + sum / (Math.sqrt(sum2) * Math.sqrt(sum3)));
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

        JSONObject res = new JSONObject();
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
        res.put("results", result);

        Double alpha = 1.00;
        Double beta  = 0.75;
        Double gamma = 0.25;

        ArrayList<Pair<Double, JSONObject>> suggestionsList = new ArrayList<>();
        if(suggestion) {
            HashMap<String, Double> bagOfWordsOfQuery = query.getBagOfWords(kw);
            ArrayList<HashMap<String, Double>> listOfBestDocumentsBagOfWords = new ArrayList<>();
            ArrayList<HashMap<String, Double>> listOfWorstDocumentsBagOfWords = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                listOfBestDocumentsBagOfWords.add(ranking.get(i).getValue().getBagOfWords(kw));
            }

            for (int i = 0; i < 5; i++) {
                listOfWorstDocumentsBagOfWords.add(ranking.get(ranking.size()-i-1).getValue().getBagOfWords(kw));
            }

            List<String> stemmedList = kw.getStemmedList();
            for (int i = 0; i < stemmedList.size(); i++) {
                String keyword = stemmedList.get(i);
                String originalKeyword = kw.getList().get(i);
                if(query.getStemmedText().contains(keyword)) continue;
                int sumOfBestDocumentsBagOfWordsValues = 0;
                int sumOfWorstDocumentsBagOfWordsValues = 0;
                for (HashMap<String, Double> bestDocumentBagOfWord : listOfBestDocumentsBagOfWords) {
                    sumOfBestDocumentsBagOfWordsValues += bestDocumentBagOfWord.getOrDefault(keyword, .0);
                    //System.out.println(sumOfBestDocumentsBagOfWordsValues);
                }
                for (HashMap<String, Double> worstDocumentBagOfWord : listOfWorstDocumentsBagOfWords) {
                    sumOfWorstDocumentsBagOfWordsValues += worstDocumentBagOfWord.getOrDefault(keyword, .0);

                }
                System.out.println(alpha);
                Double value = bagOfWordsOfQuery.getOrDefault(keyword, .0) * alpha + (1./5.) * beta * sumOfBestDocumentsBagOfWordsValues - (1./5.) * gamma * sumOfWorstDocumentsBagOfWordsValues;
                System.out.println(new Pair<>(value, keyword));
                JSONObject sugestion = new JSONObject();
                sugestion.put("value", query.getText() + ' ' + originalKeyword);
                sugestion.put("q", value);
                suggestionsList.add(new Pair<>(value, sugestion));
            }
        }

        try {
            Collections.sort(suggestionsList, new Comparator<Pair<Double, JSONObject>>() {
                @Override
                public int compare(final Pair<Double, JSONObject> o1, final Pair<Double, JSONObject> o2) {
                    return (o1.getKey() - o2.getKey()) < 0 ? 1 : -1;
                }
            });
        } catch (IllegalArgumentException e) {
//            System.out.println("UPS");
        }

        JSONArray suggestionsArrayJSON = new JSONArray();

        for (int i = 0; i < Math.min(suggestionsList.size(), 5); i++) {
            suggestionsArrayJSON.put(suggestionsList.get(i).getValue());
        }
        res.put("suggestions", suggestionsArrayJSON);

        return res.toString();
    }
}
