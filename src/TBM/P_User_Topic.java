package TBM;

import Index.IndexUtility;
import Utility.Constants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by Zohreh on 5/19/2018.
 * Calculate p(e|m) probability of TBM baseline
 */
public class P_User_Topic {
    IndexUtility u;
    IndexReader reader;
    IndexSearcher searcher;
    Analyzer analyzer;
    HashMap<Integer, Integer> User_TopicCounts;

    public P_User_Topic() {
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(Constants.IndexDirectory)));
            searcher = new IndexSearcher(reader);
            analyzer = new EnglishAnalyzer();
            u = new IndexUtility();
            User_TopicCounts = new HashMap<Integer, Integer>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void get_P_E_M() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.Features_Directory + "P_User_Topic.txt"));
            System.setOut(out);
            System.out.println("User,Year,Topic,P(e|m)");

            for (int year = 2008; year < 2016; year++) {
                for (int topic = 0; topic < 50; topic++) {
                    Query q = u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchTopic(topic));
                    TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs = hits.scoreDocs;
                    Integer TopicCount = ScDocs.length;

                    for (int i = 0; i < ScDocs.length; ++i) {
                        int docId = ScDocs[i].doc;
                        Document d = searcher.doc(docId);
                        Integer eid = Integer.parseInt(d.get("OwnerUserId"));
                        if (User_TopicCounts.containsKey(eid)) {
                            User_TopicCounts.put(eid, User_TopicCounts.get(eid) + 1);
                        } else {
                            User_TopicCounts.put(eid, 1);
                        }
                    }

                    for (int e : User_TopicCounts.keySet()) {
                        Integer UserTopicCounts = User_TopicCounts.get(e);
                        System.out.println(e + "," + year + "," + topic + "," + (UserTopicCounts * 1.0 / TopicCount));
                    }

                    User_TopicCounts.clear();
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        P_User_Topic p = new P_User_Topic();
        p.get_P_E_M();
    }
}
