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
 * Created by Zohreh on 7/8/2017.
 */
public class Conservativeness {
    HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> TopicUserActivity;// key = Year , value=(key=Topic , value=( key=user  value= Count )  )
    HashSet<Integer> CandidateUsers;
    IndexUtility u;
    IndexReader reader;
    IndexSearcher searcher;
    Analyzer analyzer;

    public Conservativeness() {
        try {
            TopicUserActivity = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
            CandidateUsers = new HashSet<Integer>();
            u = new IndexUtility();
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(Constants.IndexDirectory)));
            searcher = new IndexSearcher(reader);
            analyzer = new EnglishAnalyzer();
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

    /**
     * Select top 2000 user of each topic for current year
     *
     * @param futureYear
     */
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

    public void startCalculations(String TopicUserActivityVersion) {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.Features_Directory + "Conservativeness_" + TopicUserActivityVersion + ".txt"));
            System.setOut(out);

            System.out.println("OwnerUserId,CurrentYear,Conservativeness");

            loadTopicUserActivity(TopicUserActivityVersion);

            for (int futureYear = 2009; futureYear < 2016; futureYear++) {
                selectCandidateUsers(futureYear);

                for (Integer eid : CandidateUsers) {
                    double conservativeness = getConservativenessProbability(eid, futureYear);
                    System.out.println(eid + "," + (futureYear - 1) + "," + conservativeness);
                }

                CandidateUsers.clear();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double getConservativenessProbability(Integer eid, int futureYear) {
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

    public static void main(String[] args) {
        Conservativeness b = new Conservativeness();
        b.startCalculations("V2");
    }
}
