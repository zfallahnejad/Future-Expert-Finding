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

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Asus on 1/28/2018.
 */
public class TPBM {
    HashMap<Integer, Double> P_at_e;// key= topics from currentYearTopics  ,  values = Probability of having documnets written by eid with specified topics in year t
    HashMap<String, HashMap<Integer, Double>> All_Pate;// key = "User,Year" , value=( key=Topic , value=Probability of having documnets written by eid with specified topics in year t )
    HashMap<String, HashMap<Integer, Double>> AllPWAs;// key = Tag , value=(key=Topic , value= P(W|T) )
    HashMap<Integer, Double> PWA;// key = Topic , value= P(W|T)
    HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> TopicUserActivity;// key = Year , value=(key=Topic , value=( key=user  value= Count )  )
    HashSet<Integer> CandidateUsers;
    Set<Integer> futureYearTopics;
    IndexUtility u;
    IndexReader reader;
    IndexSearcher searcher;
    Analyzer analyzer;

    double beta = 0.5;
    double[][] Similarity = new double[50][50];
    HashMap<String, Double> Popularity;
    HashMap<String, Double> Conservativeness;

    public TPBM() {
        try {
            P_at_e = new HashMap<Integer, Double>();
            All_Pate = new HashMap<String, HashMap<Integer, Double>>();
            AllPWAs = new HashMap<String, HashMap<Integer, Double>>();
            TopicUserActivity = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
            CandidateUsers = new HashSet<Integer>();
            u = new IndexUtility();
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(Constants.IndexDirectory)));
            searcher = new IndexSearcher(reader);
            analyzer = new EnglishAnalyzer();
            Conservativeness = new HashMap<String, Double>();
            Popularity = new HashMap<String, Double>();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPateFile() {//Answer Version
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.Features_Directory + "p_A_T_.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] TC = line.split("\t");
                //13	2008	44	0.033333333333
                if (All_Pate.containsKey(TC[0] + "," + TC[1])) {
                    All_Pate.get(TC[0] + "," + TC[1]).put(Integer.parseInt(TC[2]), Double.parseDouble(TC[3]));
                } else {
                    HashMap<Integer, Double> val = new HashMap<Integer, Double>();
                    val.put(Integer.parseInt(TC[2]), Double.parseDouble(TC[3]));
                    All_Pate.put(TC[0] + "," + TC[1], val);
                }
            }
            reader.close();
            //System.out.println(All_Pate.get("13,2008").get(44));//13	2008	44	0.033333333333
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPateFile(String P_at_e_Version) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.Features_Directory + "Pate_" + P_at_e_Version + ".txt"));
            String line = reader.readLine();//OwnerUserId,CurrentYear,Topic,P_at_e
            while ((line = reader.readLine()) != null) {
                String[] TC = line.split(",");
                if (All_Pate.containsKey(TC[0] + "," + TC[1])) {
                    All_Pate.get(TC[0] + "," + TC[1]).put(Integer.parseInt(TC[2]), Double.parseDouble(TC[3]));
                } else {
                    HashMap<Integer, Double> val = new HashMap<Integer, Double>();
                    val.put(Integer.parseInt(TC[2]), Double.parseDouble(TC[3]));
                    All_Pate.put(TC[0] + "," + TC[1], val);
                }
            }
            reader.close();
            //System.out.println(All_Pate.get("13,2008").get(44));//13	2008	44	0.033333333333
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPWAFromFile() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.Features_Directory + "PWA.txt"));
            String line = reader.readLine();//Word,Topic,PWA
            while ((line = reader.readLine()) != null) {
                String[] TC = line.split(",");
                if (AllPWAs.containsKey(TC[0]))
                    AllPWAs.get(TC[0]).put(Integer.parseInt(TC[1]), Double.parseDouble(TC[2]));
                else {
                    HashMap<Integer, Double> val = new HashMap<Integer, Double>();
                    val.put(Integer.parseInt(TC[1]), Double.parseDouble(TC[2]));
                    AllPWAs.put(TC[0], val);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTopicUserActivity(String TopicUserActivityVersion) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.Features_Directory + "TopicUserActivity_" + TopicUserActivityVersion + ".txt"));
            reader.readLine();
            String line;//expert	year	topic	sum
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts[0].equalsIgnoreCase("NULL"))
                    continue;
                Integer Expert = Integer.parseInt(parts[0]);
                Integer Year = Integer.parseInt(parts[1]);
                Integer Topic = Integer.parseInt(parts[2]);
                Integer Count = Integer.parseInt(parts[3]);
                if (TopicUserActivity.containsKey(Year)) {
                    HashMap<Integer, HashMap<Integer, Integer>> topic_userActivities = TopicUserActivity.get(Year);
                    if (topic_userActivities.containsKey(Topic)) {
                        HashMap<Integer, Integer> userActivities = topic_userActivities.get(Topic);
                        userActivities.put(Expert, Count);
                    } else {
                        HashMap<Integer, Integer> userActivities = new HashMap<Integer, Integer>();
                        userActivities.put(Expert, Count);
                        topic_userActivities.put(Topic, userActivities);
                    }
                } else {
                    HashMap<Integer, Integer> userActivities = new HashMap<Integer, Integer>();
                    userActivities.put(Expert, Count);
                    HashMap<Integer, HashMap<Integer, Integer>> topic_userActivities = new HashMap<Integer, HashMap<Integer, Integer>>();
                    topic_userActivities.put(Topic, userActivities);
                    TopicUserActivity.put(Year, topic_userActivities);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSimilarity() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.MalletTopicSimilarity));
            String line;
            int topicNum = 0;
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split("\t");
                for (int i = 0; i < temp.length; i++)
                    Similarity[topicNum][i] = Double.parseDouble(temp[i]);
                topicNum++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConservativeness(String TopicUserActivityVersion) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.Features_Directory + "Conservativeness_" + TopicUserActivityVersion + ".txt"));
            String line = reader.readLine();//OwnerUserId,CurrentYear,Conservativeness
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split(",");
                Conservativeness.put(temp[0] + "," + temp[1], Double.parseDouble(temp[2]));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPopularity(String PopularityVersion) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.Features_Directory + "Popularity" + PopularityVersion + ".txt"));
            String line = reader.readLine();//System.out.println("Topic,CurrentYear,Popularity");
            while ((line = reader.readLine()) != null) {
                String[] TC = line.split(",");
                Popularity.put(TC[0] + "," + TC[1], Double.parseDouble(TC[2]));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void selectCandidateUsers(int futureYear) {
        for (Integer topic = 0; topic < 50; topic++) {
            if (TopicUserActivity.get(futureYear - 1).containsKey(topic)) {
                HashMap<Integer, Integer> userActivities = TopicUserActivity.get(futureYear - 1).get(topic);
                ArrayList<UserActivity> list1 = new ArrayList<UserActivity>();
                for (Integer e : userActivities.keySet()) {
                    UserActivity s = new UserActivity(e, userActivities.get(e));
                    list1.add(s);
                }
                Collections.sort(list1);
                int len1 = (2000 > list1.size()) ? list1.size() : 2000;
                for (int i = 0; i < len1; i++) {
                    CandidateUsers.add(list1.get(i).Expert);
                }
            }
        }
    }

    public void startFangBaselineCalculations(String TopicUserActivityVersion, String PopularityVersion, String P_at_e_Version) {
        loadPateFile();// Better
        //loadPateFile(P_at_e_Version);
        loadPWAFromFile();
        loadTopicUserActivity(TopicUserActivityVersion);
        loadSimilarity();
        loadConservativeness(TopicUserActivityVersion);
        loadPopularity(PopularityVersion);

        for (String tag : Constants.TopTags) {
            PWA = AllPWAs.get(tag);
            futureYearTopics = PWA.keySet();

            for (int futureYear = 2009; futureYear < 2016; futureYear++) {
                selectCandidateUsers(futureYear);

                getExpertiseProbability(tag, futureYear);

                CandidateUsers.clear();
            }
        }
    }

    private void getExpertiseProbability(String tag, int futureYear) {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TPBM_Directory + "TPBM_" + tag + "_FYear_" + futureYear + ".txt"));
            System.setOut(out);

            for (Integer eid : CandidateUsers) {
                if (All_Pate.containsKey(eid + "," + (futureYear - 1))) {
                    HashMap<Integer, Double> topics = All_Pate.get(eid + "," + (futureYear - 1));
                    for (Integer topic : topics.keySet()) {
                        P_at_e.put(topic, topics.get(topic));
                    }
                    //P_at_e = All_Pate.get(eid + "," + (futureYear - 1));

                    double result = getWordProbabilityByExpertAndYear(tag, eid, futureYear);

                    if (result != 0.0)
                        System.out.println(eid + "," + tag + "," + futureYear + "," + result);

                    P_at_e.clear();
                }
            }

            System.setOut(stdout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Start from here
    public double getWordProbabilityByExpertAndYear(String word, Integer eid, int futureYear) {
        double output = 0;
        for (Integer FutureTopic : futureYearTopics) {
            double P_W_A = PWA.get(FutureTopic);
            double P_A_E = getFutureTagProbabilityByExpertAndYear(FutureTopic, eid, futureYear);
            output += P_W_A * P_A_E;
        }
        return output;
    }

    private double getFutureTagProbabilityByExpertAndYear(Integer futureTopic, Integer eid, int futureYear) {
        double output = 0;
        for (Integer currentTopic : P_at_e.keySet()) {
            output += TransitionProbability(currentTopic, futureTopic, eid, futureYear)
                    * P_at_e.get(currentTopic);
        }
        return output;
    }

    private double TransitionProbability(Integer currentTopic, Integer futureTopic, Integer eid, int futureYear) {
        double oldTagProbability = getProbabilityChoosingTagFromCurrentYearTags(currentTopic, futureTopic, eid, futureYear);
        double newTagProbability = getProbabilityChoosingNewTag(currentTopic, futureTopic, eid, futureYear);
        double output = (getConservativenessProbability(eid, futureYear) * oldTagProbability) +
                ((1 - getConservativenessProbability(eid, futureYear)) * newTagProbability);
        return output;
    }

    private double getProbabilityChoosingTagFromCurrentYearTags(Integer currentTopic, Integer futureTopic, Integer eid, int futureYear) {
        return (P_at_e.containsKey(futureTopic) ? P_at_e.get(futureTopic) : 0);
    }

    private double getProbabilityChoosingNewTag(Integer currentTopic, Integer futureTopic, Integer eid, int futureYear) {
        return ((beta * tagSimilarity(currentTopic, futureTopic, futureYear)) + ((1 - beta) * tagPopularity(futureTopic, futureYear - 1)));
    }

    private double tagSimilarity(Integer currentTopic, Integer futureTopic, int futureYear) {
        return Similarity[futureTopic][currentTopic];
    }

    private double tagPopularity(Integer futureTopic, int currentYear) {
        if (Popularity.containsKey(futureTopic + "," + currentYear))
            return Popularity.get(futureTopic + "," + currentYear);
        else {
            Integer N_t = u.getDocCount(u.SearchCreationDate(currentYear));
            Integer N_at1_t = u.getDocCount(
                    u.BooleanQueryAnd(
                            u.SearchCreationDate(currentYear), u.SearchTopic(futureTopic)));
            double output = (1.0 * N_at1_t) / N_t;
            return output;
        }
    }

    private double getConservativenessProbability(Integer eid, int futureYear) {
        if (Conservativeness.containsKey(eid + "," + (futureYear - 1)))
            return Conservativeness.get(eid + "," + (futureYear - 1));
        else {
            double output = 0;
            ArrayList<Integer> activityYears = u.getActivityYearsByExpertID(eid, futureYear);

            HashMap<Integer, HashSet<Integer>> TopicMap = new HashMap<Integer, HashSet<Integer>>();
            for (int i = 0; i < activityYears.size(); i++) {
                int year = activityYears.get(i);
                HashSet<Integer> A = getTopicsByAuthorAndYear(year, eid);
                TopicMap.put(year, A);
            }

            int count = 0;
            for (int i = 1; i < activityYears.size(); i++) {
                output += getConservativenessProbabilityByYear(eid, activityYears.get(i), activityYears.get(i - 1), TopicMap);
                count++;
            }
            output = (count == 0 ? 0.5 : output / count);
            return output;
        }
    }

    HashSet<Integer> getTopicsByAuthorAndYear(int year, Integer eid) {
        HashSet<Integer> Topics = new HashSet<Integer>();
        try {
            Query Q_Author_Year = u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchOwnerUserId(eid));
            TopDocs hits = searcher.search(Q_Author_Year, Integer.MAX_VALUE);
            ScoreDoc[] ScDocs = hits.scoreDocs;
            for (int i = 0; i < ScDocs.length; ++i) {
                int docId = ScDocs[i].doc;
                Document d = searcher.doc(docId);
                //System.out.println("Id: "+d.get("Id"));
                for (IndexableField t : d.getFields("Topics")) {
                    Integer topic = Integer.parseInt(t.stringValue());
                    if (topic != -1) {
                        Topics.add(topic);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Topics;
    }

    private double getConservativenessProbabilityByYear(Integer eid, int year, int lastYear, HashMap<Integer, HashSet<Integer>> TopicMap) {
        double output;

        HashSet<Integer> A_t = TopicMap.get(year);
        HashSet<Integer> A_t_1 = TopicMap.get(lastYear);

        HashSet<Integer> IntersectionSet = new HashSet<>();
        IntersectionSet.addAll(A_t);
        IntersectionSet.retainAll(A_t_1);

        A_t_1.addAll(A_t);
        output = (A_t_1.size() == 0 ? 0 : (IntersectionSet.size() * 1.0) / A_t_1.size());
        return output;
    }

    /*
    private double getFutureTopicProbabilityByExpertAndCurrentTopic
            (Integer futureTopic, Integer currentTopic, int eid, int currentYear) {
        double cons = Conservativeness.get(eid);
        double newTagProbability = getProbabilityChoosingNewTag(futureTopic, currentTopic, eid, currentYear);
        double output;
        if (cons == 0.0) {
            output = newTagProbability;
        } else {
            double oldTagProbability = getProbabilityChoosingTagFromCurrentYearTags(futureTopic, currentTopic, eid, currentYear);
            output = ((cons * oldTagProbability) + ((1 - cons) * newTagProbability));
        }
        return output;
    }*/

    public static void main(String[] args) {
        TPBM b = new TPBM();
        b.startFangBaselineCalculations("V1", "V1", "Answer");
    }

    class UserActivity implements Comparable<UserActivity> {
        Integer Expert;
        Integer Count;

        public UserActivity(Integer Expert, Integer Count) {
            this.Expert = Expert;
            this.Count = Count;
        }

        @Override
        public int compareTo(UserActivity o) {
            return -1 * Integer.compare(Count, o.Count);
        }
    }
}
