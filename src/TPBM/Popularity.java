package TPBM;

import Index.IndexUtility;
import Utility.Constants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Zohreh on 7/8/2017.
 */
public class Popularity {
    IndexUtility u;
    IndexReader reader;
    IndexSearcher searcher;
    Analyzer analyzer;

    public Popularity() {
        try {
            u = new IndexUtility();
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(Constants.IndexDirectory)));
            searcher = new IndexSearcher(reader);
            analyzer = new EnglishAnalyzer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startCalculations() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.Features_Directory + "PopularityV1.txt"));
            System.setOut(out);

            System.out.println("Topic,CurrentYear,Popularity");

            for (int futureYear = 2009; futureYear < 2017; futureYear++) {
                for (Integer topic = 0;topic<50;topic++) {
                    double popularity = tagPopularity(topic, (futureYear-1));
                    System.out.println(topic + "," + (futureYear - 1) + "," + popularity);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private double tagPopularity(Integer futureTopic, int currentYear) {
        Integer N_t = u.getDocCount(u.SearchCreationDate(currentYear));
        Integer N_atp1_t = u.getDocCount(
                u.BooleanQueryAnd(u.SearchCreationDate(currentYear), u.SearchTopic(futureTopic)));
        double output = (1.0 * N_atp1_t) / N_t;
        return output;
    }

    public static void main(String[] args) {
        Popularity p = new Popularity();
        p.startCalculations();
    }
}
