package FeatureExtraction;

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
import java.util.HashMap;

/**
 * Created by Zohreh on 1/28/2018.
 */
public class TopicFeature {
    IndexUtility u;
    IndexReader reader;
    IndexSearcher searcher;
    Analyzer analyzer;

    public TopicFeature() {
        try {
            u = new IndexUtility();
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(Constants.IndexDirectory)));
            searcher = new IndexSearcher(reader);
            analyzer = new EnglishAnalyzer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Popularity of topic2 (posts of year y)
     * Feature F10 in paper
     */
    private void getTopicFeature1() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature1.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature1");
            for (int year = 2008; year < 2015; year++) {
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Integer N_t = u.getDocCount(u.SearchCreationDate(year));
                    Integer N_at1_t = u.getDocCount(u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchTopic(topic2)));
                    double output = (1.0 * N_at1_t) / N_t;
                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F3 in paper
     */
    private void getTopicFeature8() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature8.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,TopicFeature8");
            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    for (int topic2 = 0; topic2 < 50; topic2++) {
                        Integer topic1PostCount = u.getDocCount(u.BooleanQueryAnd(u.SearchTopic(topic1), u.SearchCreationDate(year)));
                        Integer topic2PostCount = u.getDocCount(u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));

                        System.out.println(topic1 + "," + topic2 + "," + year + "," + (topic2PostCount - topic1PostCount));
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F4 in paper
     */
    private void getTopicFeature11() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature11.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature11");
            for (int year = 2008; year < 2015; year++) {
                Query exQ1 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.SearchCreationDate(year));
                TopDocs hits1 = searcher.search(exQ1, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs1 = hits1.scoreDocs;

                Integer TotalNumOfPostsTopic1 = ScDocs1.length;
                Integer TotalAnswerCountTopic1 = 0;

                for (int i = 0; i < ScDocs1.length; ++i) {
                    int docId = ScDocs1[i].doc;
                    Document d = searcher.doc(docId);
                    TotalAnswerCountTopic1 += Integer.parseInt(d.get("AnswerCount"));
                }
                Double AverageAnswerCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalAnswerCountTopic1 * 1.0 / TotalNumOfPostsTopic1));
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                    Integer TotalAnswerCountTopic2 = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        TotalAnswerCountTopic2 += Integer.parseInt(d.get("AnswerCount"));
                    }
                    Double AverageTopic2AnswerCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalAnswerCountTopic2 * 1.0 / TotalNumOfPostsTopic2));
                    Double output = (AverageAnswerCount == 0 ? 0 : (AverageTopic2AnswerCount * 1.0 / AverageAnswerCount));
                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F5 in paper
     */
    private void getTopicFeature12() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature12.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,TopicFeature12");
            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    Query exQ = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic1), u.SearchCreationDate(year)));
                    TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs = hits.scoreDocs;

                    Integer TotalNumOfPostsTopic1 = ScDocs.length;
                    Integer TotalAnswerCountTopic1 = 0;

                    for (int i = 0; i < ScDocs.length; ++i) {
                        int docId = ScDocs[i].doc;
                        Document d = searcher.doc(docId);
                        TotalAnswerCountTopic1 += Integer.parseInt(d.get("AnswerCount"));
                    }
                    Double AverageTopic1AnswerCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalAnswerCountTopic1 * 1.0 / TotalNumOfPostsTopic1));

                    for (int topic2 = 0; topic2 < 50; topic2++) {
                        Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                        TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                        ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                        Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                        Integer TotalAnswerCountTopic2 = 0;

                        for (int i = 0; i < ScDocs2.length; ++i) {
                            int docId = ScDocs2[i].doc;
                            Document d = searcher.doc(docId);
                            TotalAnswerCountTopic2 += Integer.parseInt(d.get("AnswerCount"));
                        }
                        Double AverageTopic2AnswerCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalAnswerCountTopic2 * 1.0 / TotalNumOfPostsTopic2));

                        System.out.println(topic1 + "," + topic2 + "," + year + "," + (AverageTopic2AnswerCount - AverageTopic1AnswerCount));
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F6 in paper
     */
    private void getTopicFeature13() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature13.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature13");
            for (int year = 2008; year < 2015; year++) {
                Query exQ1 = u.SearchCreationDate(year);
                TopDocs hits1 = searcher.search(exQ1, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs1 = hits1.scoreDocs;

                Integer TotalNumOfPostsTopic1 = ScDocs1.length;
                Integer TotalCommentCountTopic1 = 0;

                for (int i = 0; i < ScDocs1.length; ++i) {
                    int docId = ScDocs1[i].doc;
                    Document d = searcher.doc(docId);
                    TotalCommentCountTopic1 += Integer.parseInt(d.get("CommentCount"));
                }
                Double AverageCommentCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalCommentCountTopic1 * 1.0 / TotalNumOfPostsTopic1));
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                    Integer TotalCommentCountTopic2 = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        TotalCommentCountTopic2 += Integer.parseInt(d.get("CommentCount"));
                    }
                    Double AverageTopic2CommentCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalCommentCountTopic2 * 1.0 / TotalNumOfPostsTopic2));
                    Double output = (AverageCommentCount == 0 ? 0 : (AverageTopic2CommentCount * 1.0 / AverageCommentCount));

                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F7 in paper
     */
    private void getTopicFeature16() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature16.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,TopicFeature16");
            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    Query exQ = u.BooleanQueryAnd(u.SearchTopic(topic1), u.SearchCreationDate(year));
                    TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs = hits.scoreDocs;

                    Integer TotalNumOfPostsTopic1 = ScDocs.length;
                    Integer TotalCommentCountTopic1 = 0;

                    for (int i = 0; i < ScDocs.length; ++i) {
                        int docId = ScDocs[i].doc;
                        Document d = searcher.doc(docId);
                        TotalCommentCountTopic1 += Integer.parseInt(d.get("CommentCount"));
                    }
                    Double AverageTopic1CommentCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalCommentCountTopic1 * 1.0 / TotalNumOfPostsTopic1));

                    for (int topic2 = 0; topic2 < 50; topic2++) {
                        Query exQ2 = u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year));
                        TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                        ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                        Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                        Integer TotalCommentCountTopic2 = 0;

                        for (int i = 0; i < ScDocs2.length; ++i) {
                            int docId = ScDocs2[i].doc;
                            Document d = searcher.doc(docId);
                            TotalCommentCountTopic2 += Integer.parseInt(d.get("CommentCount"));
                        }
                        Double AverageTopic2CommentCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalCommentCountTopic2 * 1.0 / TotalNumOfPostsTopic2));

                        System.out.println(topic1 + "," + topic2 + "," + year + "," + (AverageTopic2CommentCount - AverageTopic1CommentCount));
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F8 in paper
     */
    private void getTopicFeature19() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature19.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature19");
            for (int year = 2008; year < 2015; year++) {
                Query exQ1 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.SearchCreationDate(year));
                TopDocs hits1 = searcher.search(exQ1, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs1 = hits1.scoreDocs;

                Integer TotalNumOfPostsTopic1 = ScDocs1.length;
                Integer TotalFavoriteCountTopic1 = 0;

                for (int i = 0; i < ScDocs1.length; ++i) {
                    int docId = ScDocs1[i].doc;
                    Document d = searcher.doc(docId);
                    TotalFavoriteCountTopic1 += Integer.parseInt(d.get("FavoriteCount"));
                }
                Double AverageFavoriteCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalFavoriteCountTopic1 * 1.0 / TotalNumOfPostsTopic1));
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                    Integer TotalFavoriteCountTopic2 = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        TotalFavoriteCountTopic2 += Integer.parseInt(d.get("FavoriteCount"));
                    }
                    Double AverageTopic2FavoriteCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalFavoriteCountTopic2 * 1.0 / TotalNumOfPostsTopic2));
                    Double output = (AverageFavoriteCount == 0 ? 0 : (AverageTopic2FavoriteCount * 1.0 / AverageFavoriteCount));

                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F9 in paper
     */
    private void getTopicFeature20() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature20.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,TopicFeature20");
            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    Query exQ = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic1), u.SearchCreationDate(year)));
                    TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs = hits.scoreDocs;

                    Integer TotalNumOfPostsTopic1 = ScDocs.length;
                    Integer TotalFavoriteCountTopic1 = 0;

                    for (int i = 0; i < ScDocs.length; ++i) {
                        int docId = ScDocs[i].doc;
                        Document d = searcher.doc(docId);
                        TotalFavoriteCountTopic1 += Integer.parseInt(d.get("FavoriteCount"));
                    }
                    Double AverageTopic1FavoriteCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalFavoriteCountTopic1 * 1.0 / TotalNumOfPostsTopic1));

                    for (int topic2 = 0; topic2 < 50; topic2++) {
                        Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                        TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                        ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                        Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                        Integer TotalFavoriteCountTopic2 = 0;

                        for (int i = 0; i < ScDocs2.length; ++i) {
                            int docId = ScDocs2[i].doc;
                            Document d = searcher.doc(docId);
                            TotalFavoriteCountTopic2 += Integer.parseInt(d.get("FavoriteCount"));
                        }
                        Double AverageTopic2FavoriteCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalFavoriteCountTopic2 * 1.0 / TotalNumOfPostsTopic2));

                        System.out.println(topic1 + "," + topic2 + "," + year + "," + (AverageTopic2FavoriteCount - AverageTopic1FavoriteCount));
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Feature F11 in paper
     */
    private void getTopicFeature21() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature21.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature21");
            for (int year = 2008; year < 2015; year++) {
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                    Integer TotalViewCountTopic2 = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        TotalViewCountTopic2 += Integer.parseInt(d.get("ViewCount"));
                    }
                    Double AverageTopic2ViewCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalViewCountTopic2 * 1.0 / TotalNumOfPostsTopic2));
                    System.out.println(topic2 + "," + year + "," + AverageTopic2ViewCount);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /*
        TopicFeature f = new TopicFeature();
        f.getTopicFeature1();
        f.getTopicFeature2();
        f.getTopicFeature3();
        f.getTopicFeature4();
        f.getTopicFeature5();
        f.getTopicFeature6();
        f.getTopicFeature7();
        f.getTopicFeature8();
        f.getTopicFeature9();
        f.getTopicFeature10();
        f.getTopicFeature11();
        f.getTopicFeature12();
        f.getTopicFeature13();
        f.getTopicFeature14();
        f.getTopicFeature15();
        f.getTopicFeature16();
        f.getTopicFeature17();
        f.getTopicFeature18();
        f.getTopicFeature19();
        f.getTopicFeature20();
        f.getTopicFeature21();
        f.getTopicFeature23();
        f.getTopicFeature24();
        f.getTopicFeature25();
        */
    }

    public void startFeatureCalculations() {
        getTopicFeature8();// F3
        getTopicFeature11();// F4
        getTopicFeature12();// F5
        getTopicFeature13();// F6
        getTopicFeature16();// F7
        getTopicFeature19();// F8
        getTopicFeature20();// F9
        getTopicFeature1();// F10
        getTopicFeature21();// F11
    }

    /******************* Not reported in paper **********************/
    /**
     * Popularity of topic2 (questions of year y)
     */
    private void getTopicFeature2() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature2.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature2");
            for (int year = 2008; year < 2015; year++) {
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Integer N_t = u.getDocCount(u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchPostTypeID(1)));
                    Integer N_at1_t = u.getDocCount(u.BooleanQueryAnd(u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchTopic(topic2)), u.SearchPostTypeID(1)));

                    double output = (1.0 * N_at1_t) / N_t;
                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Popularity of topic2 (answers of year y)
     */
    private void getTopicFeature3() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature3.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature3");
            for (int year = 2008; year < 2015; year++) {
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Integer N_t = u.getDocCount(u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchPostTypeID(2)));
                    Integer N_at1_t = u.getDocCount(u.BooleanQueryAnd(u.BooleanQueryAnd(u.SearchCreationDate(year), u.SearchTopic(topic2)), u.SearchPostTypeID(2)));

                    double output = (1.0 * N_at1_t) / N_t;
                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Average Score of topic2 (posts of years y)
     */
    private void getTopicFeature4() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature4.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature4");
            for (int year = 2008; year < 2015; year++) {
                Query exQ1 = u.SearchCreationDate(year);
                TopDocs hits1 = searcher.search(exQ1, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs1 = hits1.scoreDocs;
                Integer TotalNumOfPosts1 = ScDocs1.length;
                Integer TotalScore1 = 0;

                for (int i = 0; i < ScDocs1.length; ++i) {
                    int docId = ScDocs1[i].doc;
                    Document d = searcher.doc(docId);
                    TotalScore1 += Integer.parseInt(d.get("Score"));
                }
                Double AverageScore = (TotalNumOfPosts1 == 0 ? 0 : (TotalScore1 * 1.0 / TotalNumOfPosts1));

                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfPostsTag2 = ScDocs2.length;
                    Integer TotalScoreTag2 = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        TotalScoreTag2 += Integer.parseInt(d.get("Score"));
                    }
                    Double AverageTag2Score = (TotalNumOfPostsTag2 == 0 ? 0 : (TotalScoreTag2 * 1.0 / TotalNumOfPostsTag2));
                    Double output = (AverageScore == 0 ? 0 : (AverageTag2Score * 1.0 / AverageScore));
                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Average Score of topic2 (questions of years y)
     */
    private void getTopicFeature5() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature5.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature5");
            for (int year = 2008; year < 2015; year++) {
                Query exQ1 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.SearchCreationDate(year));
                TopDocs hits1 = searcher.search(exQ1, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs1 = hits1.scoreDocs;
                Integer TotalNumOfPosts1 = ScDocs1.length;
                Integer TotalScore1 = 0;

                for (int i = 0; i < ScDocs1.length; ++i) {
                    int docId = ScDocs1[i].doc;
                    Document d = searcher.doc(docId);
                    TotalScore1 += Integer.parseInt(d.get("Score"));
                }
                Double AverageScore = (TotalNumOfPosts1 == 0 ? 0 : (TotalScore1 * 1.0 / TotalNumOfPosts1));

                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfPostsTag2 = ScDocs2.length;
                    Integer TotalScoreTag2 = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        TotalScoreTag2 += Integer.parseInt(d.get("Score"));
                    }
                    Double AverageTag2Score = (TotalNumOfPostsTag2 == 0 ? 0 : (TotalScoreTag2 * 1.0 / TotalNumOfPostsTag2));
                    Double output = (AverageScore == 0 ? 0 : (AverageTag2Score * 1.0 / AverageScore));
                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Average Score of topic2 (answers of years y)
     */
    private void getTopicFeature6() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature6.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature6");
            for (int year = 2008; year < 2015; year++) {
                Query exQ1 = u.BooleanQueryAnd(u.SearchPostTypeID(2), u.SearchCreationDate(year));
                TopDocs hits1 = searcher.search(exQ1, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs1 = hits1.scoreDocs;
                Integer TotalNumOfPosts1 = ScDocs1.length;
                Integer TotalScore1 = 0;

                for (int i = 0; i < ScDocs1.length; ++i) {
                    int docId = ScDocs1[i].doc;
                    Document d = searcher.doc(docId);
                    TotalScore1 += Integer.parseInt(d.get("Score"));
                }
                Double AverageScore = (TotalNumOfPosts1 == 0 ? 0 : (TotalScore1 * 1.0 / TotalNumOfPosts1));

                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(2), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfPostsTag2 = ScDocs2.length;
                    Integer TotalScoreTag2 = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        TotalScoreTag2 += Integer.parseInt(d.get("Score"));
                    }
                    Double AverageTag2Score = (TotalNumOfPostsTag2 == 0 ? 0 : (TotalScoreTag2 * 1.0 / TotalNumOfPostsTag2));
                    Double output = (AverageScore == 0 ? 0 : (AverageTag2Score * 1.0 / AverageScore));
                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTopicFeature7() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature7.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,TopicFeature7");
            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    for (int topic2 = 0; topic2 < 50; topic2++) {
                        Query exQ = u.BooleanQueryAnd(u.SearchTopic(topic1), u.SearchCreationDate(year));
                        TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                        ScoreDoc[] ScDocs = hits.scoreDocs;

                        Integer TotalNumOfPostsTag1 = ScDocs.length;
                        Integer TotalScoreTag1 = 0;

                        for (int i = 0; i < ScDocs.length; ++i) {
                            int docId = ScDocs[i].doc;
                            Document d = searcher.doc(docId);
                            TotalScoreTag1 += Integer.parseInt(d.get("Score"));
                        }
                        Double AverageTag1Score = (TotalNumOfPostsTag1 == 0 ? 0 : (TotalScoreTag1 * 1.0 / TotalNumOfPostsTag1));

                        Query exQ2 = u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year));
                        TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                        ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                        Integer TotalNumOfPostsTag2 = ScDocs2.length;
                        Integer TotalScoreTag2 = 0;

                        for (int i = 0; i < ScDocs2.length; ++i) {
                            int docId = ScDocs2[i].doc;
                            Document d = searcher.doc(docId);
                            TotalScoreTag2 += Integer.parseInt(d.get("Score"));
                        }
                        Double AverageTag2Score = (TotalNumOfPostsTag2 == 0 ? 0 : (TotalScoreTag2 * 1.0 / TotalNumOfPostsTag2));

                        System.out.println(topic1 + "," + topic2 + "," + year + "," + (AverageTag2Score - AverageTag1Score));
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTopicFeature9() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature9.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature9");
            for (int year = 2008; year < 2015; year++) {
                Query exQ1 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.SearchCreationDate(year));
                TopDocs hits1 = searcher.search(exQ1, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs1 = hits1.scoreDocs;

                Integer TotalNumOfPostsTopic1 = ScDocs1.length;
                Integer TotalViewCountTopic1 = 0;

                for (int i = 0; i < ScDocs1.length; ++i) {
                    int docId = ScDocs1[i].doc;
                    Document d = searcher.doc(docId);
                    TotalViewCountTopic1 += Integer.parseInt(d.get("ViewCount"));
                }
                Double AverageViewCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalViewCountTopic1 * 1.0 / TotalNumOfPostsTopic1));

                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                    Integer TotalViewCountTopic2 = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        TotalViewCountTopic2 += Integer.parseInt(d.get("ViewCount"));
                    }
                    Double AverageTopic2ViewCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalViewCountTopic2 * 1.0 / TotalNumOfPostsTopic2));
                    Double output = (AverageViewCount == 0 ? 0 : (AverageTopic2ViewCount * 1.0 / AverageViewCount));
                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTopicFeature10() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature10.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,TopicFeature10");
            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    for (int topic2 = 0; topic2 < 50; topic2++) {
                        Query exQ = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic1), u.SearchCreationDate(year)));
                        TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                        ScoreDoc[] ScDocs = hits.scoreDocs;

                        Integer TotalNumOfPostsTopic1 = ScDocs.length;
                        Integer TotalViewCountTopic1 = 0;

                        for (int i = 0; i < ScDocs.length; ++i) {
                            int docId = ScDocs[i].doc;
                            Document d = searcher.doc(docId);
                            TotalViewCountTopic1 += Integer.parseInt(d.get("ViewCount"));
                        }
                        Double AverageTopic1ViewCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalViewCountTopic1 * 1.0 / TotalNumOfPostsTopic1));

                        Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                        TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                        ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                        Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                        Integer TotalViewCountTopic2 = 0;

                        for (int i = 0; i < ScDocs2.length; ++i) {
                            int docId = ScDocs2[i].doc;
                            Document d = searcher.doc(docId);
                            TotalViewCountTopic2 += Integer.parseInt(d.get("ViewCount"));
                        }
                        Double AverageTopic2ViewCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalViewCountTopic2 * 1.0 / TotalNumOfPostsTopic2));

                        System.out.println(topic1 + "," + topic2 + "," + year + "," + (AverageTopic2ViewCount - AverageTopic1ViewCount));
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTopicFeature14() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature14.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature14");
            for (int year = 2008; year < 2015; year++) {
                Query exQ1 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.SearchCreationDate(year));
                TopDocs hits1 = searcher.search(exQ1, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs1 = hits1.scoreDocs;

                Integer TotalNumOfPostsTopic1 = ScDocs1.length;
                Integer TotalCommentCountTopic1 = 0;

                for (int i = 0; i < ScDocs1.length; ++i) {
                    int docId = ScDocs1[i].doc;
                    Document d = searcher.doc(docId);
                    TotalCommentCountTopic1 += Integer.parseInt(d.get("CommentCount"));
                }
                Double AverageCommentCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalCommentCountTopic1 * 1.0 / TotalNumOfPostsTopic1));
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                    Integer TotalCommentCountTopic2 = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        TotalCommentCountTopic2 += Integer.parseInt(d.get("CommentCount"));
                    }
                    Double AverageTopic2CommentCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalCommentCountTopic2 * 1.0 / TotalNumOfPostsTopic2));
                    Double output = (AverageCommentCount == 0 ? 0 : (AverageTopic2CommentCount * 1.0 / AverageCommentCount));

                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTopicFeature15() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature15.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature15");
            for (int year = 2008; year < 2015; year++) {
                Query exQ1 = u.BooleanQueryAnd(u.SearchPostTypeID(2), u.SearchCreationDate(year));
                TopDocs hits1 = searcher.search(exQ1, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs1 = hits1.scoreDocs;

                Integer TotalNumOfPostsTopic1 = ScDocs1.length;
                Integer TotalCommentCountTopic1 = 0;

                for (int i = 0; i < ScDocs1.length; ++i) {
                    int docId = ScDocs1[i].doc;
                    Document d = searcher.doc(docId);
                    TotalCommentCountTopic1 += Integer.parseInt(d.get("CommentCount"));
                }
                Double AverageCommentCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalCommentCountTopic1 * 1.0 / TotalNumOfPostsTopic1));
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(2), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                    Integer TotalCommentCountTopic2 = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        TotalCommentCountTopic2 += Integer.parseInt(d.get("CommentCount"));
                    }
                    Double AverageTopic2CommentCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalCommentCountTopic2 * 1.0 / TotalNumOfPostsTopic2));
                    Double output = (AverageCommentCount == 0 ? 0 : (AverageTopic2CommentCount * 1.0 / AverageCommentCount));

                    System.out.println(topic2 + "," + year + "," + output);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTopicFeature17() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature17.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,TopicFeature17");
            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    Query exQ = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic1), u.SearchCreationDate(year)));
                    TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs = hits.scoreDocs;

                    Integer TotalNumOfPostsTopic1 = ScDocs.length;
                    Integer TotalCommentCountTopic1 = 0;

                    for (int i = 0; i < ScDocs.length; ++i) {
                        int docId = ScDocs[i].doc;
                        Document d = searcher.doc(docId);
                        TotalCommentCountTopic1 += Integer.parseInt(d.get("CommentCount"));
                    }
                    Double AverageTopic1CommentCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalCommentCountTopic1 * 1.0 / TotalNumOfPostsTopic1));

                    for (int topic2 = 0; topic2 < 50; topic2++) {
                        Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                        TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                        ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                        Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                        Integer TotalCommentCountTopic2 = 0;

                        for (int i = 0; i < ScDocs2.length; ++i) {
                            int docId = ScDocs2[i].doc;
                            Document d = searcher.doc(docId);
                            TotalCommentCountTopic2 += Integer.parseInt(d.get("CommentCount"));
                        }
                        Double AverageTopic2CommentCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalCommentCountTopic2 * 1.0 / TotalNumOfPostsTopic2));

                        System.out.println(topic1 + "," + topic2 + "," + year + "," + (AverageTopic2CommentCount - AverageTopic1CommentCount));
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTopicFeature18() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature18.txt"));
            System.setOut(out);

            System.out.println("Topic1,Topic2,Year,TopicFeature18");
            for (int year = 2008; year < 2015; year++) {
                for (int topic1 = 0; topic1 < 50; topic1++) {
                    Query exQ = u.BooleanQueryAnd(u.SearchPostTypeID(2), u.BooleanQueryAnd(u.SearchTopic(topic1), u.SearchCreationDate(year)));
                    TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs = hits.scoreDocs;

                    Integer TotalNumOfPostsTopic1 = ScDocs.length;
                    Integer TotalCommentCountTopic1 = 0;

                    for (int i = 0; i < ScDocs.length; ++i) {
                        int docId = ScDocs[i].doc;
                        Document d = searcher.doc(docId);
                        TotalCommentCountTopic1 += Integer.parseInt(d.get("CommentCount"));
                    }
                    Double AverageTopic1CommentCount = (TotalNumOfPostsTopic1 == 0 ? 0 : (TotalCommentCountTopic1 * 1.0 / TotalNumOfPostsTopic1));

                    for (int topic2 = 0; topic2 < 50; topic2++) {
                        Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(2), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                        TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                        ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                        Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                        Integer TotalCommentCountTopic2 = 0;

                        for (int i = 0; i < ScDocs2.length; ++i) {
                            int docId = ScDocs2[i].doc;
                            Document d = searcher.doc(docId);
                            TotalCommentCountTopic2 += Integer.parseInt(d.get("CommentCount"));
                        }
                        Double AverageTopic2CommentCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalCommentCountTopic2 * 1.0 / TotalNumOfPostsTopic2));

                        System.out.println(topic1 + "," + topic2 + "," + year + "," + (AverageTopic2CommentCount - AverageTopic1CommentCount));
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTopicFeature22() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature22.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature22");
            for (int year = 2008; year < 2015; year++) {
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfPostsTopic2 = ScDocs2.length;
                    Integer TotalAnswerCountTopic2 = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        TotalAnswerCountTopic2 += Integer.parseInt(d.get("AnswerCount"));
                    }
                    Double AverageTopic2AnswerCount = (TotalNumOfPostsTopic2 == 0 ? 0 : (TotalAnswerCountTopic2 * 1.0 / TotalNumOfPostsTopic2));
                    System.out.println(topic2 + "," + year + "," + AverageTopic2AnswerCount);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTopicFeature23() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature23.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature23");
            for (int year = 2008; year < 2015; year++) {
                HashMap<Integer, Integer> TopicCounts = new HashMap<Integer, Integer>();// key=Topic , value=OccurenceCount
                Query exQ2 = u.SearchCreationDate(year);
                TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                Integer TotalTopic_Occurence = 0;
                for (int i = 0; i < ScDocs2.length; ++i) {
                    int docId = ScDocs2[i].doc;
                    Document d = searcher.doc(docId);
                    for (IndexableField t : d.getFields("Topics")) {
                        Integer topic = Integer.parseInt(t.stringValue());
                        if (!topic.equals(-1)) {
                            TotalTopic_Occurence++;
                            if (TopicCounts.containsKey(topic)) {
                                TopicCounts.put(topic, TopicCounts.get(topic) + 1);
                            } else {
                                TopicCounts.put(topic, 1);
                            }
                        }
                    }
                }
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    if (TopicCounts.containsKey(topic2)) {
                        System.out.println(topic2 + "," + year + "," + (TopicCounts.get(topic2) * 1.0 / TotalTopic_Occurence));
                    } else {
                        System.out.println(topic2 + "," + year + "," + 0);
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * For each topic t and year y: |Question of topic t in year y with have accepted answer|/|Question of topic t in year y|
     */
    private void getTopicFeature24() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature24.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature24");
            for (int year = 2008; year < 2015; year++) {
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    Query exQ2 = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.BooleanQueryAnd(u.SearchTopic(topic2), u.SearchCreationDate(year)));
                    TopDocs hits2 = searcher.search(exQ2, Integer.MAX_VALUE);
                    ScoreDoc[] ScDocs2 = hits2.scoreDocs;

                    Integer TotalNumOfQuestionsInTopic2 = ScDocs2.length;
                    Integer TotalNumOfQuestionsInTopic2WithAcceptedAnswer = 0;

                    for (int i = 0; i < ScDocs2.length; ++i) {
                        int docId = ScDocs2[i].doc;
                        Document d = searcher.doc(docId);
                        if (Integer.parseInt(d.get("AcceptedAnswerId")) != -1)
                            TotalNumOfQuestionsInTopic2WithAcceptedAnswer++;
                    }
                    Double AcceptancePrecision = (TotalNumOfQuestionsInTopic2 == 0 ? 0 : (TotalNumOfQuestionsInTopic2WithAcceptedAnswer * 1.0 / TotalNumOfQuestionsInTopic2));
                    System.out.println(topic2 + "," + year + "," + AcceptancePrecision);
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * For each topic t and year y: |Question of topic t in year y with have accepted answer|/|Question in year y with have accepted answer|
     */
    private void getTopicFeature25() {
        try {
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream(Constants.TopicFeatureDirectory + "TopicFeature25.txt"));
            System.setOut(out);

            System.out.println("Topic,Year,TopicFeature25");
            for (int year = 2008; year < 2015; year++) {
                Query exQ = u.BooleanQueryAnd(u.SearchPostTypeID(1), u.SearchCreationDate(year));
                TopDocs hits = searcher.search(exQ, Integer.MAX_VALUE);
                ScoreDoc[] ScDocs = hits.scoreDocs;

                Integer TotalNumOfQuestionsInWithAcceptedAnswer = 0;
                HashMap<Integer, Integer> TopicAcceptedAnswerCounts = new HashMap<Integer, Integer>();// key=Topic , value=AcceptedAnswerCount

                for (int i = 0; i < ScDocs.length; ++i) {
                    int docId = ScDocs[i].doc;
                    Document d = searcher.doc(docId);
                    if (Integer.parseInt(d.get("AcceptedAnswerId")) != -1) {
                        TotalNumOfQuestionsInWithAcceptedAnswer++;
                        for (IndexableField t : d.getFields("Topics")) {
                            Integer topic = Integer.parseInt(t.stringValue());
                            if (!topic.equals(-1)) {
                                if (TopicAcceptedAnswerCounts.containsKey(topic)) {
                                    TopicAcceptedAnswerCounts.put(topic, TopicAcceptedAnswerCounts.get(topic) + 1);
                                } else {
                                    TopicAcceptedAnswerCounts.put(topic, 1);
                                }
                            }
                        }
                    }
                }
                for (int topic2 = 0; topic2 < 50; topic2++) {
                    if (TopicAcceptedAnswerCounts.containsKey(topic2)) {
                        System.out.println(topic2 + "," + year + "," + (TopicAcceptedAnswerCounts.get(topic2) * 1.0 / TotalNumOfQuestionsInWithAcceptedAnswer));
                    } else {
                        System.out.println(topic2 + "," + year + "," + 0);
                    }
                }
            }
            System.setOut(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
