package FeatureExtraction;

import Index.IndexUtility;
import Utility.Constants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Zohreh on 1/28/2018.
 */
public class TopicTransition {
    IndexUtility u;
    IndexReader reader;
    IndexSearcher searcher;
    Analyzer analyzer;
    HashMap<String, HashMap<Integer, Integer>> TopicExperts;// key="Topic,Year" , value=( key=User , value=NumberOfCorrectAnswers )

    public TopicTransition() {
        try {
            u = new IndexUtility();
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(Constants.IndexDirectory)));
            searcher = new IndexSearcher(reader);
            analyzer = new EnglishAnalyzer();
            TopicExperts = new HashMap<String, HashMap<Integer, Integer>>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTopicExpertsFile() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.TopicGoldenSetDirectory + "TopicExperts.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] TC = line.split(",");
                if (TopicExperts.containsKey(TC[0] + "," + TC[1])) {
                    TopicExperts.get(TC[0] + "," + TC[1]).put(Integer.parseInt(TC[2]), Integer.parseInt(TC[3]));
                } else {
                    HashMap<Integer, Integer> val = new HashMap<Integer, Integer>();
                    val.put(Integer.parseInt(TC[2]), Integer.parseInt(TC[3]));
                    TopicExperts.put(TC[0] + "," + TC[1], val);
                }
            }
            reader.close();
            //System.out.println(TopicExperts.get("49,2015").toString());//49,2015,3448419,3
            //System.out.println(TopicExperts.get("49,2015").get(3448419));//49,2015,3448419,3
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature:  F22 in Paper
     */
    private void getTopicTransition1() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicTransitionDirectory + "TopicTransition1.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,TopicTransition1");
            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    Query exQ = u.BooleanQueryAnd(u.SearchTopic(topic1), u.SearchCreationDate(year));
                    TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs = hits.scoreDocs;
                    HashSet<Integer> CurrentYearUsers = new HashSet<>();

                    for (int i = 0; i < ScDocs.length; ++i) {
                        int docId = ScDocs[i].doc;
                        Document d = searcher.doc(docId);
                        CurrentYearUsers.add(Integer.parseInt(d.get("OwnerUserId")));
                    }

                    for (int topic2 = 0; topic2 < 50; topic2++) {
                        Query exQ2 = u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year + 1));
                        TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                        ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                        HashSet<Integer> FutureYearUsers = new HashSet<>();

                        for (int i = 0; i < ScDocs2.length; ++i) {
                            int docId = ScDocs2[i].doc;
                            Document d = searcher.doc(docId);
                            FutureYearUsers.add(Integer.parseInt(d.get("OwnerUserId")));
                        }

                        HashSet<Integer> IntersectionSet = new HashSet<>();
                        IntersectionSet.addAll(CurrentYearUsers);
                        IntersectionSet.retainAll(FutureYearUsers);

                        double output = (CurrentYearUsers.size() == 0) ? 0.0 : (IntersectionSet.size() * 1.0) / CurrentYearUsers.size();
                        System.out.println(topic1 + "," + topic2 + "," + year + "," + output);
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature:  F23 in Paper
     */
    private void getSumOfTopicTransition1() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicTransitionDirectory + "SumTopicTransition1.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,SumTopicTransition1");
            HashMap<String, Double> TopicTransitions = new HashMap<String, Double>();//key= "topic1+","+topic2+","+year" value= TopicTransition
            BufferedReader reader = new BufferedReader(new FileReader(Constants.TopicTransitionDirectory + "TopicTransition1.txt"));
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(",");
                TopicTransitions.put(temp[0] + "," + temp[1] + "," + temp[2], Double.parseDouble(temp[3]));
            }
            reader.close();

           for (int topic1 = 0; topic1 < 50; topic1++)
                for (int topic2 = 0; topic2 < 50; topic2++)
                    for (int year = 2008; year < 2015; year++) {
                        double output = 0.0;
                        for (int y = 2008; y <= year; y++) {
                            output += TopicTransitions.get(topic1 + "," + topic2 + "," + y);
                        }
                        System.out.println(topic1 + "," + topic2 + "," + year + "," + output);
                    }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature:  F24 in Paper
     */
    private void getTopicTransition2() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicTransitionDirectory + "TopicTransition2.txt"));
            System.setOut(out);
            System.out.println("Topic2,Year,TopicTransition2");
            for (int year = 2008; year < 2016; year++) {
                Query exQ = u.SearchCreationDate(year);
                TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs = hits.scoreDocs;
                HashSet<Integer> CurrentYearUsers = new HashSet<>();
                HashMap<Integer, HashSet<Integer>> CurrentYearUsersOfEachTopic = new HashMap<Integer, HashSet<Integer>>();

                for (int i = 0; i < ScDocs.length; ++i) {
                    int docId = ScDocs[i].doc;
                    Document d = searcher.doc(docId);
                    Integer OwnerUserId = Integer.parseInt(d.get("OwnerUserId"));
                    CurrentYearUsers.add(OwnerUserId);
                    for (IndexableField t : d.getFields("Topics")) {
                        Integer Topic = Integer.parseInt(t.stringValue());
                        if (Topic == -1)
                            continue;

                        if (CurrentYearUsersOfEachTopic.containsKey(Topic)) {
                            CurrentYearUsersOfEachTopic.get(Topic).add(OwnerUserId);
                        } else {
                            HashSet<Integer> TopicUsers = new HashSet<Integer>();
                            TopicUsers.add(OwnerUserId);
                            CurrentYearUsersOfEachTopic.put(Topic, TopicUsers);
                        }
                    }
                }

                for (Integer topic : CurrentYearUsersOfEachTopic.keySet()) {
                    double output = (CurrentYearUsers.size() == 0) ? 0.0 : (CurrentYearUsersOfEachTopic.get(topic).size() * 1.0) / CurrentYearUsers.size();
                    System.out.println(topic + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * For n=1: F19 in Paper
     * For n=3: F20 in Paper
     * For n=10: F21 in Paper
     */
    private void getTopicTransition3(int n) {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicTransitionDirectory + "TopicTransition3_" + n + ".txt"));
            System.setOut(out);
            System.out.println("Topic1,Topic2,Year,TopicTransition3_" + n);

            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    HashSet<Integer> CurrentYearUsers = new HashSet<>();
                    HashMap<Integer, Integer> UserCorrectAnswerCount = TopicExperts.get(topic1 + "," + year);
                    if (!UserCorrectAnswerCount.isEmpty()) {
                        for (Integer ExpertID : UserCorrectAnswerCount.keySet())
                            if (UserCorrectAnswerCount.get(ExpertID) >= n)
                                CurrentYearUsers.add(ExpertID);

                        for (int topic2 = 0; topic2 < 50; topic2++) {
                            HashSet<Integer> FutureYearUsers = new HashSet<>();
                            HashMap<Integer, Integer> UserCorrectAnswerCount2 = TopicExperts.get(topic2 + "," + (year + 1));
                            if (!UserCorrectAnswerCount2.isEmpty()) {
                                for (Integer ExpertID : UserCorrectAnswerCount2.keySet())
                                    if (UserCorrectAnswerCount2.get(ExpertID) >= n)
                                        FutureYearUsers.add(ExpertID);

                                HashSet<Integer> IntersectionSet = new HashSet<>();
                                IntersectionSet.addAll(CurrentYearUsers);
                                IntersectionSet.retainAll(FutureYearUsers);

                                double output = (CurrentYearUsers.size() == 0) ? 0.0 : (IntersectionSet.size() * 1.0) / CurrentYearUsers.size();
                                System.out.println(topic1 + "," + topic2 + "," + year + "," + output);
                            }
                        }
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getSumOfTopicTransition3(int n) {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicTransitionDirectory + "SumTopicTransition3_" + n + ".txt"));
            System.setOut(out);
            System.out.println("Topic1,Topic2,Year,SumTopicTransition3_" + n);

            HashMap<String, Double> TopicTransitions = new HashMap<String, Double>();
            BufferedReader reader = new BufferedReader(new FileReader(Constants.TopicTransitionDirectory + "TopicTransition3_" + n + ".txt"));
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(",");
                TopicTransitions.put(temp[0] + "," + temp[1] + "," + temp[2], Double.parseDouble(temp[3]));
            }
            reader.close();

            for (int topic1 = 0; topic1 < 50; topic1++)
                for (int topic2 = 0; topic2 < 50; topic2++)
                    for (int year = 2008; year < 2015; year++) {
                        double output = 0.0;
                        for (int y = 2008; y <= year; y++) {
                            output += TopicTransitions.get(topic1 + "," + topic2 + "," + y);
                        }
                        System.out.println(topic1 + "," + topic2 + "," + year + "," + output);
                    }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /*TopicTransition f = new TopicTransition();

        f.getTopicTransition1();
        f.getSumOfTopicTransition1();

        f.getTopicTransition2();

        f.loadTopicExpertsFile();
        f.getTopicTransition3(1);
        f.getTopicTransition3(3);
        f.getTopicTransition3(5);
        f.getTopicTransition3(10);

        f.getSumOfTopicTransition3(1);
        f.getSumOfTopicTransition3(3);
        f.getSumOfTopicTransition3(5);
        f.getSumOfTopicTransition3(10);
        */
    }

    public void startFeatureCalculations() {
        getTopicTransition1(); //F22
        getSumOfTopicTransition1();//F23
        getTopicTransition2();//F24

        loadTopicExpertsFile();
        getTopicTransition3(1);//F19
        getTopicTransition3(3);//F20
        getTopicTransition3(10);//F21
    }
}
