package Utility;

import javax.xml.transform.Result;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Zohreh on 6/30/2017.
 */
public class MAP {

    /*public static void main(String[] args) throws IOException {
        MAP m = new MAP();

        for (String tag : Constants.TopTags) {
            for (int fyear = 2009; fyear < 2016; fyear++) {
                m.computeMAP(tag,fyear,"");
            }
        }
    }*/

    public void computeMAP(String tag, Integer fyear, String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Constants.TagGoldenSetDirectory+ tag + ".txt"));
        ArrayList<String> goldenMeasure = new ArrayList<>();
        String line = "";
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (Integer.parseInt(parts[1]) == fyear && Integer.parseInt(parts[3]) >= 10) {
                goldenMeasure.add(parts[2]);
            }
        }
        //System.out.println(goldenMeasure);
        reader.close();

        BufferedReader reader2 = new BufferedReader(new FileReader(fileName));
        String line2 = "";
        ArrayList<result> list = new ArrayList<result>();

        while ((line2 = reader2.readLine()) != null) {
            String[] parts = line2.split(",");
            result s = new result(parts[0], Double.parseDouble(parts[3]), goldenMeasure.contains(parts[0]));
            list.add(s);
        }
        reader2.close();
        Collections.sort(list);
        int relevant = 0;
        double sum = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isExpert) relevant++;
            double pati = relevant / (i + 1.0);
            if (list.get(i).isExpert) sum += pati;
        }
        System.out.println(tag + "," + fyear + "," + relevant + "," + goldenMeasure.size() + "," + sum / goldenMeasure.size());

    }

    public void computePat(String tag, Integer fyear, String fileName, int n) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Constants.TagGoldenSetDirectory+ tag + ".txt"));
        ArrayList<String> goldenMeasure = new ArrayList<>();
        String line = "";
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (Integer.parseInt(parts[1]) == fyear && Integer.parseInt(parts[3]) >= 10) {
                goldenMeasure.add(parts[2]);
            }
        }
        //System.out.println(goldenMeasure);
        reader.close();

        BufferedReader reader2 = new BufferedReader(new FileReader(fileName));
        String line2 = "";
        ArrayList<result> list = new ArrayList<result>();

        while ((line2 = reader2.readLine()) != null) {
            String[] parts = line2.split(",");
            result s = new result(parts[0], Double.parseDouble(parts[3]), goldenMeasure.contains(parts[0]));
            list.add(s);
        }
        reader2.close();
        Collections.sort(list);
        int relevant = 0;
        int len = list.size() < n ? list.size() : n;
        for (int i = 0; i < len; i++)
            if (list.get(i).isExpert) relevant++;
        double pati = relevant * 1.0 / n;
        System.out.println(tag + "," + fyear + "," + relevant + "," + n + "," + pati);
    }

    class result implements Comparable<result> {
        String eid;
        Double score;
        boolean isExpert;

        public result(String eid, Double score, boolean contains) {
            this.eid = eid;
            this.isExpert = contains;
            if (isExpert)
                this.score = score + 0.0001;
            else
                this.score = score;

        }

        @Override
        public int compareTo(result o) {
            return -1 * Double.compare(score, o.score);
        }
    }
}
