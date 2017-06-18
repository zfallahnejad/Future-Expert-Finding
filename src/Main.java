import GoldenSet.ExpertUsers;
import GoldenSet.TopicalExpertUsers;
import Index.LuceneIndex;
import Utility.Constants;

/**
 * Created by Zohreh on 6/18/2017.
 */
public class Main {
    public static void main(String[] args) {
        Main m = new Main();

        m.preparation();


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

}
