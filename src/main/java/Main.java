import javafx.util.Pair;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static spark.Spark.*;

public class Main {

    public static void word() {
        Dictionary dictionary = null;
        try {
            dictionary = Dictionary.getDefaultResourceInstance();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
//        dictionary.
    }

    public static void main(String[] args) {
        word();

        if (true) {
            String projectDir = System.getProperty("user.dir");
            String staticDir = "/src/main/resources/public";
            staticFiles.externalLocation(projectDir + staticDir);
        } else {
            staticFiles.location("/public");
        }
        SearchTFIDF searchTfIdf = new SearchTFIDF("documents.txt", "keywords.txt");
        post("/search", (request, response) -> {
            HashMap<String, String> Data = new HashMap<String, String>();
            String[] fields = request.body().split("&");
            for (String field : fields) {
                String[] keyValue = field.split("=");
                Data.put(keyValue[0], keyValue[1]);
            }
            return searchTfIdf.search(Data.getOrDefault("query", ""), false);
        });

        post("/search-extended", (request, response) -> {
            HashMap<String, String> Data = new HashMap<String, String>();
            String[] fields = request.body().split("&");
            for (String field : fields) {
                String[] keyValue = field.split("=");
                Data.put(keyValue[0], keyValue[1]);
            }
            return searchTfIdf.search(Data.getOrDefault("query", ""), true);
        });
    }
}
