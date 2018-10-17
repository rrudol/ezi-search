import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentTest {

    @Test
    void createDocument() {
        Document document = new Document("David W. Aha:  Machine Learning Page\n" +
                "Machine Learning Resources. Suggestions welcome. ... (WizRule); ZDM Scientific\n" +
                "Ltd. Conference Announcements. Courses on Machine Learning. Data Repositories. ...\n" +
                "Description: Comprehensive machine learning resources from Applications to Tutorials.");
        assertEquals(document.getTitle(), "David W. Aha:  Machine Learning Page");
        assertEquals(document.getContent(), "Machine Learning Resources. Suggestions welcome. ... (WizRule); ZDM Scientific\n" +
                "Ltd. Conference Announcements. Courses on Machine Learning. Data Repositories. ...\n" +
                "Description: Comprehensive machine learning resources from Applications to Tutorials.");
    }

    @Test
    void stemDocument() {
        Document document = new Document("David W. Aha:  Machine Learning Page\n" +
                "Machine Learning Resources. Suggestions welcome. ... (WizRule); ZDM Scientific\n" +
                "Ltd. Conference Announcements. Courses on Machine Learning. Data Repositories. ...\n" +
                "Description: Comprehensive machine learning resources from Applications to Tutorials.");
        assertEquals(document.getStemmedText(), ("david w aha  machin learn page\n" +
                "machin learn resourc suggest welcom  wizrul zdm scientific\n" +
                "ltd confer announc cours on machin learn data repositori \n" +
                "descript comprehens machin learn resourc from applic to tutori "));
    }

    @Test
    void bagOfWords() {
        Document document = new Document("David W. Aha:  Machine Learning Page\n" +
                "Machine Learning Resources. Suggestions welcome. ... (WizRule); ZDM Scientific\n" +
                "Ltd. Conference Announcements. Courses on Machine Learning. Data Repositories. ...\n" +
                "Description: Comprehensive machine learning resources from Applications to Tutorials.");
        Keywords keywords = new Keywords("Machine\nLearn\nPage");
        System.out.println(document.getStemmedText());
        HashMap<String, Double> bagOfWords = document.getBagOfWords(keywords);
        assertEquals(bagOfWords.getOrDefault("machin", .0), (Double) 4.0);
    }

    @Test
    void getMaxWordCount() {
        Document document = new Document("David W. Aha:  Machine Learning Page\n" +
                "Machine Learning Resources. Suggestions welcome. ... (WizRule); ZDM Scientific\n" +
                "Ltd. Conference Announcements. Courses on Machine Learning. Data Repositories. ...\n" +
                "Description: Comprehensive machine learning resources from Applications to Tutorials.");
        Keywords keywords = new Keywords("Machine\nLearn\nPage");
        assertEquals(document.getMaxWordCount(keywords), (Double) 4.0);
    }

    @Test
    void getTF() {
        Document document = new Document("David W. Aha:  Machine Learning Page\n" +
                "Machine Learning Resources. Suggestions welcome. ... (WizRule); ZDM Scientific\n" +
                "Ltd. Conference Announcements. Courses on Machine Learning. Data Repositories. ...\n" +
                "Description: Comprehensive machine learning resources from Applications to Tutorials.");
        Keywords keywords = new Keywords("Machine\nLearn\nPage");
        assertEquals(document.getTF(keywords).getOrDefault("machin", .0), (Double) .75);
        assertEquals(document.getTF(keywords).getOrDefault("learn", .0), (Double) 1.);
    }
}