package analysis;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.util.Properties;

public class UsersGeoLocation {

    private static final String OUTPUT_HASHTAGS_USE  = "hashtags-use.csv";


    public static void main(String[] args) throws IOException {

        // read properties files
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties prop = new Properties();
        prop.load(loader.getResourceAsStream("gun-control.properties"));


        // connect to MongoDB database
        MongoClient mongoClient = new MongoClient(prop.getProperty("DB_SERVER"), Integer.parseInt(prop.getProperty("DB_PORT")));
        MongoDatabase guncontrol = mongoClient.getDatabase(prop.getProperty("DB_NAME"));
        MongoCollection userCollection = guncontrol.getCollection("users");

        // read all user info
        MongoCursor<Document> cursor = userCollection.find().noCursorTimeout(true).iterator();
        int totalProcessed = 0;
        while (cursor.hasNext()) {
            Document userDoc = cursor.next();
            totalProcessed++;
            if (totalProcessed % 1000 == 0)
                System.out.println(totalProcessed + " users processed!");
        }
        mongoClient.close();

        // write info to a file
//        PrintStream out = new PrintStream(new FileOutputStream(OUTPUT_HASHTAGS_USE));
//        for (String hashtag: hashtagsCounters.keySet())
//            out.println(hashtag + ";" + hashtagsCounters.get(hashtag));
//        out.close();
    }
}
