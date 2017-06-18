package Index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by Zohreh on 6/18/2017.
 */
public class LuceneIndex {
    private Analyzer analyzer;
    private Directory fsDir;
    private Directory ramDir;
    private IndexWriter ramWriter, fileWriter;
    private IndexWriterConfig config;
    private LMJelinekMercerSimilarity sim;
    private final String indexPath;
    private int maxDocInMemory = 100000;
    private int countInMemoryDoc = 0;

    public LuceneIndex(String indexPath) {
        this.indexPath = indexPath;
        try {
            setUp();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    protected void setUp() throws IOException, ParseException {
        analyzer = new EnglishAnalyzer();
        ramDir = new RAMDirectory();
        fsDir = FSDirectory.open(Paths.get(indexPath));
        sim = new LMJelinekMercerSimilarity(0.7f);
        IndexWriterConfig config1 = new IndexWriterConfig(analyzer);
        config1.setSimilarity(sim);
        fileWriter = new IndexWriter(fsDir, config1);
        config = new IndexWriterConfig(analyzer);
        config.setSimilarity(sim);
        ramWriter = new IndexWriter(ramDir, config);
    }

    public void index(String xmlFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("<row")) {
                    Post post = new Post(line);
                    addToIndex(post.getLuceneDocument());
                }
            }
            reader.close();
            closeIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToIndex(Document document) throws IOException {
        if (countInMemoryDoc < maxDocInMemory) {
            // Add to Ram Memory and count up
            ramWriter.addDocument(document);
            countInMemoryDoc++;
        } else {
            System.out.println("Making index for " + countInMemoryDoc + " Documents.");
            // Merge Ram Memory and create a new ram memory
            ramWriter.addDocument(document);
            ramWriter.close();
            fileWriter.addIndexes(ramDir);
            ramDir.close();
            ramDir = new RAMDirectory();
            ramWriter = new IndexWriter(ramDir, new IndexWriterConfig(analyzer));
            countInMemoryDoc = 0;
        }
    }

    private void closeIndex() throws IOException {
        ramWriter.close();
        fileWriter.addIndexes(ramDir);
        ramDir.close();
        countInMemoryDoc = 0;
        fileWriter.close();
        fsDir.close();
    }

}
