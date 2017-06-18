package Index;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.apache.lucene.document.*;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Zohreh on 6/18/2017.
 */
public class Post {
    SimpleDateFormat formatter;
    public Integer Id;
    public Integer PostTypeId;
    public Integer ParentId;
    public Integer AcceptedAnswerId;
    public Date CreationDate;
    public Integer Score;
    public Integer ViewCount;
    public String Body;
    public Integer OwnerUserId;
    public String OwnerDisplayName;
    public Integer LastEditorUserId;
    public String LastEditorDisplayName;
    public Date LastEditDate;
    public Date LastActivityDate;
    public Date ClosedDate;
    public String Title;
    public ArrayList<String> Tags;
    public ArrayList<Integer> Topics;
    public Integer AnswerCount;
    public Integer CommentCount;
    public Integer FavoriteCount;
    public Date CommunityOwnedDate;

    public Post(String xmlLine) {
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        Elements row = Jsoup.parse(xmlLine).getElementsByTag("row");
        Id = getIntegerValue(row, "Id");
        PostTypeId = getIntegerValue(row, "PostTypeId");
        ParentId = getIntegerValue(row, "ParentId");
        AcceptedAnswerId = getIntegerValue(row, "AcceptedAnswerId");
        CreationDate = getDateValue(row, "CreationDate");
        Score = getIntegerValue(row, "Score");
        ViewCount = getIntegerValue(row, "ViewCount");
        Body = getStringValue(row, "Body");
        OwnerUserId = getIntegerValue(row, "OwnerUserId");
        OwnerDisplayName = getStringValue(row, "OwnerDisplayName");
        LastEditorUserId = getIntegerValue(row, "LastEditorUserId");
        LastEditorDisplayName = getStringValue(row, "LastEditorDisplayName");
        LastEditDate = getDateValue(row, "LastEditDate");
        LastActivityDate = getDateValue(row, "LastActivityDate");
        ClosedDate = getDateValue(row, "ClosedDate");
        Title = getStringValue(row, "Title");
        Tags = getStringList(row, "Tags");
        Topics = getTopicSet(row);
        AnswerCount = getIntegerValue(row, "AnswerCount");
        CommentCount = getIntegerValue(row, "CommentCount");
        FavoriteCount = getIntegerValue(row, "FavoriteCount");
        CommunityOwnedDate = getDateValue(row, "CommunityOwnedDate");
    }

    private ArrayList<String> getStringList(Elements row, String tag) {
        ArrayList<String> out = new ArrayList<>();
        String[] ss = row.attr(tag).split("<|>");
        for (String s : ss) {
            if (s != null && s.trim().length() > 0)
                out.add(s.trim());
        }
        return out;
    }

    private ArrayList<Integer> getTopicSet(Elements row) {
        ArrayList<Integer> out = new ArrayList<Integer>();
        String[] ss = row.attr("Topics").split("#");
        for (String s : ss) {
            if (s != null && s.trim().length() > 0)
                out.add(Integer.parseInt(s.trim()));
        }
        return out;
    }

    private String getStringValue(Elements row, String tag) {
        try {
            String html = row.attr(tag);
            html = html.replace("&lt;", "<").replace("&gt;", ">");
            return Jsoup.parse(html).text();
        } catch (Exception e) {
            return null;
        }
    }

    private Date getDateValue(Elements row, String tag) {
        Date date;
        try {
            XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(row.attr(tag));
            Calendar c3 = cal.toGregorianCalendar();
            c3.clear(Calendar.MINUTE);
            c3.clear(Calendar.HOUR);
            c3.clear(Calendar.SECOND);
            c3.clear(Calendar.MILLISECOND);
            date = c3.getTime();
        } catch (Exception e) {
            date = null;
        }
        return date;
    }

    private Integer getIntegerValue(Elements row, String tag) {
        Integer value;
        try {
            value = Integer.parseInt(row.attr(tag));
        } catch (Exception e) {
            value = null;
        }
        return value;
    }

    public Document getLuceneDocument() {
        Document doc = new Document();

        doc.add(new IntField("Id", Id != null ? Id : -1, Field.Store.YES));
        doc.add(new IntField("PostTypeId", PostTypeId != null ? PostTypeId : -1, Field.Store.YES));
        doc.add(new IntField("ParentId", ParentId != null ? ParentId : -1, Field.Store.YES));
        doc.add(new IntField("AcceptedAnswerId", AcceptedAnswerId != null ? AcceptedAnswerId : -1, Field.Store.YES));
        doc.add(new LongField("CreationDate", CreationDate != null ? CreationDate.getTime() : -1, Field.Store.YES));
        doc.add(new IntField("Score", Score != null ? Score : -1, Field.Store.YES));
        doc.add(new IntField("ViewCount", ViewCount != null ? ViewCount : -1, Field.Store.YES));
        doc.add(new TextField("Body", Body, Field.Store.NO));
        doc.add(new IntField("OwnerUserId", OwnerUserId != null ? OwnerUserId : -1, Field.Store.YES));
        doc.add(new StringField("OwnerDisplayName", OwnerDisplayName != null ? OwnerDisplayName : "", Field.Store.YES));
        doc.add(new IntField("LastEditorUserId", LastEditorUserId != null ? LastEditorUserId : -1, Field.Store.YES));
        doc.add(new StringField("LastEditorDisplayName", LastEditorDisplayName != null ? LastEditorDisplayName : "", Field.Store.YES));

        doc.add(new LongField("LastEditDate", LastEditDate != null ? LastEditDate.getTime() : -1, Field.Store.YES));
        doc.add(new LongField("LastActivityDate", LastActivityDate != null ? LastActivityDate.getTime() : -1, Field.Store.YES));
        doc.add(new LongField("ClosedDate", ClosedDate != null ? ClosedDate.getTime() : -1, Field.Store.YES));
        doc.add(new TextField("Title", Title, Field.Store.NO));

        if (Tags.size() == 0)
            doc.add(new StringField("Tags", "", Field.Store.YES));
        else {
            for (String tag : Tags)
                doc.add(new StringField("Tags", tag, Field.Store.YES));
        }

        if (Topics.size() == 0)
            doc.add(new IntField("Topics", -1, Field.Store.YES));
        else {
            for (Integer topicId : Topics)
                doc.add(new IntField("Topics", topicId, Field.Store.YES));
        }

        doc.add(new IntField("AnswerCount", AnswerCount != null ? AnswerCount : -1, Field.Store.YES));
        doc.add(new IntField("CommentCount", CommentCount != null ? CommentCount : -1, Field.Store.YES));
        doc.add(new IntField("FavoriteCount", FavoriteCount != null ? FavoriteCount : -1, Field.Store.YES));
        doc.add(new LongField("CommunityOwnedDate", CommunityOwnedDate != null ? CommunityOwnedDate.getTime() : -1, Field.Store.YES));

        return doc;
    }
}


