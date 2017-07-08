package TPBM;

import Index.IndexUtility;
import Utility.Constants;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Zohreh on 7/8/2017.
 */
public class PWA {
    public static final String[] TopTags = new String[]{"java", "android", "swing", "spring", "eclipse", "hibernate", "multithreading", "arrays", "xml", "jsp", "maven", "servlets", "string", "mysql", "spring-mvc", "java-ee", "json", "jpa", "tomcat", "regex", "jdbc", "web-services", "arraylist", "sql", "javascript", "sockets", "generics", "netbeans", "user-interface", "jar", "file", "junit", "database", "google-app-engine", "exception", "html", "rest", "algorithm", "jsf", "gwt", "class", "performance", "image", "applet", "jframe", "jtable", "nullpointerexception", "methods", "linux", "collections", "jpanel"};

    Set<String> QueryWords;
    HashMap<Integer, Integer> TopicCounts;//key = Topic , value= Count
    HashMap<String, HashMap<Integer, Integer>> QueryTopicCounts;// key = Query , value=(key=Topic , value=Count )

    public PWA() {
        TopicCounts = new HashMap<Integer, Integer>();
        for (int topic = 0; topic < 50; topic++)
            TopicCounts.put(topic, 0);

        QueryTopicCounts = new HashMap<String, HashMap<Integer, Integer>>();
        for (String tag : TopTags) {
            HashMap<Integer, Integer> TopicCountsForEachTag = new HashMap<Integer, Integer>();
            for (int topic = 0; topic < 50; topic++)
                TopicCountsForEachTag.put(topic, 0);
            QueryTopicCounts.put(tag, TopicCountsForEachTag);
        }
        QueryWords = new HashSet<String>(Arrays.asList(TopTags));
    }

    public void startPWACalculations() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.MalletWordTopic));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(" ");
                for (int i = 2; i < temp.length; i++) {
                    String[] TopicCountPair = temp[i].split(":");
                    Integer Topic = Integer.parseInt(TopicCountPair[0]);
                    Integer Count = Integer.parseInt(TopicCountPair[1]);
                    TopicCounts.put(Topic, TopicCounts.get(Topic) + Count);
                    if (QueryWords.contains(temp[1])){
                        HashMap<Integer, Integer> TopicCountsForEachTag = QueryTopicCounts.get(temp[1]);
                        TopicCountsForEachTag.put(Topic, TopicCountsForEachTag.get(Topic) + Count);
                    }
                }
            }
            reader.close();

            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.Features_Directory + "PWA.txt"));
            System.setOut(out);
            System.out.println("Word,Topic,PWA");
            for (String tag : TopTags) {
                HashMap<Integer, Integer> TopicCountsForEachTag = QueryTopicCounts.get(tag);
                for (int topic = 0; topic < 50; topic++)
                    if(TopicCountsForEachTag.get(topic) != 0)
                        System.out.println(tag+","+topic+","+(TopicCountsForEachTag.get(topic)*1.0/TopicCounts.get(topic)));
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PWA p = new PWA();
        p.startPWACalculations();
    }
}
