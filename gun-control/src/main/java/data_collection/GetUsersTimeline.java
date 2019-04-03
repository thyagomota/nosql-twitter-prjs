package data_collection;

import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.*;
import twitter4j.*;
import twitter4j.conf.*;
import twitter4j.json.DataObjectFactory;

import java.io.*;
import java.util.*;

/**
 * This class collects users timeline using the search API.
 *
 * @date 2018-08-01
 * @version 1.0
 * @author Thyago Mota
 */
public class GetUsersTimeline {

    /**
     * Twitter's rate limit control: backoff procedure to be executed when receive a Twitter rate limiting error.
     */
    static void rateLimitBackoff(final TwitterException ex) {
        System.out.println("Rate limit started...");
        RateLimitStatus rateLimitStatus = ex.getRateLimitStatus();
        int secondsUntilReset = rateLimitStatus.getSecondsUntilReset();
        try {
            Thread.sleep((secondsUntilReset + 5) * 1000);
        }
        catch (InterruptedException interruptedEx) { }
        System.out.println("Rate limit was lifted!");
    }

    public static void main(String[] args) throws IOException {

        // read properties files
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties prop = new Properties();
        prop.load(loader.getResourceAsStream("gun-control.properties"));

        // connect to MongoDB database and get a reference to the collections
        MongoClient mongoClient = new MongoClient(prop.getProperty("DB_SERVER"), Integer.parseInt(prop.getProperty("DB_PORT")));
        MongoDatabase guncontrol = mongoClient.getDatabase(prop.getProperty("DB_NAME"));
        MongoCollection usersCollection = guncontrol.getCollection("users");

        // configure credentials
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(prop.getProperty("SEARCH_API_CONSUMER_KEY"));
        cb.setOAuthConsumerSecret(prop.getProperty("SEARCH_API_CONSUMER_SECRET"));
        cb.setOAuthAccessToken(prop.getProperty("SEARCH_API_ACCESS_TOKEN"));
        cb.setOAuthAccessTokenSecret(prop.getProperty("SEARCH_API_ACCESS_TOKEN_SECRET"));
        cb.setJSONStoreEnabled(true);
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();

        // get preemptive backoff time in ms
        int preemptiveBackoff = Integer.parseInt(prop.getProperty("SEARCH_API_BACKOFF_TIME")) * 1000;

        // query users without timeline info
        MongoCursor<Document> cursor = usersCollection.find(Document.parse("{ timeline: { $exists: false } }")).noCursorTimeout(true).iterator();
        while (cursor.hasNext()) {
            Document userDoc = cursor.next();
            long userId = userDoc.getLong("id");
            Paging paging = new Paging();
            paging.setCount(200);
            List<String> timeline = new LinkedList<String>();

            try {
                ResponseList<Status> statuses = twitter.getUserTimeline(userId, paging);
                for (Status status : statuses)
                    timeline.add(TwitterObjectFactory.getRawJSON(status));
            } catch (TwitterException ex) {
                if (ex.exceededRateLimitation())
                    rateLimitBackoff(ex);
            }

            try {
                usersCollection.updateOne(Document.parse("{ id: " + userId + " }"), Document.parse("{ $set: { \"timeline\": " + timeline + "} }"));
            }
            catch (MongoException ex) {
                // ignore DB errors for now...
            }

            // before starting a new user, do a preemptive backoff...
            try {
                System.out.println("Preemptive backoff...");
                Thread.sleep(preemptiveBackoff);
                System.out.println("done!");
            }
            catch (InterruptedException ex) {

            }
        } // end while (all users)
    } // end main
}
