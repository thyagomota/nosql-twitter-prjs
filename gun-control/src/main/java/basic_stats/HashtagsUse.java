package basic_stats;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import model.Hashtags;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bson.Document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HashtagsUse {

    private static final String OUTPUT_HASHTAGS_USE  = "hashtags-use.csv";


    public static void main(String[] args) throws IOException {

        // read properties files
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties prop = new Properties();
        prop.load(loader.getResourceAsStream("gun-control.properties"));

        // Map with the hashtags and counters
        Map<String, Integer> hashtagsCounters = new HashMap<String, Integer>();

        // connect to MongoDB database
        MongoClient mongoClient = new MongoClient(prop.getProperty("DB_SERVER"), Integer.parseInt(prop.getProperty("DB_PORT")));
        MongoDatabase guncontrol = mongoClient.getDatabase(prop.getProperty("DB_NAME"));
        MongoCollection userCollection = guncontrol.getCollection("users");

        // read all user info
        MongoCursor<Document> cursor = userCollection.find().noCursorTimeout(true).iterator();
        int totalProcessed = 0;
        while (cursor.hasNext()) {
            Document userDoc = cursor.next();
            List<Document> timeline = (List<Document>) userDoc.get("timeline");

            for (Document tweet: timeline) {
                boolean isGunControlTweet = false;
                Document entities = (Document) tweet.get("entities");
                for (Document hashtagDoc: (List<Document>) entities.get("hashtags")) {
                    String hashtag = hashtagDoc.getString("text");
                    if (Hashtags.contains(hashtag)) {
                        isGunControlTweet = true;
                        break;
                    }
                }

                if (isGunControlTweet)
                    for (Document hashtagDoc: (List<Document>) entities.get("hashtags")) {
                        String hashtag = hashtagDoc.getString("text");
                        hashtag = hashtag.toLowerCase();
                        if (Hashtags.contains(hashtag)) {
                            if (!hashtagsCounters.containsKey(hashtag))
                                hashtagsCounters.put(hashtag, 1);
                            else
                                hashtagsCounters.put(hashtag, hashtagsCounters.get(hashtag) + 1);
                        }
                    }

            } // end for

            totalProcessed++;
            if (totalProcessed % 1000 == 0)
                System.out.println(totalProcessed + " users processed!");
        }
        mongoClient.close();

        // write info to a file
        PrintStream out = new PrintStream(new FileOutputStream(OUTPUT_HASHTAGS_USE));
        for (String hashtag: hashtagsCounters.keySet())
            out.println(hashtag + ";" + hashtagsCounters.get(hashtag));
        out.close();
    }
}
