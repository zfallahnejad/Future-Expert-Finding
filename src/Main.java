import DBM.DBM_ALL;
import DBM.DBM_RECENT;
import FeatureExtraction.TopicFeature;
import FeatureExtraction.TopicSimilarity;
import FeatureExtraction.TopicTransition;
import FeatureExtraction.UserFeature;
import GoldenSet.ExpertUsers;
import GoldenSet.TopicalExpertUsers;
import Index.LuceneIndex;
import ML.PrepareLearningSet;
import TPBM.*;
import TBM.*;
import Utility.Constants;
import Utility.MAP;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Created by Zohreh on 6/18/2017.
 */
public class Main {
    public static void main(String[] args) {
        Main m = new Main();

        //m.preparation();

        //Document based model (DBM)
        //m.DBM_Baseline();

        //Topic Based model (TBM)
        //m.TBM_Baseline();

        //Temporal profile based model (TPBM)
        //m.TPBM_Baseline();

        // Features
        //m.CalculateFeatures();

        //Prepare for Learning Phase
        //m.ML_preparation();
    }

    /********************************
     * Preparation
     ******************************************/

    private void preparation() {
        //Create a lucene index for java subset of stackoverflow dataset
        IndexInputData();

        //Sort users based on accepted answers in each tag decreasingly
        //as evidence of expertise in a tag
        FindExpertUsers();

        //Sort users based on accepted answers in each topic decreasingly
        //as evidence of expertise in a topic
        FindTopicalExpertUsers();
    }

    /**
     * Construct Lucene Index from xml input data
     */
    public void IndexInputData() {
        LuceneIndex l = new LuceneIndex(Constants.IndexDirectory);
        l.index(Constants.JavaXMLInput);
    }

    /**
     * Finds Experts Users to be used as golden set
     * This function considered users expertise in each tag
     */
    public void FindExpertUsers() {
        ExpertUsers e = new ExpertUsers();
        e.FindExperts();
    }

    /**
     * Finds Experts Users
     * This function considered users expertise in each topic
     */
    public void FindTopicalExpertUsers() {
        TopicalExpertUsers e = new TopicalExpertUsers();
        e.FindExperts();
    }


    /********************************        Baseline1       ******************************************/
    /*****************************  Document based model (DBM)   **************************************/

    /**
     * This function calls balog baselines and evaluate their results
     */
    public void DBM_Baseline() {
        DBM_Recent_baseline();
        getMAP(Constants.DBM_RECENT_Directory, "DBM_Recent_", "DBM_RECENT");
        getPat(Constants.DBM_RECENT_Directory, "DBM_Recent_", 1, "DBM_RECENT");
        getPat(Constants.DBM_RECENT_Directory, "DBM_Recent_", 5, "DBM_RECENT");
        getPat(Constants.DBM_RECENT_Directory, "DBM_Recent_", 10, "DBM_RECENT");

        DBM_All_baseline();
        getMAP(Constants.DBM_ALL_Directory, "DBM_All_", "DBM_ALL");
        getPat(Constants.DBM_ALL_Directory, "DBM_All_", 1, "DBM_ALL");
        getPat(Constants.DBM_ALL_Directory, "DBM_All_", 5, "DBM_ALL");
        getPat(Constants.DBM_ALL_Directory, "DBM_All_", 10, "DBM_ALL");
    }

    private void DBM_Recent_baseline() {
        DBM_RECENT b = new DBM_RECENT();
        b.startDBM_RECENT();
    }

    private void DBM_All_baseline() {
        DBM_ALL b = new DBM_ALL();
        b.startDBM_ALL();
    }

    /********************************        Baseline2      ******************************************/
    /********************************  Topic Based model (TBM)   *************************************/

    /**
     * This function calls Momtazi baseline and evaluate their results
     */
    public void TBM_Baseline() {
        P_User_Topic p = new P_User_Topic();
        p.get_P_E_M();

        TBM t = new TBM();
        t.startFangBaselineCalculations("V1", "Answer");

        getMAP(Constants.TBM_Directory, "TBM_", "TBM");
        getPat(Constants.TBM_Directory, "TBM_", 1, "TBM");
        getPat(Constants.TBM_Directory, "TBM_", 5, "TBM");
        getPat(Constants.TBM_Directory, "TBM_", 10, "TBM");
    }


    /********************************        Baseline3      ******************************************/
    /***********************  Temporal profile based model (TPBM)   **********************************/

    /**
     * This function calls Fang baseline and evaluate their results
     */
    public void TPBM_Baseline() {
        // Calculate popularity of each topic in each year
        Popularity p = new Popularity();
        p.startCalculations();

        // Calculate various version of P(at|e) or P_t(a|e)
        // Probability of association between a topic a and a candidate e at time t
        P_at_e p2 = new P_at_e();
        p2.P_at_e_AnswerVersion();
        p2.P_at_e_QuestionAnswerVersion();
        p2.TopicUserActivity_V2_Answer();

        // Calculare probability of generation of query word given the topic (Retrieve required values from Mallet LDA outputs)
        PWA p3 = new PWA();
        p3.startPWACalculations();

        // Calculate cnservativeness of each user
        Conservativeness c = new Conservativeness();
        c.startCalculations("V1");

        TPBM b = new TPBM();
        b.startFangBaselineCalculations("V1", "V1", "Answer");

        getMAP(Constants.TPBM_Directory, "TPBM_", "TPBM");
        getPat(Constants.TPBM_Directory, "TPBM_", 1, "TPBM");
        getPat(Constants.TPBM_Directory, "TPBM_", 5, "TPBM");
        getPat(Constants.TPBM_Directory, "TPBM_", 10, "TPBM");
    }

    /*********************************************
     * *********     Features       ***************
     *********************************************/

    private void CalculateFeatures() {
        // Topic Transition Group of Features(F19-F24)
        TopicTransition tt = new TopicTransition();
        tt.startFeatureCalculations();

        // Topic Feature Group of Features(F3-F11)
        TopicFeature tf = new TopicFeature();
        tf.startFeatureCalculations();

        // User Group of Features(F12-F18)
        UserFeature uf = new UserFeature();
        uf.startFeatureCalculations();

        // Topic Similarity Group of Features(F1 and F2)
        TopicSimilarity ts = new TopicSimilarity();
        ts.startFeatureCalculations();
    }

    /********************************
     * Preparation for Learning Phase
     ******************************************/

    private void ML_preparation() {
        PrepareLearningSet p = new PrepareLearningSet();
        p.startFeatureCalculations();
    }

    /********************************        Evaluation       ******************************************/

    /**
     * This function evaluate results based on MAP mteric
     *
     * @param DirName              Result Directory
     * @param fileName             Result FileName
     * @param prediction_directory Prediction Directory
     */
    private void getMAP(String DirName, String fileName, String prediction_directory) {
        MAP m = new MAP();

        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.EvaluationResultsDirectory + prediction_directory + "\\" + fileName + "_MAP.txt"));
            System.setOut(out);

            for (int fyear = 2009; fyear < 2016; fyear++) {
                for (String tag : Constants.TopTags) {
                    m.computeMAP(tag, fyear, DirName + fileName + tag + "_FYear_" + fyear + ".txt");
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            System.out.println(Constants.EvaluationResultsDirectory + prediction_directory + "\\" + fileName + "_MAP.txt");
            System.out.println("\n\n\n\nSorry!\n\n\n\n");
        }
    }

    /**
     * This function evaluate results based on P@n mteric
     *
     * @param DirName              Result Directory
     * @param fileName             Result FileName
     * @param prediction_directory Prediction Directory
     */
    private void getPat(String DirName, String fileName, int n, String prediction_directory) {
        MAP m = new MAP();

        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.EvaluationResultsDirectory + prediction_directory + "\\" + fileName + "_P_" + n + ".txt"));
            System.setOut(out);

            for (int fyear = 2009; fyear < 2016; fyear++) {
                for (String tag : Constants.TopTags) {
                    m.computePat(tag, fyear, DirName + fileName + tag + "_FYear_" + fyear + ".txt", n);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            System.out.println("\n\n\n\nSorry!\n\n\n\n");
        }
    }
}
