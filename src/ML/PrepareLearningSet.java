package ML;

import Utility.Constants;

import java.io.*;
import java.text.Normalizer;
import java.util.*;

/**
 * Created by Zohreh on 5/17/2018.
 */
public class PrepareLearningSet {
    HashMap<String, String> FeatureMap;

    public PrepareLearningSet() {
        FeatureMap = new HashMap<String, String>();
    }

    private void getSampleUsers(String FileDirectory) {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(FileDirectory));
            System.setOut(out);

            HashMap<Integer, HashMap<Integer, HashSet<Integer>>> ExpertiseTopicsOfEachUserInYear = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();
            HashMap<Integer, HashMap<Integer, HashSet<Integer>>> TopicsOfEachUserInYear = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();
            BufferedReader reader = new BufferedReader(new FileReader(Constants.TopicGoldenSetDirectory + "TopicExperts.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] TC = line.split(",");
                Integer Topic = Integer.parseInt(TC[0]);
                Integer Year = Integer.parseInt(TC[1]);
                Integer UserID = Integer.parseInt(TC[2]);
                Integer Count = Integer.parseInt(TC[3]);
                if (TopicsOfEachUserInYear.containsKey(Year)) {
                    HashMap<Integer, HashSet<Integer>> expertTopics = TopicsOfEachUserInYear.get(Year);
                    if (expertTopics.containsKey(UserID)) {
                        expertTopics.get(UserID).add(Topic);
                    } else {
                        HashSet<Integer> topics = new HashSet<Integer>();
                        topics.add(Topic);
                        expertTopics.put(UserID, topics);
                    }
                } else {
                    HashSet<Integer> topics = new HashSet<Integer>();
                    topics.add(Topic);
                    HashMap<Integer, HashSet<Integer>> userTopics = new HashMap<Integer, HashSet<Integer>>();
                    userTopics.put(UserID, topics);
                    TopicsOfEachUserInYear.put(Year, userTopics);
                }

                if (Count >= 3) {
                    if (ExpertiseTopicsOfEachUserInYear.containsKey(Year)) {
                        HashMap<Integer, HashSet<Integer>> expertTopics = ExpertiseTopicsOfEachUserInYear.get(Year);
                        if (expertTopics.containsKey(UserID)) {
                            expertTopics.get(UserID).add(Topic);
                        } else {
                            HashSet<Integer> topics = new HashSet<Integer>();
                            topics.add(Topic);
                            expertTopics.put(UserID, topics);
                        }
                    } else {
                        HashSet<Integer> topics = new HashSet<Integer>();
                        topics.add(Topic);
                        HashMap<Integer, HashSet<Integer>> userTopics = new HashMap<Integer, HashSet<Integer>>();
                        userTopics.put(UserID, topics);
                        ExpertiseTopicsOfEachUserInYear.put(Year, userTopics);
                    }
                }
            }
            reader.close();

            System.out.println("Year,Topic1,Topic2,OwnerUserId,Label");
            //Label = 1
            for (int year = 2008; year < 2015; year++) {
                Set<Integer> CurrentUsers = TopicsOfEachUserInYear.get(year).keySet();
                Set<Integer> FutureExpertUsers = ExpertiseTopicsOfEachUserInYear.get(year + 1).keySet();

                //which one of the users have activity in current year and become expert in future year?
                HashSet<Integer> IntersectionSetOfUsers = new HashSet<Integer>();
                IntersectionSetOfUsers.addAll(CurrentUsers);
                IntersectionSetOfUsers.retainAll(FutureExpertUsers);

                for (Integer user : IntersectionSetOfUsers) {
                    HashSet<Integer> CurrentTopics = TopicsOfEachUserInYear.get(year).get(user);//Current topical activity
                    HashSet<Integer> FutureExpertsTopics = ExpertiseTopicsOfEachUserInYear.get(year + 1).get(user);//Future Expertise Topics
                    HashSet<String> Tuples = new HashSet<String>();
                    for (Integer CT : CurrentTopics) {
                        for (Integer FT : FutureExpertsTopics) {
                            Tuples.add(CT + "," + FT);
                        }
                    }
                    List<String> TuplesList = new ArrayList<String>(Tuples);
                    Collections.shuffle(TuplesList, new Random(10));
                    Integer len = (TuplesList.size() > 35) ? 35 : TuplesList.size();
                    for (String tuple : TuplesList.subList(0, len)) {
                        System.out.println(year + "," + tuple + "," + user + ",1");
                    }
                }
            }

            //Label = 0
            for (int year = 2008; year < 2015; year++) {
                Set<Integer> CurrentUsers = TopicsOfEachUserInYear.get(year).keySet();
                Set<Integer> FutureExpertUsers = ExpertiseTopicsOfEachUserInYear.get(year + 1).keySet();

                //which one of the users have activity in current year and become expert in future year?
                HashSet<Integer> IntersectionSetOfUsers = new HashSet<Integer>();
                IntersectionSetOfUsers.addAll(CurrentUsers);
                IntersectionSetOfUsers.retainAll(FutureExpertUsers);

                for (Integer user : IntersectionSetOfUsers) {
                    HashSet<Integer> CurrentTopics = TopicsOfEachUserInYear.get(year).get(user);//Current topical activity
                    HashSet<Integer> FutureTopics = ExpertiseTopicsOfEachUserInYear.get(year + 1).get(user);//Future Expertise Topics
                    HashSet<Integer> FutureTopicsComplement = new HashSet<Integer>();//Complement Future Expertise Topics
                    for (int i = 0; i < 50; i++)
                        if (!FutureTopics.contains(i))
                            FutureTopicsComplement.add(i);
                    HashSet<String> Tuples = new HashSet<String>();
                    for (Integer CT : CurrentTopics) {
                        for (Integer FT : FutureTopicsComplement) {
                            Tuples.add(CT + "," + FT);
                        }
                    }
                    List<String> TuplesList = new ArrayList<String>(Tuples);
                    Collections.shuffle(TuplesList, new Random(10));
                    Integer len = (TuplesList.size() > 4) ? 4 : TuplesList.size();
                    for (String tuple : TuplesList.subList(0, len)) {
                        System.out.println(year + "," + tuple + "," + user + ",0");
                    }
                }
            }

            for (int year = 2008; year < 2015; year++) {
                Set<Integer> CurrentUsers = TopicsOfEachUserInYear.get(year).keySet();
                Set<Integer> FutureExpertUsers = ExpertiseTopicsOfEachUserInYear.get(year + 1).keySet();
                //which one of the users have activity in current year and never become expert in future year?
                CurrentUsers.removeAll(FutureExpertUsers);

                for (Integer user : CurrentUsers) {
                    HashSet<Integer> CurrentTopics = TopicsOfEachUserInYear.get(year).get(user);//Current topical activity
                    HashSet<Integer> FutureTopics = TopicsOfEachUserInYear.get(year + 1).get(user);//Future topical activity
                    if (FutureTopics != null) {
                        HashSet<String> Tuples = new HashSet<String>();
                        for (Integer CT : CurrentTopics) {
                            for (Integer FT : FutureTopics) {
                                Tuples.add(CT + "," + FT);
                            }
                        }
                        List<String> TuplesList = new ArrayList<String>(Tuples);
                        Collections.shuffle(TuplesList, new Random(10));
                        Integer len = (TuplesList.size() > 4) ? 4 : TuplesList.size();
                        for (String tuple : TuplesList.subList(0, len)) {
                            System.out.println(year + "," + tuple + "," + user + ",0");
                        }
                    }
                }
            }
            System.setOut(stdout);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFeatureFile(String featureDirectoty, String featureFileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(featureDirectoty + featureFileName + ".txt"));
            String line = reader.readLine();
            String FirstLine = line.substring(0, line.lastIndexOf(","));
            while ((line = reader.readLine()) != null) {
                String line1 = line.replace("\n", "");
                String key = line1.substring(0, line1.lastIndexOf(","));
                String value = line1.substring(line1.lastIndexOf(",") + 1, line1.length());
                FeatureMap.put(key, value);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ExtractFeatures(String FileDirectory, String FileName, String outputFileName) {
        try {
            // Load sample file
            ArrayList<UserTuple> Samples = new ArrayList<UserTuple>();
            BufferedReader reader = new BufferedReader(new FileReader(FileDirectory + FileName));
            String line = reader.readLine();
            // Year,Topic1,Topic2,OwnerUserId,Label
            while ((line = reader.readLine()) != null) {
                String[] TC = line.split(",");
                UserTuple ut = new UserTuple(TC[0], TC[1], TC[2], TC[3], TC[4]);
                Samples.add(ut);
            }
            reader.close();

            String[] FeatureNames = new String[]{
                    "UserFeature1", "UserFeature5", "UserFeature9", "UserFeature11", "UserFeature21", "UserFeature23", "Conservativeness_V1",
                    "TopicTransition1", "TopicTransition2", "TopicTransition3_1", "TopicTransition3_3", "TopicTransition3_10", "SumTopicTransition1",
                    "TopicSimilarity1", "TopicSimilarity7",
                    "TopicFeature1", "TopicFeature8", "TopicFeature11", "TopicFeature12", "TopicFeature13", "TopicFeature16", "TopicFeature19", "TopicFeature20", "TopicFeature21"
            };
            // Extract feature values
            for (String fname : FeatureNames) {
                switch (fname) {
                    //F1
                    case "TopicSimilarity1"://Topic1,Topic2,Year,TopicSimilarity1
                        loadFeatureFile(Constants.TopicSimilarityDirectory, "TopicSimilarity1");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year))
                                ut.TopicSimilarity1 = FeatureMap.get(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicSimilarity1 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F2
                    case "TopicSimilarity7"://Topic1,Topic2,Year,TopicSimilarity7
                        loadFeatureFile(Constants.TopicSimilarityDirectory, "TopicSimilarity7");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year))
                                ut.TopicSimilarity7 = FeatureMap.get(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicSimilarity7 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F3
                    case "TopicFeature8"://Topic1,Topic2,Year,TopicFeature8
                        loadFeatureFile(Constants.TopicFeatureDirectory, "TopicFeature8");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year))
                                ut.TopicFeature8 = FeatureMap.get(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicFeature8 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F4
                    case "TopicFeature11"://Topic,Year,TopicFeature11
                        loadFeatureFile(Constants.TopicFeatureDirectory, "TopicFeature11");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic2 + "," + ut.Year))
                                ut.TopicFeature11 = FeatureMap.get(ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicFeature11 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F5
                    case "TopicFeature12"://Topic1,Topic2,Year,TopicFeature12
                        loadFeatureFile(Constants.TopicFeatureDirectory, "TopicFeature12");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year))
                                ut.TopicFeature12 = FeatureMap.get(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicFeature12 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F6
                    case "TopicFeature13"://Topic,Year,TopicFeature13
                        loadFeatureFile(Constants.TopicFeatureDirectory, "TopicFeature13");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic2 + "," + ut.Year))
                                ut.TopicFeature13 = FeatureMap.get(ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicFeature13 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F7
                    case "TopicFeature16"://Topic1,Topic2,Year,TopicFeature16
                        loadFeatureFile(Constants.TopicFeatureDirectory, "TopicFeature16");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year))
                                ut.TopicFeature16 = FeatureMap.get(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicFeature16 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F8
                    case "TopicFeature19"://Topic,Year,TopicFeature19
                        loadFeatureFile(Constants.TopicFeatureDirectory, "TopicFeature19");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic2 + "," + ut.Year))
                                ut.TopicFeature19 = FeatureMap.get(ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicFeature19 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F9
                    case "TopicFeature20"://Topic1,Topic2,Year,TopicFeature20
                        loadFeatureFile(Constants.TopicFeatureDirectory, "TopicFeature20");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year))
                                ut.TopicFeature20 = FeatureMap.get(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicFeature20 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F10
                    case "TopicFeature1"://Topic,Year,TopicFeature1
                        loadFeatureFile(Constants.TopicFeatureDirectory, "TopicFeature1");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic2 + "," + ut.Year))
                                ut.TopicFeature1 = FeatureMap.get(ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicFeature1 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F11
                    case "TopicFeature21"://Topic,Year,TopicFeature21
                        loadFeatureFile(Constants.TopicFeatureDirectory, "TopicFeature21");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic2 + "," + ut.Year))
                                ut.TopicFeature21 = FeatureMap.get(ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicFeature21 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    // F12
                    case "UserFeature5"://UserId,CurrentYear,UserFeature5
                        loadFeatureFile(Constants.UserFeatureDirectory, "UserFeature5");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.UserID + "," + ut.Year))
                                ut.UserFeature5 = FeatureMap.get(ut.UserID + "," + ut.Year);
                            else
                                ut.UserFeature5 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F13
                    case "UserFeature9"://UserId,Topic,CurrentYear,UserFeature9
                        loadFeatureFile(Constants.UserFeatureDirectory, "UserFeature9");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.UserID + "," + ut.Topic2 + "," + ut.Year))
                                ut.UserFeature9 = FeatureMap.get(ut.UserID + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.UserFeature9 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F14
                    case "UserFeature21"://UserId,Topic,CurrentYear,UserFeature21
                        loadFeatureFile(Constants.UserFeatureDirectory, "UserFeature21");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.UserID + "," + ut.Topic2 + "," + ut.Year))
                                ut.UserFeature21 = FeatureMap.get(ut.UserID + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.UserFeature21 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //15
                    case "UserFeature11"://UserId,CurrentYear,UserFeature11
                        loadFeatureFile(Constants.UserFeatureDirectory, "UserFeature11");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.UserID + "," + ut.Year))
                                ut.UserFeature11 = FeatureMap.get(ut.UserID + "," + ut.Year);
                            else
                                ut.UserFeature11 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F16
                    case "Conservativeness_V1"://OwnerUserId,CurrentYear,Conservativeness
                        loadFeatureFile(Constants.Features_Directory, "Conservativeness_V1");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.UserID + "," + ut.Year))
                                ut.Conservativeness_V1 = FeatureMap.get(ut.UserID + "," + ut.Year);
                            else
                                ut.Conservativeness_V1 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F17
                    case "UserFeature23"://UserId,CurrentYear,UserFeature23
                        loadFeatureFile(Constants.UserFeatureDirectory, "UserFeature23");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.UserID + "," + ut.Year))
                                ut.UserFeature23 = FeatureMap.get(ut.UserID + "," + ut.Year);
                            else
                                ut.UserFeature23 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    // F18
                    case "UserFeature1"://UserId,Topic,CurrentYear,UserFeature1
                        loadFeatureFile(Constants.UserFeatureDirectory, "UserFeature1");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.UserID + "," + ut.Topic2 + "," + ut.Year))
                                ut.UserFeature1 = FeatureMap.get(ut.UserID + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.UserFeature1 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F19
                    case "TopicTransition3_1"://Topic1,Topic2,Year,TopicTransition3_1
                        loadFeatureFile(Constants.TopicTransitionDirectory, "TopicTransition3_1");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year))
                                ut.TopicTransition3_1 = FeatureMap.get(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicTransition3_1 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F20
                    case "TopicTransition3_3"://Topic1,Topic2,Year,TopicTransition3_3
                        loadFeatureFile(Constants.TopicTransitionDirectory, "TopicTransition3_3");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year))
                                ut.TopicTransition3_3 = FeatureMap.get(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicTransition3_3 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F21
                    case "TopicTransition3_10"://Topic1,Topic2,Year,TopicTransition3_10
                        loadFeatureFile(Constants.TopicTransitionDirectory, "TopicTransition3_10");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year))
                                ut.TopicTransition3_10 = FeatureMap.get(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicTransition3_10 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F22
                    case "TopicTransition1"://Topic1,Topic2,Year,TopicTransition1
                        loadFeatureFile(Constants.TopicTransitionDirectory, "TopicTransition1");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year))
                                ut.TopicTransition1 = FeatureMap.get(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicTransition1 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F23
                    case "SumTopicTransition1"://Topic1,Topic2,Year,SumTopicTransition1
                        loadFeatureFile(Constants.TopicTransitionDirectory, "SumTopicTransition1");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year))
                                ut.SumTopicTransition1 = FeatureMap.get(ut.Topic1 + "," + ut.Topic2 + "," + ut.Year);
                            else
                                ut.SumTopicTransition1 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                    //F24
                    case "TopicTransition2"://Topic2,Year,TopicTransition2
                        loadFeatureFile(Constants.TopicTransitionDirectory, "TopicTransition2");
                        for (UserTuple ut : Samples) {
                            if (FeatureMap.containsKey(ut.Topic2 + "," + ut.Year))
                                ut.TopicTransition2 = FeatureMap.get(ut.Topic2 + "," + ut.Year);
                            else
                                ut.TopicTransition2 = "0.0";
                        }
                        FeatureMap.clear();
                        break;
                }
            }

            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(FileDirectory + outputFileName));
            System.setOut(out);

            System.out.println("Year,Topic1,Topic2,OwnerUserId,Label," +
                            "UserFeature1,UserFeature5,UserFeature9,UserFeature11,UserFeature21,UserFeature23,Conservativeness_V1," +
                            "TopicTransition1,TopicTransition2,TopicTransition3_1,TopicTransition3_3,TopicTransition3_10,SumTopicTransition1," +
                            "TopicSimilarity1,TopicSimilarity7," +
                            "TopicFeature1,TopicFeature8,TopicFeature11,TopicFeature12,TopicFeature13,TopicFeature16,TopicFeature19,TopicFeature20,TopicFeature21"
            );
            for (UserTuple ut : Samples) {
                System.out.println(
                        ut.Year + "," + ut.Topic1 + "," + ut.Topic2 + "," + ut.UserID + "," + ut.Label + "," +
                                ut.UserFeature1 + "," + ut.UserFeature5 + "," + ut.UserFeature9 + "," + ut.UserFeature11 + "," + ut.UserFeature21 + "," + ut.UserFeature23 + "," + ut.Conservativeness_V1 + "," +
                                ut.TopicTransition1 + "," + ut.TopicTransition2 + "," + ut.TopicTransition3_1 + "," + ut.TopicTransition3_3 + "," + ut.TopicTransition3_10 + "," + ut.SumTopicTransition1 + "," +
                                ut.TopicSimilarity1 + "," + ut.TopicSimilarity7 + "," +
                                ut.TopicFeature1 + "," + ut.TopicFeature8 + "," + ut.TopicFeature11 + "," + ut.TopicFeature12 + "," + ut.TopicFeature13 + "," + ut.TopicFeature16 + "," + ut.TopicFeature19 + "," + ut.TopicFeature20 + "," + ut.TopicFeature21
                );
            }

            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calculateMinAndMax(Features minFeatures, Features maxFeatures, String UserFeature1, String UserFeature5, String UserFeature9, String UserFeature11, String UserFeature21,
                                    String UserFeature23, String Conservativeness_V1, String TopicTransition1, String TopicTransition2, String TopicTransition3_1,
                                    String TopicTransition3_3, String TopicTransition3_10, String SumTopicTransition1, String TopicSimilarity1,
                                    String TopicSimilarity7, String TopicFeature1, String TopicFeature8, String TopicFeature11, String TopicFeature12,
                                    String TopicFeature13, String TopicFeature16, String TopicFeature19, String TopicFeature20, String TopicFeature21) {
        Double UF1 = Double.parseDouble(UserFeature1);
        minFeatures.UserFeature1 = (UF1 < minFeatures.UserFeature1) ? UF1 : minFeatures.UserFeature1;
        maxFeatures.UserFeature1 = (UF1 > maxFeatures.UserFeature1) ? UF1 : maxFeatures.UserFeature1;

        Double UF5 = Double.parseDouble(UserFeature5);
        minFeatures.UserFeature5 = (UF5 < minFeatures.UserFeature5) ? UF5 : minFeatures.UserFeature5;
        maxFeatures.UserFeature5 = (UF5 > maxFeatures.UserFeature5) ? UF5 : maxFeatures.UserFeature5;

        Double UF9 = Double.parseDouble(UserFeature9);
        minFeatures.UserFeature9 = (UF9 < minFeatures.UserFeature9) ? UF9 : minFeatures.UserFeature9;
        maxFeatures.UserFeature9 = (UF9 > maxFeatures.UserFeature9) ? UF9 : maxFeatures.UserFeature9;

        Double UF11 = Double.parseDouble(UserFeature11);
        minFeatures.UserFeature11 = (UF11 < minFeatures.UserFeature11) ? UF11 : minFeatures.UserFeature11;
        maxFeatures.UserFeature11 = (UF11 > maxFeatures.UserFeature11) ? UF11 : maxFeatures.UserFeature11;

        Double UF21 = Double.parseDouble(UserFeature21);
        minFeatures.UserFeature21 = (UF21 < minFeatures.UserFeature21) ? UF21 : minFeatures.UserFeature21;
        maxFeatures.UserFeature21 = (UF21 > maxFeatures.UserFeature21) ? UF21 : maxFeatures.UserFeature21;

        Double UF23 = Double.parseDouble(UserFeature23);
        minFeatures.UserFeature23 = (UF23 < minFeatures.UserFeature23) ? UF23 : minFeatures.UserFeature23;
        maxFeatures.UserFeature23 = (UF23 > maxFeatures.UserFeature23) ? UF23 : maxFeatures.UserFeature23;

        Double Conserv = Double.parseDouble(Conservativeness_V1);
        minFeatures.Conservativeness_V1 = (Conserv < minFeatures.Conservativeness_V1) ? Conserv : minFeatures.Conservativeness_V1;
        maxFeatures.Conservativeness_V1 = (Conserv > maxFeatures.Conservativeness_V1) ? Conserv : maxFeatures.Conservativeness_V1;

        Double TT1 = Double.parseDouble(TopicTransition1);
        minFeatures.TopicTransition1 = (TT1 < minFeatures.TopicTransition1) ? TT1 : minFeatures.TopicTransition1;
        maxFeatures.TopicTransition1 = (TT1 > maxFeatures.TopicTransition1) ? TT1 : maxFeatures.TopicTransition1;

        Double TT2 = Double.parseDouble(TopicTransition2);
        minFeatures.TopicTransition2 = (TT2 < minFeatures.TopicTransition2) ? TT2 : minFeatures.TopicTransition2;
        maxFeatures.TopicTransition2 = (TT2 > maxFeatures.TopicTransition2) ? TT2 : maxFeatures.TopicTransition2;

        Double TT3_1 = Double.parseDouble(TopicTransition3_1);
        minFeatures.TopicTransition3_1 = (TT3_1 < minFeatures.TopicTransition3_1) ? TT3_1 : minFeatures.TopicTransition3_1;
        maxFeatures.TopicTransition3_1 = (TT3_1 > maxFeatures.TopicTransition3_1) ? TT3_1 : maxFeatures.TopicTransition3_1;

        Double TT3_3 = Double.parseDouble(TopicTransition3_3);
        minFeatures.TopicTransition3_3 = (TT3_3 < minFeatures.TopicTransition3_3) ? TT3_3 : minFeatures.TopicTransition3_3;
        maxFeatures.TopicTransition3_3 = (TT3_3 > maxFeatures.TopicTransition3_3) ? TT3_3 : maxFeatures.TopicTransition3_3;

        Double TT3_10 = Double.parseDouble(TopicTransition3_10);
        minFeatures.TopicTransition3_10 = (TT3_10 < minFeatures.TopicTransition3_10) ? TT3_10 : minFeatures.TopicTransition3_10;
        maxFeatures.TopicTransition3_10 = (TT3_10 > maxFeatures.TopicTransition3_10) ? TT3_10 : maxFeatures.TopicTransition3_10;

        Double STT1 = Double.parseDouble(SumTopicTransition1);
        minFeatures.SumTopicTransition1 = (STT1 < minFeatures.SumTopicTransition1) ? STT1 : minFeatures.SumTopicTransition1;
        maxFeatures.SumTopicTransition1 = (STT1 > maxFeatures.SumTopicTransition1) ? STT1 : maxFeatures.SumTopicTransition1;

        Double TS1 = Double.parseDouble(TopicSimilarity1);
        minFeatures.TopicSimilarity1 = (TS1 < minFeatures.TopicSimilarity1) ? TS1 : minFeatures.TopicSimilarity1;
        maxFeatures.TopicSimilarity1 = (TS1 > maxFeatures.TopicSimilarity1) ? TS1 : maxFeatures.TopicSimilarity1;

        Double TS7 = Double.parseDouble(TopicSimilarity7);
        minFeatures.TopicSimilarity7 = (TS7 < minFeatures.TopicSimilarity7) ? TS7 : minFeatures.TopicSimilarity7;
        maxFeatures.TopicSimilarity7 = (TS7 > maxFeatures.TopicSimilarity7) ? TS7 : maxFeatures.TopicSimilarity7;

        Double TF1 = Double.parseDouble(TopicFeature1);
        minFeatures.TopicFeature1 = (TF1 < minFeatures.TopicFeature1) ? TF1 : minFeatures.TopicFeature1;
        maxFeatures.TopicFeature1 = (TF1 > maxFeatures.TopicFeature1) ? TF1 : maxFeatures.TopicFeature1;

        Double TF8 = Double.parseDouble(TopicFeature8);
        minFeatures.TopicFeature8 = (TF8 < minFeatures.TopicFeature8) ? TF8 : minFeatures.TopicFeature8;
        maxFeatures.TopicFeature8 = (TF8 > maxFeatures.TopicFeature8) ? TF8 : maxFeatures.TopicFeature8;

        Double TF11 = Double.parseDouble(TopicFeature11);
        minFeatures.TopicFeature11 = (TF11 < minFeatures.TopicFeature11) ? TF11 : minFeatures.TopicFeature11;
        maxFeatures.TopicFeature11 = (TF11 > maxFeatures.TopicFeature11) ? TF11 : maxFeatures.TopicFeature11;

        Double TF12 = Double.parseDouble(TopicFeature12);
        minFeatures.TopicFeature12 = (TF12 < minFeatures.TopicFeature12) ? TF12 : minFeatures.TopicFeature12;
        maxFeatures.TopicFeature12 = (TF12 > maxFeatures.TopicFeature12) ? TF12 : maxFeatures.TopicFeature12;

        Double TF13 = Double.parseDouble(TopicFeature13);
        minFeatures.TopicFeature13 = (TF13 < minFeatures.TopicFeature13) ? TF13 : minFeatures.TopicFeature13;
        maxFeatures.TopicFeature13 = (TF13 > maxFeatures.TopicFeature13) ? TF13 : maxFeatures.TopicFeature13;

        Double TF16 = Double.parseDouble(TopicFeature16);
        minFeatures.TopicFeature16 = (TF16 < minFeatures.TopicFeature16) ? TF16 : minFeatures.TopicFeature16;
        maxFeatures.TopicFeature16 = (TF16 > maxFeatures.TopicFeature16) ? TF16 : maxFeatures.TopicFeature16;

        Double TF19 = Double.parseDouble(TopicFeature19);
        minFeatures.TopicFeature19 = (TF19 < minFeatures.TopicFeature19) ? TF19 : minFeatures.TopicFeature19;
        maxFeatures.TopicFeature19 = (TF19 > maxFeatures.TopicFeature19) ? TF19 : maxFeatures.TopicFeature19;

        Double TF20 = Double.parseDouble(TopicFeature20);
        minFeatures.TopicFeature20 = (TF20 < minFeatures.TopicFeature20) ? TF20 : minFeatures.TopicFeature20;
        maxFeatures.TopicFeature20 = (TF20 > maxFeatures.TopicFeature20) ? TF20 : maxFeatures.TopicFeature20;

        Double TF21 = Double.parseDouble(TopicFeature21);
        minFeatures.TopicFeature21 = (TF21 < minFeatures.TopicFeature21) ? TF21 : minFeatures.TopicFeature21;
        maxFeatures.TopicFeature21 = (TF21 > maxFeatures.TopicFeature21) ? TF21 : maxFeatures.TopicFeature21;
    }

    private void NormalizeInputData(String FileDirectory, String FileName, String outputFileName) {
        try {
            // Load input file
            BufferedReader reader = new BufferedReader(new FileReader(FileDirectory + FileName));
            // Year,Topic1,Topic2,OwnerUserId,Label,UserFeature1,UserFeature5,UserFeature9,UserFeature11,UserFeature21,UserFeature23,Conservativeness_V1,TopicTransition1,TopicTransition2,TopicTransition3_1,TopicTransition3_3,TopicTransition3_10,SumTopicTransition1,TopicSimilarity1,TopicSimilarity7,TopicFeature1,TopicFeature8,TopicFeature11,TopicFeature12,TopicFeature13,TopicFeature16,TopicFeature19,TopicFeature20,TopicFeature21
            String line = reader.readLine();

            //read first line
            String[] TC = reader.readLine().split(",");//FirstlineValues
            Features minFeatures = new Features(TC[5], TC[6], TC[7], TC[8], TC[9], TC[10], TC[11], TC[12], TC[13], TC[14], TC[15],
                    TC[16], TC[17], TC[18], TC[19], TC[20], TC[21], TC[22], TC[23], TC[24], TC[25], TC[26], TC[27], TC[28]);
            Features maxFeatures = new Features(TC[5], TC[6], TC[7], TC[8], TC[9], TC[10], TC[11], TC[12], TC[13], TC[14], TC[15],
                    TC[16], TC[17], TC[18], TC[19], TC[20], TC[21], TC[22], TC[23], TC[24], TC[25], TC[26], TC[27], TC[28]);

            while ((line = reader.readLine()) != null) {
                String[] TC1 = line.split(",");
                calculateMinAndMax(minFeatures, maxFeatures, TC1[5], TC1[6], TC1[7], TC1[8], TC1[9], TC1[10], TC1[11], TC1[12], TC1[13], TC1[14], TC1[15],
                        TC1[16], TC1[17], TC1[18], TC1[19], TC1[20], TC1[21], TC1[22], TC1[23], TC1[24], TC1[25], TC1[26], TC1[27], TC1[28]);
            }
            Features max_minus_min = new Features(maxFeatures.UserFeature1 - minFeatures.UserFeature1,
                    maxFeatures.UserFeature5 - minFeatures.UserFeature5, maxFeatures.UserFeature9 - minFeatures.UserFeature9,
                    maxFeatures.UserFeature11 - minFeatures.UserFeature11, maxFeatures.UserFeature21 - minFeatures.UserFeature21,
                    maxFeatures.UserFeature23 - minFeatures.UserFeature23, maxFeatures.Conservativeness_V1 - minFeatures.Conservativeness_V1,
                    maxFeatures.TopicTransition1 - minFeatures.TopicTransition1, maxFeatures.TopicTransition2 - minFeatures.TopicTransition2,
                    maxFeatures.TopicTransition3_1 - minFeatures.TopicTransition3_1, maxFeatures.TopicTransition3_3 - minFeatures.TopicTransition3_3,
                    maxFeatures.TopicTransition3_10 - minFeatures.TopicTransition3_10, maxFeatures.SumTopicTransition1 - minFeatures.SumTopicTransition1,
                    maxFeatures.TopicSimilarity1 - minFeatures.TopicSimilarity1, maxFeatures.TopicSimilarity7 - minFeatures.TopicSimilarity7,
                    maxFeatures.TopicFeature1 - minFeatures.TopicFeature1, maxFeatures.TopicFeature8 - minFeatures.TopicFeature8,
                    maxFeatures.TopicFeature11 - minFeatures.TopicFeature11, maxFeatures.TopicFeature12 - minFeatures.TopicFeature12,
                    maxFeatures.TopicFeature13 - minFeatures.TopicFeature13, maxFeatures.TopicFeature16 - minFeatures.TopicFeature16,
                    maxFeatures.TopicFeature19 - minFeatures.TopicFeature19, maxFeatures.TopicFeature20 - minFeatures.TopicFeature20,
                    maxFeatures.TopicFeature21 - minFeatures.TopicFeature21);
            reader.close();

            //write normalized data
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(FileDirectory + outputFileName));
            System.setOut(out);

            System.out.println("Year,Topic1,Topic2,OwnerUserId,Label," +
                            "UserFeature1,UserFeature5,UserFeature9,UserFeature11,UserFeature21,UserFeature23,Conservativeness_V1," +
                            "TopicTransition1,TopicTransition2,TopicTransition3_1,TopicTransition3_3,TopicTransition3_10,SumTopicTransition1," +
                            "TopicSimilarity1,TopicSimilarity7," +
                            "TopicFeature1,TopicFeature8,TopicFeature11,TopicFeature12,TopicFeature13,TopicFeature16,TopicFeature19,TopicFeature20,TopicFeature21"
            );

            BufferedReader reader2 = new BufferedReader(new FileReader(FileDirectory + FileName));
            String line2 = reader2.readLine();
            while ((line2 = reader2.readLine()) != null) {
                String[] TC2 = line2.split(",");
                String OutputLine = TC2[0] + "," + TC2[1] + "," + TC2[2] + "," + TC2[3] + "," + TC2[4] + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[5]) - minFeatures.UserFeature1) / max_minus_min.UserFeature1)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[6]) - minFeatures.UserFeature5) / max_minus_min.UserFeature5)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[7]) - minFeatures.UserFeature9) / max_minus_min.UserFeature9)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[8]) - minFeatures.UserFeature11) / max_minus_min.UserFeature11)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[9]) - minFeatures.UserFeature21) / max_minus_min.UserFeature21)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[10]) - minFeatures.UserFeature23) / max_minus_min.UserFeature23)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[11]) - minFeatures.Conservativeness_V1) / max_minus_min.Conservativeness_V1)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[12]) - minFeatures.TopicTransition1) / max_minus_min.TopicTransition1)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[13]) - minFeatures.TopicTransition2) / max_minus_min.TopicTransition2)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[14]) - minFeatures.TopicTransition3_1) / max_minus_min.TopicTransition3_1)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[15]) - minFeatures.TopicTransition3_3) / max_minus_min.TopicTransition3_3)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[16]) - minFeatures.TopicTransition3_10) / max_minus_min.TopicTransition3_10)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[17]) - minFeatures.SumTopicTransition1) / max_minus_min.SumTopicTransition1)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[18]) - minFeatures.TopicSimilarity1) / max_minus_min.TopicSimilarity1)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[19]) - minFeatures.TopicSimilarity7) / max_minus_min.TopicSimilarity7)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[20]) - minFeatures.TopicFeature1) / max_minus_min.TopicFeature1)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[21]) - minFeatures.TopicFeature8) / max_minus_min.TopicFeature8)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[22]) - minFeatures.TopicFeature11) / max_minus_min.TopicFeature11)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[23]) - minFeatures.TopicFeature12) / max_minus_min.TopicFeature12)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[24]) - minFeatures.TopicFeature13) / max_minus_min.TopicFeature13)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[25]) - minFeatures.TopicFeature16) / max_minus_min.TopicFeature16)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[26]) - minFeatures.TopicFeature19) / max_minus_min.TopicFeature19)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[27]) - minFeatures.TopicFeature20) / max_minus_min.TopicFeature20)) + ",";
                OutputLine += String.valueOf(((Double.parseDouble(TC2[28]) - minFeatures.TopicFeature21) / max_minus_min.TopicFeature21));
                System.out.println(OutputLine);
            }
            reader2.close();

            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void PreparePastTrainTestSet() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.PastSampleUsersDirectory + "NormalizedPastInputData.csv"));
            //Year,Topic1,Topic2,OwnerUserId,Label,UserFeature1,UserFeature5,UserFeature9,UserFeature11,UserFeature21,UserFeature23,Conservativeness_V1,TopicTransition1,TopicTransition2,TopicTransition3_1,TopicTransition3_3,TopicTransition3_10,SumTopicTransition1,TopicSimilarity1,TopicSimilarity7,TopicFeature1,TopicFeature8,TopicFeature11,TopicFeature12,TopicFeature13,TopicFeature16,TopicFeature19,TopicFeature20,TopicFeature21
            String Firstline = reader.readLine();
            String line;
            HashMap<String, String> NormalizedPastInputData = new HashMap<String, String>();
            while ((line = reader.readLine()) != null) {
                String[] TC = line.split(",");
                NormalizedPastInputData.put(TC[0] + "," + TC[1] + "," + TC[2] + "," + TC[3] + "," + TC[4], line);
            }
            reader.close();

            //write trining data
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.PastSampleUsersDirectory + "NormalizedPastTrainingSet.csv"));
            System.setOut(out);

            System.out.println(Firstline);
            BufferedReader reader2 = new BufferedReader(new FileReader(Constants.PastSampleUsersDirectory + "PastTrainingSample.csv"));
            String line2 = reader2.readLine();
            while ((line2 = reader2.readLine()) != null) {
                line2 = line2.replace("\n", "");
                if (NormalizedPastInputData.containsKey(line2))
                    System.out.println(NormalizedPastInputData.get(line2));
            }
            reader2.close();

            System.setOut(stdout);

            //write test data
            PrintStream out2 = new PrintStream(new FileOutputStream(Constants.PastSampleUsersDirectory + "NormalizedPastTestSet.csv"));
            System.setOut(out2);

            System.out.println(Firstline);
            BufferedReader reader3 = new BufferedReader(new FileReader(Constants.PastSampleUsersDirectory + "PastTestSample.csv"));
            String line3 = reader3.readLine();
            while ((line3 = reader3.readLine()) != null) {
                line3 = line3.replace("\n", "");
                if (NormalizedPastInputData.containsKey(line3))
                    System.out.println(NormalizedPastInputData.get(line3));
            }
            reader3.close();

            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void PartitionData(String FileDirectory, String InputFileName, String TrainingFileName, String TestFileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.NewSampleUsersDirectory + "NormalizedNewInputData.csv"));
            //Year,Topic1,Topic2,OwnerUserId,Label,UserFeature1,UserFeature5,UserFeature9,UserFeature11,UserFeature21,UserFeature23,Conservativeness_V1,TopicTransition1,TopicTransition2,TopicTransition3_1,TopicTransition3_3,TopicTransition3_10,SumTopicTransition1,TopicSimilarity1,TopicSimilarity7,TopicFeature1,TopicFeature8,TopicFeature11,TopicFeature12,TopicFeature13,TopicFeature16,TopicFeature19,TopicFeature20,TopicFeature21
            String Firstline = reader.readLine();
            String line;
            HashSet<String> positiveInstance = new HashSet<String>();
            HashSet<String> negetiveInstance = new HashSet<String>();
            Integer i=0;
            while ((line = reader.readLine()) != null) {
                String[] TC = line.split(",");
                if (Integer.parseInt(TC[4]) == 1) {
                    positiveInstance.add(line);
                } else {
                    negetiveInstance.add(line);
                }
                i+=1;
            }
            reader.close();

            List<String> PositiveTuplesList = new ArrayList<String>(positiveInstance);
            List<String> NegetiveTuplesList = new ArrayList<String>(negetiveInstance);
            Collections.shuffle(PositiveTuplesList, new Random(10));
            Collections.shuffle(NegetiveTuplesList, new Random(10));

            Double PositiveTrainingSampleCount = (positiveInstance.size()*0.7);
            Double NegativeTrainingSampleCount = (negetiveInstance.size()*0.7);

            //write trining data
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.NewSampleUsersDirectory + "NormalizedNewTrainingSet.csv"));
            System.setOut(out);

            System.out.println(Firstline);
            for (String tuple : PositiveTuplesList.subList(0, PositiveTrainingSampleCount.intValue()+1)) {
                System.out.println(tuple);
            }
            for (String tuple : NegetiveTuplesList.subList(0, NegativeTrainingSampleCount.intValue()+1)) {
                System.out.println(tuple);
            }

            System.setOut(stdout);

            //write test data
            PrintStream out2 = new PrintStream(new FileOutputStream(Constants.NewSampleUsersDirectory + "NormalizedNewTestSet.csv"));
            System.setOut(out2);

            System.out.println(Firstline);
            for (String tuple : PositiveTuplesList.subList(PositiveTrainingSampleCount.intValue()+1, PositiveTuplesList.size())) {
                System.out.println(tuple);
            }
            for (String tuple : NegetiveTuplesList.subList(NegativeTrainingSampleCount.intValue()+1, NegetiveTuplesList.size())) {
                System.out.println(tuple);
            }

            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PrepareLearningSet p = new PrepareLearningSet();
        // Generate a sample set of tuples (Year,Topic1,Topic2,OwnerUserId,Label)
        //p.getSampleUsers(Constants.NewSampleUsersDirectory + "NewSampleUsers.txt");

        //Extract precalculated features for these tuples
        //p.ExtractFeatures(Constants.NewSampleUsersDirectory, "NewSampleUsers.txt", "NewInputData.csv");
        //p.ExtractFeatures(Constants.PastSampleUsersDirectory, "PastSampleUsers.txt", "PastInputData.csv");

        //Normalize(min-max) each feature of data file
        //p.NormalizeInputData(Constants.NewSampleUsersDirectory, "NewInputData.csv", "NormalizedNewInputData.csv");
        //p.NormalizeInputData(Constants.PastSampleUsersDirectory, "PastInputData.csv", "NormalizedPastInputData.csv");

        //p.PreparePastTrainTestSet();

        p.PartitionData(Constants.NewSampleUsersDirectory, "NormalizedNewInputData.csv", "NormalizedNewTrainingSet.csv", "NormalizedNewTestSet.csv");
    }


    public void startFeatureCalculations() {
        // Generate a sample set of tuples (Year,Topic1,Topic2,OwnerUserId,Label)
        getSampleUsers(Constants.NewSampleUsersDirectory + "NewSampleUsers.txt");

        //Extract precalculated features for these tuples
        ExtractFeatures(Constants.NewSampleUsersDirectory, "NewSampleUsers.txt", "NewInputData.csv");

        //Normalize(min-max) each feature of data file
        NormalizeInputData(Constants.NewSampleUsersDirectory, "NewInputData.csv", "NormalizedNewInputData.csv");

        //Partition data
        PartitionData(Constants.NewSampleUsersDirectory, "NormalizedNewInputData.csv", "NormalizedNewTrainingSet.csv", "NormalizedNewTestSet.csv");
    }

    class UserTuple {
        String Year;
        String Topic1;
        String Topic2;
        String UserID;
        String Label;
        String UserFeature1;
        String UserFeature5;
        String UserFeature9;
        String UserFeature11;
        String UserFeature21;
        String UserFeature23;
        String Conservativeness_V1;
        String TopicTransition1;
        String TopicTransition2;
        String TopicTransition3_1;
        String TopicTransition3_3;
        String TopicTransition3_10;
        String SumTopicTransition1;
        String TopicSimilarity1;
        String TopicSimilarity7;
        String TopicFeature1;
        String TopicFeature8;
        String TopicFeature11;
        String TopicFeature12;
        String TopicFeature13;
        String TopicFeature16;
        String TopicFeature19;
        String TopicFeature20;
        String TopicFeature21;

        public UserTuple(String Year, String Topic1, String Topic2, String UserID, String Label) {
            this.Year = Year;
            this.Topic1 = Topic1;
            this.Topic2 = Topic2;
            this.UserID = UserID;
            this.Label = Label;
        }
    }

    class Features {
        Double UserFeature1;
        Double UserFeature5;
        Double UserFeature9;
        Double UserFeature11;
        Double UserFeature21;
        Double UserFeature23;
        Double Conservativeness_V1;
        Double TopicTransition1;
        Double TopicTransition2;
        Double TopicTransition3_1;
        Double TopicTransition3_3;
        Double TopicTransition3_10;
        Double SumTopicTransition1;
        Double TopicSimilarity1;
        Double TopicSimilarity7;
        Double TopicFeature1;
        Double TopicFeature8;
        Double TopicFeature11;
        Double TopicFeature12;
        Double TopicFeature13;
        Double TopicFeature16;
        Double TopicFeature19;
        Double TopicFeature20;
        Double TopicFeature21;

        public Features(String UserFeature1, String UserFeature5, String UserFeature9, String UserFeature11, String UserFeature21,
                        String UserFeature23, String Conservativeness_V1, String TopicTransition1, String TopicTransition2, String TopicTransition3_1,
                        String TopicTransition3_3, String TopicTransition3_10, String SumTopicTransition1, String TopicSimilarity1,
                        String TopicSimilarity7, String TopicFeature1, String TopicFeature8, String TopicFeature11, String TopicFeature12,
                        String TopicFeature13, String TopicFeature16, String TopicFeature19, String TopicFeature20, String TopicFeature21) {
            this.UserFeature1 = Double.parseDouble(UserFeature1);
            this.UserFeature5 = Double.parseDouble(UserFeature5);
            this.UserFeature9 = Double.parseDouble(UserFeature9);
            this.UserFeature11 = Double.parseDouble(UserFeature11);
            this.UserFeature21 = Double.parseDouble(UserFeature21);
            this.UserFeature23 = Double.parseDouble(UserFeature23);
            this.Conservativeness_V1 = Double.parseDouble(Conservativeness_V1);
            this.TopicTransition1 = Double.parseDouble(TopicTransition1);
            this.TopicTransition2 = Double.parseDouble(TopicTransition2);
            this.TopicTransition3_1 = Double.parseDouble(TopicTransition3_1);
            this.TopicTransition3_3 = Double.parseDouble(TopicTransition3_3);
            this.TopicTransition3_10 = Double.parseDouble(TopicTransition3_10);
            this.SumTopicTransition1 = Double.parseDouble(SumTopicTransition1);
            this.TopicSimilarity1 = Double.parseDouble(TopicSimilarity1);
            this.TopicSimilarity7 = Double.parseDouble(TopicSimilarity7);
            this.TopicFeature1 = Double.parseDouble(TopicFeature1);
            this.TopicFeature8 = Double.parseDouble(TopicFeature8);
            this.TopicFeature11 = Double.parseDouble(TopicFeature11);
            this.TopicFeature12 = Double.parseDouble(TopicFeature12);
            this.TopicFeature13 = Double.parseDouble(TopicFeature13);
            this.TopicFeature16 = Double.parseDouble(TopicFeature16);
            this.TopicFeature19 = Double.parseDouble(TopicFeature19);
            this.TopicFeature20 = Double.parseDouble(TopicFeature20);
            this.TopicFeature21 = Double.parseDouble(TopicFeature21);
        }

        public Features(Double UserFeature1, Double UserFeature5, Double UserFeature9, Double UserFeature11, Double UserFeature21,
                        Double UserFeature23, Double Conservativeness_V1, Double TopicTransition1, Double TopicTransition2, Double TopicTransition3_1,
                        Double TopicTransition3_3, Double TopicTransition3_10, Double SumTopicTransition1, Double TopicSimilarity1,
                        Double TopicSimilarity7, Double TopicFeature1, Double TopicFeature8, Double TopicFeature11, Double TopicFeature12,
                        Double TopicFeature13, Double TopicFeature16, Double TopicFeature19, Double TopicFeature20, Double TopicFeature21) {
            this.UserFeature1 = UserFeature1;
            this.UserFeature5 = UserFeature5;
            this.UserFeature9 = UserFeature9;
            this.UserFeature11 = UserFeature11;
            this.UserFeature21 = UserFeature21;
            this.UserFeature23 = UserFeature23;
            this.Conservativeness_V1 = Conservativeness_V1;
            this.TopicTransition1 = TopicTransition1;
            this.TopicTransition2 = TopicTransition2;
            this.TopicTransition3_1 = TopicTransition3_1;
            this.TopicTransition3_3 = TopicTransition3_3;
            this.TopicTransition3_10 = TopicTransition3_10;
            this.SumTopicTransition1 = SumTopicTransition1;
            this.TopicSimilarity1 = TopicSimilarity1;
            this.TopicSimilarity7 = TopicSimilarity7;
            this.TopicFeature1 = TopicFeature1;
            this.TopicFeature8 = TopicFeature8;
            this.TopicFeature11 = TopicFeature11;
            this.TopicFeature12 = TopicFeature12;
            this.TopicFeature13 = TopicFeature13;
            this.TopicFeature16 = TopicFeature16;
            this.TopicFeature19 = TopicFeature19;
            this.TopicFeature20 = TopicFeature20;
            this.TopicFeature21 = TopicFeature21;
        }
    }
}


