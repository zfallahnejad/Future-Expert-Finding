package TBM;

import Utility.Constants;

import java.io.*;
import java.util.*;

/**
 * Created by Zohreh on 5/19/2018.
 */
public class TBM {
    HashMap<String, HashMap<Integer, Double>> All_Pate;// key = "User,Year" , value=( key=Topic , value=Probability of having documnets written by eid with specified topics in year t )
    HashMap<String, HashMap<Integer, Double>> AllPWAs;// key = Tag , value=(key=Topic , value= P(W|T) )
    HashMap<Integer, Double> PWA;// key = Topic , value= P(W|T)
    HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> TopicUserActivity;// key = Year , value=(key=Topic , value=( key=user  value= Count )  )
    HashSet<Integer> CandidateUsers;
    HashMap<String, Double> P_User_Topic;
    Set<Integer> futureYearTopics;

    public TBM() {
        All_Pate = new HashMap<String, HashMap<Integer, Double>>();
        AllPWAs = new HashMap<String, HashMap<Integer, Double>>();
        TopicUserActivity = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
        CandidateUsers = new HashSet<Integer>();
        P_User_Topic = new HashMap<String, Double>();
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

    private void load_P_User_Topic_File() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.Features_Directory + "P_User_Topic.txt"));
            String line = reader.readLine();//User,Year,Topic,P(e|m)
            while ((line = reader.readLine()) != null) {
                String[] TC = line.split(",");
                P_User_Topic.put(TC[0] + "," + TC[1] + "," + TC[2], Double.parseDouble(TC[3]));
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

    public void startFangBaselineCalculations(String TopicUserActivityVersion, String P_at_e_Version) {
        loadPateFile();// Better
        //loadPateFile(P_at_e_Version);
        loadPWAFromFile();//load all of the P(q|m) probability for TBM Basleine
        loadTopicUserActivity(TopicUserActivityVersion);
        load_P_User_Topic_File();

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
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TBM_Directory + "TBM_" + tag + "_FYear_" + futureYear + ".txt"));
            System.setOut(out);

            for (Integer eid : CandidateUsers) {
                double result = getWordProbabilityByExpertAndYear(tag, eid, futureYear);

                if (result != 0.0)
                    System.out.println(eid + "," + tag + "," + futureYear + "," + result);
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
            double P_q_m = PWA.get(FutureTopic);//i.e. P(q|m) in TBM Baseline
            double P_e_m = P_User_Topic.containsKey(eid + "," + (futureYear - 1) + "," + FutureTopic) ? P_User_Topic.get(eid + "," + (futureYear - 1) + "," + FutureTopic) : 0.0;//i.e. P(e|m) in TBM Baseline
            output += P_q_m * P_e_m;
        }
        return output;
    }

    public static void main(String[] args) {
        TBM t = new TBM();
        t.startFangBaselineCalculations("V1", "Answer");
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
