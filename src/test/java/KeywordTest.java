import org.junit.jupiter.api.Test;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KeywordTest {

    @Test
    void createKeywords() {
        Keywords keywords = new Keywords("Machine\nLearning\nPage");
        assertEquals(keywords.getList().get(0), "Machine");
        assertEquals(keywords.getList().get(1), "Learning");
        assertEquals(keywords.getList().get(2), "Page");
    }

    @Test
    void getStemmedList() {
        Keywords keywords = new Keywords("Machine\nLearning\nPage");
        assertEquals(keywords.getStemmedList().get(0), "machin");
        assertEquals(keywords.getStemmedList().get(1), "learn");
        assertEquals(keywords.getStemmedList().get(2), "page");
    }

    @Test
    void containgDocumentsCount() {
        Document document = new Document("Machine\nPage");
        ArrayList<Document> documents = new ArrayList<Document>();
        documents.add(document);
        documents.add(document);
        Keywords keywords = new Keywords("Machine\nLearning\nPage");
        assertEquals(keywords.containgDocumentsCount(documents, "machin"), (Integer) 2);
    }

}