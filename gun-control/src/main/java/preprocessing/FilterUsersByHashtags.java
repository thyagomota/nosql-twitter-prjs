package preprocessing;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import model.Hashtags;
import org.bson.Document;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * For the users that have timeline, filter the ones that have a significant number of gun-control hashtags.
 *
 *  @date 2018-09-13
 *  @version 1.0
 *  @author Thyago Mota
 */
public class FilterUsersByHashtags {

    private static final String INPUT_USERS_COLLECTION  = "users-jan31";
    private static final String OUTPUT_USERS_COLLECTION = "users";
    private static final int    MIN_GUN_CONTROL_TWEETS  = 5;

    public static void main(String[] args) throws IOException {

        // read properties files
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties prop = new Properties();
        prop.load(loader.getResourceAsStream("gun-control.properties"));

        // connect to MongoDB database
        MongoClient mongoClient = new MongoClient(prop.getProperty("DB_SERVER"), Integer.parseInt(prop.getProperty("DB_PORT")));
        MongoDatabase guncontrol = mongoClient.getDatabase(prop.getProperty("DB_NAME"));

        // open collections
        MongoCollection inputUsersCollection  = guncontrol.getCollection(INPUT_USERS_COLLECTION);
        MongoCollection outputUsersCollection = guncontrol.getCollection(OUTPUT_USERS_COLLECTION);

        // preprocessing procedure starts now
        int totalProcessed = 0;
        MongoCursor<Document> cursor = inputUsersCollection.find(Document.parse("{ timeline: { $exists: true } }")).noCursorTimeout(true).iterator();

        while (cursor.hasNext()) {
            Document userDoc = cursor.next();

            List<Document> timeline = (List<Document>) userDoc.get("timeline");

            int gunControlTweets = 0;
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
                    gunControlTweets++;
            } // end for

            if (gunControlTweets >= MIN_GUN_CONTROL_TWEETS)
                outputUsersCollection.insertOne(userDoc);

            totalProcessed++;
            if (totalProcessed % 1000 == 0)
                System.out.println(totalProcessed + " users processed!");
        }

        mongoClient.close();
    }
}
