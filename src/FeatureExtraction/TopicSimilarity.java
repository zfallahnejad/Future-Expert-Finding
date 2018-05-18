package FeatureExtraction;

import Index.IndexUtility;
import Utility.Constants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Zohreh on 5/17/2018.
 */
public class TopicSimilarity {
    IndexUtility u;

    public TopicSimilarity() {
        u = new IndexUtility();
    }

    /**
     * Feature:  F1 in Paper
     */
    private void getTopicSimilarity1() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicSimilarityDirectory + "TopicSimilarity1.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,TopicSimilarity1");
            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    for (int topic2 = 0; topic2 < 50; topic2++) {
                        double similarity = 0;
                        if (topic1 == topic2)
                            similarity = 1;
                        else {
                            HashSet<Integer> topic1PostIDs = u.getPostIDs(u.BooleanQueryAnd(u.SearchTopic(topic1), u.SearchCreationDate(year)));
                            HashSet<Integer> topic2PostIDs = u.getPostIDs(u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));

                            HashSet<Integer> IntersectionSet = new HashSet<Integer>();
                            IntersectionSet.addAll(topic1PostIDs);
                            IntersectionSet.retainAll(topic2PostIDs);
                            int intersectionC = IntersectionSet.size();

                            HashSet<Integer> UnionSet = new HashSet<Integer>();
                            UnionSet.addAll(topic1PostIDs);
                            UnionSet.addAll(topic2PostIDs);
                            int unionC = UnionSet.size();

                            similarity = (intersectionC * 1.0) / unionC;
                        }
                        System.out.println(topic1 + "," + topic2 + "," + year + "," + similarity);
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature:  F2 in Paper
     */
    private void getTopicSimilarity7() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicSimilarityDirectory + "TopicSimilarity7.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,TopicSimilarity7");
            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    Query q1 = u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchTopic(topic1));
                    HashSet<Integer> topic1Users = u.getExpertsBYTopicandYear(q1);

                    for (int topic2 = 0; topic2 < 50; topic2++) {
                        double similarity = 0;
                        if (topic1 == topic2)
                            similarity = 1;
                        else {
                            Query q2 = u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchTopic(topic2));
                            HashSet<Integer> topic2Users = u.getExpertsBYTopicandYear(q2);

                            HashSet<Integer> IntersectionSet = new HashSet<Integer>();
                            IntersectionSet.addAll(topic1Users);
                            IntersectionSet.retainAll(topic2Users);
                            int intersectionC = IntersectionSet.size();

                            topic2Users.addAll(topic1Users);
                            int unionC = topic2Users.size();
                            similarity = (intersectionC * 1.0) / unionC;
                        }
                        System.out.println(topic1 + "," + topic2 + "," + year + "," + similarity);
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TopicSimilarity s = new TopicSimilarity();
        s.getTopicSimilarity1();
        s.getTopicSimilarity7();
    }

    public void startFeatureCalculations() {
        getTopicSimilarity1();//F1
        getTopicSimilarity7();//F2
    }
}
