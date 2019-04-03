/**
 * nosql-twitter - Spanish Elections Project
 * This program preprocess a "tweets-all" collection, making sure all tweets are originated from a set of predefined accounts
 * @author Thyago Mota
 * @date Jan-8-2019
 */

package preprocessing;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class FilterByAccount {

    private static final String INPUT_USERS_COLLECTION = "tweets-all";
    private static final String OUTPUT_USERS_COLLECTION = "tweets";
    private static final String SPECIAL_CHARACTERS_REGEX = "[\"\\.\\*\\[\\]\\(\\)\\`~!@#\\$%\\^&_\\-\\+\\=;:'<>,\\?/]";

    public static void main(String[] args) throws IOException {
        // read properties files
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties prop = new Properties();
        prop.load(loader.getResourceAsStream("spanish-elections.properties"));

        // load accounts
        String accounts[] = prop.getProperty("ACCOUNTS").split(",");

        // connect to MongoDB database
        MongoClient mongoClient = new MongoClient(prop.getProperty("DB_SERVER"), Integer.parseInt(prop.getProperty("DB_PORT")));
        MongoDatabase polprt = mongoClient.getDatabase(prop.getProperty("DB_NAME"));

        // open collections
        MongoCollection inputUsersCollection = polprt.getCollection(INPUT_USERS_COLLECTION);
        MongoCollection outputUsersCollection = polprt.getCollection(OUTPUT_USERS_COLLECTION);

        // process
        int totalInserts = 0, totalProcessed = 0;
        MongoCursor<Document> cursor = inputUsersCollection.find().noCursorTimeout(true).iterator();
        while (cursor.hasNext()) {
            totalProcessed++;
            if (totalProcessed % 50000 == 0)
                System.out.println(totalProcessed + " tweets processed!");
            Document tweet = cursor.next();
            if (tweet.containsKey("user")) {
                Document user = (Document) tweet.get("user");
                if (user.containsKey("screen_name")) {
                    String screenName = user.getString("screen_name").toLowerCase();
                    boolean found = false;
                    for (int i = 0; i < accounts.length; i++)
                        if (screenName.equalsIgnoreCase(accounts[i])) {
                            found = true;
                            break;
                        }
                    if (found) {
                        String text = tweet.getString("text");
//                        System.out.println(text);
                        text = text.replaceAll(SPECIAL_CHARACTERS_REGEX, " ");
                        text = text.replaceAll("\\s+", " ");
//                        System.out.println(text);
                        tweet.put("text", text);
                        outputUsersCollection.insertOne(tweet);
                        totalInserts++;
                    }
                }
            }
        }

        // finish
        mongoClient.close();
        System.out.println(totalInserts + " tweets inserted!");
    }
}