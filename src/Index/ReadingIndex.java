package Index;

import Utility.Constants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Created by Zohreh on 6/18/2017.
 * Just for test!
 */

public class ReadingIndex {
    public static void main(String[] args) throws IOException, ParseException {
        PrintStream stdout = System.out;
        try {
            PrintStream out = new PrintStream(new FileOutputStream("ReadingIndex.txt"));
            System.setOut(out);
        } catch (IOException e) {
            System.out.println("\n\n\n\nSorry!\n\n\n\n");
        }

        long start = new Date().getTime();
        ReadingIndex s = new ReadingIndex();
        long end = new Date().getTime();
        System.out.println("Searching took " + (end - start) + " milliseconds");

    }

    public ReadingIndex() {
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(Constants.IndexDirectory)));

            //System.out.println(reader.maxDoc());
            int c = 0;
            for (int i = 0; i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                System.out.println("Id=" + doc.get("Id") + " PostTypeId=" + doc.get("PostTypeId") + " ParentId=" + doc.get("ParentId") +
                        " AcceptedAnswerId=" + doc.get("AcceptedAnswerId") + " CreationDate=" + doc.get("CreationDate") + " Score=" +
                        doc.get("Score") + " ViewCount=" + doc.get("ViewCount") + " Body=" + doc.get("Body") + " OwnerUserId=" + doc.get("OwnerUserId") +
                        " OwnerDisplayName=" + doc.get("OwnerDisplayName") + " LastEditorUserId=" + doc.get("LastEditorUserId") + " LastEditorDisplayName=" +
                        doc.get("LastEditorDisplayName") + " LastEditDate=" + doc.get("LastEditDate") + " LastActivityDate=" + doc.get("LastActivityDate")
                        + " ClosedDate=" + doc.get("ClosedDate") + " Title=" + doc.get("Title") + " AnswerCount=" + doc.get("AnswerCount") +
                        " CommentCount=" + doc.get("CommentCount") + " FavoriteCount=" + doc.get("FavoriteCount") + " CommunityOwnedDate=" + doc.get("CommunityOwnedDate"));
                c = c + 1;

                System.out.println("Tags: ");
                for (IndexableField tag : doc.getFields("Tags")) {
                    System.out.println(tag.stringValue());
                }

                System.out.println("Topics: ");
                for (IndexableField topic : doc.getFields("Topics")) {
                    System.out.println(topic.stringValue());
                }

                /*Terms terms = reader.getTermVector(i, "Body"); //get terms vectors for one document and one field
                System.out.println("Body Terms:");
                if (terms != null && terms.size() > 0) {
                    TermsEnum termsEnum = terms.iterator(); // access the terms for this field
                    BytesRef term = null;
                    while ((term = termsEnum.next()) != null) {
                        final String keyword = term.utf8ToString();
                        long termFreq = termsEnum.totalTermFreq();
                        System.out.println("term: " + keyword + ", termFreq = " + termFreq);
                    }
                }

                Terms terms2 = reader.getTermVector(i, "Title"); //get terms vectors for one document and one field
                System.out.println("Title Terms:");
                if (terms2 != null && terms2.size() > 0) {
                    TermsEnum termsEnum = terms2.iterator(); // access the terms for this field
                    BytesRef t = null;
                    while ((t = termsEnum.next()) != null) {
                        final String keyword = t.utf8ToString();
                        long termFreq = termsEnum.totalTermFreq();
                        System.out.println("term: " + keyword + ", termFreq = " + termFreq);
                    }
                }*/

            }
            //System.out.println("TotalNum=" + c);

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
