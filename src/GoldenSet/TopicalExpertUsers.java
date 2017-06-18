package GoldenSet;

import Index.IndexUtility;
import Utility.Constants;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.util.*;
import java.nio.file.Paths;

/**
 * Created by Zohreh on 6/18/2017.
 */
public class TopicalExpertUsers {
    IndexUtility u;
    IndexSearcher searcher;
    IndexReader reader;
    HashMap<Integer, Integer> Expertise;// keys=expertID values= NumberOfCorrectAnswer

    public TopicalExpertUsers() {
        try {
            u = new IndexUtility();
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(Constants.IndexDirectory)));
            searcher = new IndexSearcher(reader);
            Expertise = new HashMap<Integer, Integer>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void FindExperts() {
        PrintStream stdout = System.out;
        try {
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicGoldenSetDirectory + "TopicExperts.txt"));
            System.setOut(out);
        } catch (IOException e) {
            System.out.println("\n\n\n\nSorry!\n\n\n\n");
        }

        for (int year = 2008; year < 2016; year++) {
            for (int topic = 0; topic < 50; topic++) {
                SearchForQuestions(year, topic);
            }
        }
        System.setOut(stdout);
    }

    private void SearchForQuestions(int year, int topic) {
        try {
            Query q = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic), u.SearchCreationDate(year)));
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            ScoreDoc[] ScDocs = hits.scoreDocs;

            for (int i = 0; i < ScDocs.length; i++) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                if (Integer.parseInt(d.get("AcceptedAnswerId")) != -1)
                    SearchForOwnerOfCorrectAnswer(Integer.parseInt(d.get("AcceptedAnswerId")), year);
            }

            Iterator it = Expertise.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.println(topic + "," + year + "," + pair.getKey() + "," + pair.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }
            Expertise.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void SearchForOwnerOfCorrectAnswer(int acceptedAnswerId, int year) {
        Query q = u.BooleanQueryAnd(u.SearchPostId(acceptedAnswerId), u.SearchCreationDate(year));
        try {
            TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; i++) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                Integer oid = Integer.parseInt(d.get("OwnerUserId"));
                if (Expertise.containsKey(oid))
                    Expertise.put(oid, Expertise.get(oid) + 1);
                else
                    Expertise.put(oid, 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
