import DBM.DBM_ALL;
import DBM.DBM_RECENT;
import GoldenSet.ExpertUsers;
import GoldenSet.TopicalExpertUsers;
import Index.LuceneIndex;
import Utility.Constants;
import Utility.MAP;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by Zohreh on 6/18/2017.
 */
public class Main {
    public static void main(String[] args) {
        Main m = new Main();

        //m.preparation();

        //Document based model (DBM)
        m.DBM_Baseline();

    }

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


    /********************************        Evaluation       ******************************************/

    /**
     * This function evaluate results based on MAP mteric
     *
     * @param DirName  Result Directory
     * @param fileName Result FileName
     * @param prediction_directory Prediction Directory
     */
    private void getMAP(String DirName, String fileName, String prediction_directory) {
        MAP m = new MAP();

        PrintStream stdout = System.out;
        try {
            PrintStream out = new PrintStream(new FileOutputStream(Constants.EvaluationResultsDirectory + prediction_directory + "\\" + fileName + "_MAP.txt"));
            System.setOut(out);

            for (int fyear = 2009; fyear < 2016; fyear++) {
                for (String tag : Constants.TopTags) {
                    m.computeMAP(tag, fyear, DirName + fileName + tag + "_FYear_" + fyear + ".txt");
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            System.out.println("\n\n\n\nSorry!\n\n\n\n");
        }
    }

    /**
     * This function evaluate results based on P@n mteric
     *
     * @param DirName  Result Directory
     * @param fileName Result FileName
     * @param prediction_directory Prediction Directory
     */
    private void getPat(String DirName, String fileName, int n, String prediction_directory) {
        MAP m = new MAP();

        PrintStream stdout = System.out;
        try {
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
