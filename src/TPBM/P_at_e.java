package TPBM;

import Index.IndexUtility;
import Utility.Constants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Zohreh on 7/8/2017.
 */
public class P_at_e {
    IndexUtility u;
    IndexReader reader;
    IndexSearcher searcher;
    Analyzer analyzer;

    // key=User , value=( key=topic , value=Count )
    HashMap<Integer, HashMap<Integer, Integer>> Experts_TopicCounts;

    // key=User , value=Count
    HashMap<Integer, Integer> Experts_SumCounts;

    public P_at_e() {
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(Constants.IndexDirectory)));
            searcher = new IndexSearcher(reader);
            analyzer = new EnglishAnalyzer();
            u = new IndexUtility();
            Experts_TopicCounts = new HashMap<Integer, HashMap<Integer, Integer>>();
            Experts_SumCounts = new HashMap<Integer, Integer>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        P_at_e p = new P_at_e();
        p.P_at_e_AnswerVersion();
        p.P_at_e_QuestionAnswerVersion();

        p.TopicUserActivity_V2_Answer();
    }

    public void P_at_e_AnswerVersion() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.Features_Directory + "Pate_Answer.txt"));
            System.setOut(out);

            System.out.println("OwnerUserId,CurrentYear,Topic,Pate");

            for (int year = 2008; year < 2016; year++) {
                Query q = u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchPostTypeID(2));
                TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs = hits.scoreDocs;

                for (int i = 0; i < ScDocs.length; ++i) {
                    int docId = ScDocs[i].doc;
                    Document d = searcher.doc(docId);
                    Integer eid = Integer.parseInt(d.get("OwnerUserId"));
                    for (IndexableField t : d.getFields("Topics")) {
                        Integer topic = Integer.parseInt(t.stringValue());
                        if(topic == -1)
                            continue;
                        if (Experts_TopicCounts.containsKey(eid)) {
                            HashMap<Integer, Integer> topicsCounts = Experts_TopicCounts.get(eid);
                            if (topicsCounts.containsKey(topic)) {
                                topicsCounts.put(topic,topicsCounts.get(topic)+1);
                            } else {
                                topicsCounts.put(topic,1);
                            }
                            Experts_SumCounts.put(eid, Experts_SumCounts.get(eid) + 1);
                        } else {
                            HashMap<Integer, Integer> topicsCounts = new HashMap<Integer, Integer>();
                            topicsCounts.put(topic, 1);
                            Experts_TopicCounts.put(eid, topicsCounts);
                            Experts_SumCounts.put(eid,1);
                        }
                    }
                }

                for(int e: Experts_TopicCounts.keySet()){
                    Integer Sum = Experts_SumCounts.get(e);
                    HashMap<Integer, Integer> topicsCounts = Experts_TopicCounts.get(e);
                    for(Integer topic: topicsCounts.keySet()){
                        Integer Count = topicsCounts.get(topic);
                        System.out.println(e+","+year+","+topic+","+(Count*1.0/Sum));
                    }
                }

                Experts_TopicCounts.clear();
                Experts_SumCounts.clear();
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void P_at_e_QuestionAnswerVersion() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.Features_Directory + "Pate_QuestionAnswer.txt"));
            System.setOut(out);

            for (int year = 2008; year < 2016; year++) {
                Query q = u.SearchCreationDate(year);
                TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs = hits.scoreDocs;

                for (int i = 0; i < ScDocs.length; ++i) {
                    int docId = ScDocs[i].doc;
                    Document d = searcher.doc(docId);
                    Integer eid = Integer.parseInt(d.get("OwnerUserId"));
                    for (IndexableField t : d.getFields("Topics")) {
                        Integer topic = Integer.parseInt(t.stringValue());
                        if(topic == -1)
                            continue;
                        if (Experts_TopicCounts.containsKey(eid)) {
                            HashMap<Integer, Integer> topicsCounts = Experts_TopicCounts.get(eid);
                            if (topicsCounts.containsKey(topic)) {
                                topicsCounts.put(topic,topicsCounts.get(topic)+1);
                            } else {
                                topicsCounts.put(topic,1);
                            }
                            Experts_SumCounts.put(eid, Experts_SumCounts.get(eid) + 1);
                        } else {
                            HashMap<Integer, Integer> topicsCounts = new HashMap<Integer, Integer>();
                            topicsCounts.put(topic, 1);
                            Experts_TopicCounts.put(eid, topicsCounts);
                            Experts_SumCounts.put(eid,1);
                        }
                    }
                }

                for(int e: Experts_TopicCounts.keySet()){
                    Integer Sum = Experts_SumCounts.get(e);
                    HashMap<Integer, Integer> topicsCounts = Experts_TopicCounts.get(e);
                    for(Integer topic: topicsCounts.keySet()){
                        Integer Count = topicsCounts.get(topic);
                        System.out.println(e+","+year+","+topic+","+(Count*1.0/Sum));
                    }
                }

                Experts_TopicCounts.clear();
                Experts_SumCounts.clear();
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void TopicUserActivity_V2_Answer(){
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.Features_Directory + "TopicUserActivity_V2.txt"));
            System.setOut(out);

            System.out.println("expert\tyear\ttopic\tsum");
            for (int year = 2008; year < 2016; year++) {
                Query q = u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchPostTypeID(2));
                TopDocs hits = searcher.search(q, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs = hits.scoreDocs;

                for (int i = 0; i < ScDocs.length; ++i) {
                    int docId = ScDocs[i].doc;
                    Document d = searcher.doc(docId);
                    Integer eid = Integer.parseInt(d.get("OwnerUserId"));
                    if(eid == -1)
                        continue;

                    for (IndexableField t : d.getFields("Topics")) {
                        Integer topic = Integer.parseInt(t.stringValue());
                        if(topic == -1)
                            continue;
                        if (Experts_TopicCounts.containsKey(eid)) {
                            HashMap<Integer, Integer> topicsCounts = Experts_TopicCounts.get(eid);
                            if (topicsCounts.containsKey(topic)) {
                                topicsCounts.put(topic,topicsCounts.get(topic)+1);
                            } else {
                                topicsCounts.put(topic,1);
                            }
                        } else {
                            HashMap<Integer, Integer> topicsCounts = new HashMap<Integer, Integer>();
                            topicsCounts.put(topic, 1);
                            Experts_TopicCounts.put(eid, topicsCounts);
                        }
                    }
                }

                for(int e: Experts_TopicCounts.keySet()){
                    HashMap<Integer, Integer> topicsCounts = Experts_TopicCounts.get(e);
                    for(Integer topic: topicsCounts.keySet()){
                        Integer Count = topicsCounts.get(topic);
                        System.out.println(e+"\t"+year+"\t"+topic+"\t"+Count);
                    }
                }

                Experts_TopicCounts.clear();

            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
