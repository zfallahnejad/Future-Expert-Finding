package FeatureExtraction;

import Index.IndexUtility;
import TPBM.Conservativeness;
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
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Paths;
import java.time.Year;
import java.util.*;

/**
 * Created by Zohreh on 1/30/2018.
 */
public class UserFeature {
    IndexUtility u;
    IndexReader reader;
    IndexSearcher searcher;
    Analyzer analyzer;
    HashMap<String, HashMap<Integer, Integer>> TopicExperts;// key="Topic,Year" , value=( key=User , value=NumberOfCorrectAnswers )

    public UserFeature() {
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
     * For each year y :
     * return number of posts that each user posted in year y
     */
    private void getTotalActivityCount() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.UserFeatureDirectory + "TotalActivityCount.txt"));
            System.setOut(out);

            for (int cyear = 2008; cyear < 2016; cyear++) {
                HashMap<Integer, Integer> NtResults = new HashMap<Integer, Integer>();

                Query exQ = u.SearchCreationDate(cyear);
                TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs = hits.scoreDocs;

                for (int i = 0; i < ScDocs.length; ++i) {
                    int docId = ScDocs[i].doc;
                    Document d = searcher.doc(docId);
                    Integer eid = Integer.parseInt(d.get("OwnerUserId"));
                    if (NtResults.containsKey(eid))
                        NtResults.put(eid, NtResults.get(eid) + 1);
                    else
                        NtResults.put(eid, 1);
                }

                for (Integer userID : NtResults.keySet())
                    System.out.println(userID + "," + cyear + "," + NtResults.get(userID));
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * For each year y and each topic t:
     * return number of posts that each user posted in year y and topic t
     */
    private void getTopicActivityCount() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.UserFeatureDirectory + "TopicActivityCount.txt"));
            System.setOut(out);

            for (int cyear = 2008; cyear < 2016; cyear++) {
                HashMap<String, Integer> NtResults = new HashMap<String, Integer>();

                Query exQ = u.SearchCreationDate(cyear);
                TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs = hits.scoreDocs;

                for (int i = 0; i < ScDocs.length; ++i) {
                    int docId = ScDocs[i].doc;
                    Document d = searcher.doc(docId);
                    Integer eid = Integer.parseInt(d.get("OwnerUserId"));
                    for (IndexableField t : d.getFields("Topics")) {
                        Integer topic = Integer.parseInt(t.stringValue());
                        if (topic != -1) {
                            if (NtResults.containsKey(eid + "," + topic))
                                NtResults.put(eid + "," + topic, NtResults.get(eid + "," + topic) + 1);
                            else
                                NtResults.put(eid + "," + topic, 1);
                        }
                    }
                }
                for (String userIDTopic : NtResults.keySet())
                    System.out.println(userIDTopic + "," + cyear + "," + NtResults.get(userIDTopic));
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F18 in paper
     */
    private void getUserFeature1() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.UserFeatureDirectory + "UserFeature1.txt"));
            System.setOut(out);

            HashMap<String, Integer> NtResults = new HashMap<String, Integer>();
            BufferedReader reader = new BufferedReader(new FileReader(Constants.UserFeatureDirectory + "TotalActivityCount.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                //User,cyear,TotalActivityCount
                String[] TC = line.split(",");
                NtResults.put(TC[0] + "," + TC[1], Integer.parseInt(TC[2]));
            }
            reader.close();

            System.out.println("UserId,Topic,CurrentYear,UserFeature1");
            BufferedReader reader2 = new BufferedReader(new FileReader(Constants.UserFeatureDirectory + "\\TopicActivityCount.txt"));
            String line2;
            while ((line2 = reader2.readLine()) != null) {
                //User,Topic,Year,TopicActivityCount
                String[] TC = line2.split(",");
                Integer UserID = Integer.parseInt(TC[0]);
                Integer Topic = Integer.parseInt(TC[1]);
                Integer Year = Integer.parseInt(TC[2]);
                Integer TopicActivityCount = Integer.parseInt(TC[3]);

                double output1 = 1.0 * TopicActivityCount / NtResults.get(UserID + "," + Year);
                System.out.println(UserID + "," + Topic + "," + Year + "," + output1);
            }
            reader2.close();
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F12 in paper
     */
    private void getUserFeature5() {//topic -1 have been removed
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.UserFeatureDirectory + "UserFeature5.txt"));
            System.setOut(out);

            HashMap<String, Integer> TopicCounts = new HashMap<String, Integer>();
            BufferedReader reader = new BufferedReader(new FileReader(Constants.UserFeatureDirectory + "\\TopicActivityCount.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                //User,Topic,Year,TopicActivityCount
                String[] TC = line.split(",");
                //Integer UserID = Integer.parseInt(TC[0]);
                //Integer Topic = Integer.parseInt(TC[1]);
                //Integer Year = Integer.parseInt(TC[2]);
                //Integer TopicActivityCount = Integer.parseInt(TC[3]);
                if (TopicCounts.containsKey(TC[0] + "," + TC[2])) {
                    TopicCounts.put(TC[0] + "," + TC[2], TopicCounts.get(TC[0] + "," + TC[2]) + 1);
                } else {
                    TopicCounts.put(TC[0] + "," + TC[2], 1);
                }
            }
            reader.close();
            System.out.println("UserId,CurrentYear,UserFeature5");
            for (String UserYear : TopicCounts.keySet()) {
                System.out.println(UserYear + "," + TopicCounts.get(UserYear));
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*private void getUserFeature5() throws IOException {//previous version
        PrintStream stdout = System.out;
        PrintStream out = new PrintStream(new FileOutputStream(".\\Features\\Results\\UserFeature5.txt"));
        System.setOut(out);

        BufferedReader reader = new BufferedReader(new FileReader(".\\Features\\Results\\SampleUsers.txt"));
        String line;
        HashMap<String, Integer> Results = new HashMap<String, Integer>();

        while ((line = reader.readLine()) != null) {
            String[] TC = line.split(",");
            Integer Year = Integer.parseInt(TC[0]);
            Integer UserID = Integer.parseInt(TC[3]);

            Integer output = 0;
            if (Results.containsKey(UserID + "#" + Year)) {
                output = Results.get(UserID + "#" + Year);
            } else {
                Query exQ = u.BooleanQueryAnd(u.SearchOwnerUserId(UserID), u.SearchCreationDate(Year));
                TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs = hits.scoreDocs;

                HashSet<Integer> topics = new HashSet<Integer>();
                for (int i = 0; i < ScDocs.length; ++i) {
                    int docId = ScDocs[i].doc;
                    Document d = searcher.doc(docId);
                    for (IndexableField tag : d.getFields("Topics"))
                        topics.add(Integer.parseInt(tag.stringValue()));
                }
                output = topics.size();
                Results.put(UserID + "#" + Year, output);
            }
            System.out.println(UserID + "," + Year + "," + output);
        }

        reader.close();
        System.setOut(stdout);
    }*/

    /**
     * Feature F13 in paper
     */
    private void getUserFeature9() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.UserFeatureDirectory + "UserFeature9.txt"));
            System.setOut(out);
            System.out.println("UserId,Topic,CurrentYear,UserFeature9");

            BufferedReader reader = new BufferedReader(new FileReader(Constants.TopicGoldenSetDirectory + "TopicExperts.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                //Topic,Year,User,TopicActivityCount
                String[] TC = line.split(",");
                //Integer Topic = Integer.parseInt(TC[0]);
                //Integer Year = Integer.parseInt(TC[1]);
                //Integer UserID = Integer.parseInt(TC[2]);
                //Integer TopicActivityCount = Integer.parseInt(TC[3]);
                System.out.println(TC[2] + "," + TC[0] + "," + TC[1] + "," + TC[3]);
            }
            reader.close();
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F15 in paper
     */
    private void getUserFeature11() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.UserFeatureDirectory + "UserFeature11.txt"));
            System.setOut(out);
            System.out.println("UserId,CurrentYear,UserFeature11");

            for (int cyear = 2008; cyear < 2016; cyear++) {
                HashMap<String, HashSet<String>> User_TagSet = new HashMap<String, HashSet<String>>();
                Query exQ = u.SearchCreationDate(cyear);
                TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs = hits.scoreDocs;
                for (int i = 0; i < ScDocs.length; ++i) {
                    int docId = ScDocs[i].doc;
                    Document d = searcher.doc(docId);
                    String eid = d.get("OwnerUserId");
                    for (IndexableField tag : d.getFields("Tags")) {
                        if (User_TagSet.containsKey(eid)) {
                            User_TagSet.get(eid).add(tag.stringValue());
                        } else {
                            HashSet<String> tags = new HashSet<String>();
                            tags.add(tag.stringValue());
                            User_TagSet.put(eid, tags);
                        }
                    }
                }
                for (String User : User_TagSet.keySet()) {
                    System.out.println(User + "," + cyear + "," + User_TagSet.get(User).size());
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F14 in paper
     * 0 does not printed!!!
     */
    private void getUserFeature21() {
        try {
            HashSet<String> AllTags = new HashSet<String>();
            BufferedReader reader = new BufferedReader(new FileReader(Constants.TagsXMLInput));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("<row")) {
                    Elements row = Jsoup.parse(line).getElementsByTag("row");
                    AllTags.add(row.attr("TagName"));
                }
            }
            reader.close();
            System.out.println("Number of All Tags:" + AllTags.size());//39837

            BufferedReader reader2 = new BufferedReader(new FileReader(Constants.MalletWordTopic));
            String line2;
            HashMap<String, HashSet<Integer>> WordTopics = new HashMap<String, HashSet<Integer>>();
            HashMap<Integer, HashSet<String>> TopicTags = new HashMap<Integer, HashSet<String>>();

            while ((line2 = reader2.readLine()) != null) {
                String[] TC = line2.split(" ");
                if (AllTags.contains(TC[1])) { // If this word is a tag then
                    for (int i = 2; i < TC.length; i++) { // For all topics...
                        String[] TC2 = TC[i].split(":");
                        Integer Topic = Integer.parseInt(TC2[0]);
                        if (WordTopics.containsKey(TC[1])) {
                            WordTopics.get(TC[1]).add(Topic);
                        } else {
                            HashSet<Integer> topics = new HashSet<Integer>();
                            topics.add(Topic);
                            WordTopics.put(TC[1], topics);
                        }

                        if (TopicTags.containsKey(Topic)) {
                            TopicTags.get(Topic).add(TC[1]);
                        } else {
                            HashSet<String> tags = new HashSet<String>();
                            tags.add(TC[1]);
                            TopicTags.put(Topic, tags);
                        }
                    }
                }
            }
            reader2.close();
            AllTags.clear();
            System.out.println("Number of All Tags which associate with at least one topic:" + WordTopics.size());//20284
            /*for (int topic = 0; topic < 50; topic++) {
                System.out.println(topic);
                System.out.println(TopicTags.get(topic).toString());
            }*/

            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.UserFeatureDirectory + "UserFeature21.txt"));
            System.setOut(out);
            System.out.println("UserId,Topic,CurrentYear,UserFeature21");

            for (int cyear = 2008; cyear < 2016; cyear++) {
                HashMap<String, HashSet<String>> User_TagSet = new HashMap<String, HashSet<String>>();
                Query exQ = u.SearchCreationDate(cyear);
                TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs = hits.scoreDocs;
                for (int i = 0; i < ScDocs.length; ++i) {
                    int docId = ScDocs[i].doc;
                    Document d = searcher.doc(docId);
                    String eid = d.get("OwnerUserId");
                    for (IndexableField t : d.getFields("Tags")) {
                        String tag = t.stringValue();
                        if (WordTopics.containsKey(tag)) {
                            if (User_TagSet.containsKey(eid)) {
                                User_TagSet.get(eid).add(tag);
                            } else {
                                HashSet<String> tags = new HashSet<String>();
                                tags.add(tag);
                                User_TagSet.put(eid, tags);
                            }
                        }
                    }
                }
                for (String User : User_TagSet.keySet()) {
                    HashSet<String> user_tags = User_TagSet.get(User);
                    for (int topic = 0; topic < 50; topic++) {
                        HashSet<String> topic_tags = new HashSet<String>(TopicTags.get(topic));
                        topic_tags.retainAll(user_tags);
                        if (topic_tags.size() != 0)
                            System.out.println(User + "," + topic + "," + cyear + "," + topic_tags.size());
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F17 in paper
     */
    private void getUserFeature23() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.UserFeatureDirectory + "UserFeature23.txt"));
            System.setOut(out);

            BufferedReader reader = new BufferedReader(new FileReader(Constants.BadgesXMLInput));
            String line;
            HashMap<Integer, HashMap<Integer, Integer>> BadgeCounts = new HashMap<Integer, HashMap<Integer, Integer>>();
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("<row")) {
                    Elements row = Jsoup.parse(line).getElementsByTag("row");
                    Integer UserID = Integer.parseInt(row.attr("UserId"));
                    //String BadgeName = row.attr("Name");
                    Integer Year = Integer.parseInt(row.attr("Date").substring(0, 4));
                    if (BadgeCounts.containsKey(Year)) {
                        HashMap<Integer, Integer> UserBadgeCounts = BadgeCounts.get(Year);
                        if (UserBadgeCounts.containsKey(UserID))
                            UserBadgeCounts.put(UserID, UserBadgeCounts.get(UserID) + 1);
                        else
                            UserBadgeCounts.put(UserID, 1);
                    } else {
                        HashMap<Integer, Integer> UserBadgeCounts = new HashMap<Integer, Integer>();
                        UserBadgeCounts.put(UserID, 1);
                        BadgeCounts.put(Year, UserBadgeCounts);
                    }
                }
            }
            reader.close();

            System.out.println("UserId,CurrentYear,UserFeature23");
            for (Integer Year : BadgeCounts.keySet()) {
                HashMap<Integer, Integer> UserBadgeCounts = BadgeCounts.get(Year);
                for (Integer UserID : UserBadgeCounts.keySet())
                    System.out.println(UserID + "," + Year + "," + UserBadgeCounts.get(UserID));
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UserFeature f = new UserFeature();
        /*
        f.getTotalActivityCount();
        f.getTopicActivityCount();
        f.getUserFeature1();
        f.getUserFeature5();
        f.getUserFeature9();
        f.getUserFeature11();
        f.getUserFeature21();
        f.getUserFeature23();
        */
    }

    public void startFeatureCalculations() {
        getTopicActivityCount();
        getTotalActivityCount();

        getUserFeature5();//F12
        getUserFeature9();// F13
        getUserFeature21();// F14
        getUserFeature11();// F15
        Conservativeness c = new Conservativeness();
        c.startCalculations("V1");// F16
        getUserFeature23();// F17
        getUserFeature1();// F18
    }
}

    /*
    private void getUserFeature31() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.UserFeatureDirectoty + "UserFeature31.txt"));
            System.setOut(out);

            BufferedReader reader = new BufferedReader(new FileReader(".\\Input\\Comments.xml"));
            String line;
            HashMap<Integer, HashMap<Integer, Integer>> CommentCounts = new HashMap<Integer, HashMap<Integer, Integer>>();
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("<row")) {
                    Elements row = Jsoup.parse(line).getElementsByTag("row");
                    if (!row.attr("UserId").equalsIgnoreCase("")) {
                        Integer UserID = Integer.parseInt(row.attr("UserId"));
                        Integer Year = Integer.parseInt(row.attr("CreationDate").substring(0, 4));

                        if (CommentCounts.containsKey(Year)) {
                            HashMap<Integer, Integer> UserCommentCounts = CommentCounts.get(Year);
                            if (UserCommentCounts.containsKey(UserID))
                                UserCommentCounts.put(UserID, UserCommentCounts.get(UserID) + 1);
                            else
                                UserCommentCounts.put(UserID, 1);
                        } else {
                            HashMap<Integer, Integer> UserCommentCounts = new HashMap<Integer, Integer>();
                            UserCommentCounts.put(UserID, 1);
                            CommentCounts.put(Year, UserCommentCounts);
                        }
                    }
                }
            }
            reader.close();

            System.out.println("UserId,CurrentYear,UserFeature31");
            for (Integer Year : CommentCounts.keySet()) {
                HashMap<Integer, Integer> UserCommentCounts = CommentCounts.get(Year);
                for (Integer UserID : UserCommentCounts.keySet())
                    System.out.println(UserID + "," + Year + "," + UserCommentCounts.get(UserID));
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUserFeature32() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.UserFeatureDirectoty + "UserFeature32.txt"));
            System.setOut(out);
            System.out.println("UserId,CurrentYear,UserFeature32");

            HashMap<Integer, HashMap<Integer, Integer>> UserFeature31Results = new HashMap<Integer, HashMap<Integer, Integer>>();
            BufferedReader reader = new BufferedReader(new FileReader(Constants.UserFeatureDirectoty + "UserFeature31.txt"));
            String line = reader.readLine();//UserId,CurrentYear,UserFeature31
            while ((line = reader.readLine()) != null) {
                String[] TC = line.split(",");
                Integer UserID = Integer.parseInt(TC[0]);
                Integer Year = Integer.parseInt(TC[1]);
                Integer UserFeature31 = Integer.parseInt(TC[2]);
                if (UserFeature31Results.containsKey(Year)) {
                    UserFeature31Results.get(Year).put(UserID, UserFeature31);
                } else {
                    HashMap<Integer, Integer> UserCommentsCounts = new HashMap<Integer, Integer>();
                    UserCommentsCounts.put(UserID, UserFeature31);
                    UserFeature31Results.put(Year, UserCommentsCounts);
                }
            }
            reader.close();

            for (int year = 2008; year < 2015; year++) {
                HashMap<Integer, Integer> UserCommentsCounts = UserFeature31Results.get(year);
                for (Integer User : UserCommentsCounts.keySet()) {
                    Integer UserFeature31 = UserCommentsCounts.get(User);
                    for (int y = 2008; y < year; y++) {
                        if (UserFeature31Results.get(y).containsKey(User)) {
                            UserFeature31 += UserFeature31Results.get(y).get(User);
                        }
                    }
                    System.out.println(User + "," + year + "," + UserFeature31);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
