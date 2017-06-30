package DBM;

import Index.IndexUtility;
import Utility.Constants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Zohreh on 6/30/2017.
 */
public class DBM_RECENT {
    IndexUtility u;
    IndexReader reader;
    IndexSearcher searcher;
    Analyzer analyzer;
    HashMap<Integer, Double> ExprtiseScore; // key = user , values= expertise score

    public DBM_RECENT() {
        try {
            u = new IndexUtility();
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(Constants.IndexDirectory)));
            searcher = new IndexSearcher(reader);
            analyzer = new EnglishAnalyzer();
            ExprtiseScore = new HashMap<Integer, Double>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startDBM_RECENT() {
        for (String tag : Constants.TopTags) {
            for (int CurrentYear = 2008; CurrentYear < 2016; CurrentYear++) {
                PrintStream stdout = System.out;
                try {
                    PrintStream out = new PrintStream(new FileOutputStream(Constants.DBM_RECENT_Directory + "DBM_Recent_" + tag + "_FYear_" + (CurrentYear+1) + ".txt"));
                    System.setOut(out);
                } catch (IOException e) {
                    System.out.println("\n\n\n\nSorry!\n\n\n\n");
                }

                getRelevantDocuments(tag, CurrentYear);

                Map<Integer, Double> map = sortByValues(ExprtiseScore);
                Set set = map.entrySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry expert_score = (Map.Entry) iterator.next();
                    System.out.println(expert_score.getKey() + "," + tag + "," + (CurrentYear+1) + "," + expert_score.getValue());
                }
                ExprtiseScore.clear();
                System.setOut(stdout);
            }
        }
    }

    /**
     * Search for the documents of specific tag
     * @param query tag
     * @param cyear current year
     */
    private void getRelevantDocuments(String query, int cyear) {
        try {
            Query btQ = SearchBodyTitle(query);
            if (btQ != null) {
                //Query q = u.BooleanQueryAnd(u.SearchCreationDateRange(2008, year),btQ);
                Query q = u.BooleanQueryAnd(u.SearchCreationDate(cyear), btQ);

                LMJelinekMercerSimilarity sim = new LMJelinekMercerSimilarity(0.7f);
                searcher.setSimilarity(sim);
                TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs = hits.scoreDocs;
                for (int i = 0; i < ScDocs.length; ++i) {
                    int docId = ScDocs[i].doc;
                    Document d = searcher.doc(docId);
                    //int id = Integer.parseInt(d.get("Id"));
                    double score = ScDocs[i].score;
                    //System.out.println("Score: "+score);
                    int eid = Integer.parseInt(d.get("OwnerUserId"));
                    if (ExprtiseScore.containsKey(eid))
                        ExprtiseScore.put(eid, ExprtiseScore.get(eid) + score);
                    else
                        ExprtiseScore.put(eid, score);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Query SearchBodyTitle(String query) {
        Analyzer analyzer = new EnglishAnalyzer();
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[]{"Body", "Title"}, analyzer);
        try {
            return queryParser.parse(query);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList<>(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public static void main(String[] args) {
        DBM_RECENT b = new DBM_RECENT();
        b.startDBM_RECENT();
    }
}
