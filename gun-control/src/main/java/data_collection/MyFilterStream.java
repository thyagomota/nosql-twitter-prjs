package data_collection;

import com.mongodb.*;
import com.mongodb.client.*;
import model.Hashtags;
import twitter4j.*;
import twitter4j.auth.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.*;

/**
 * This class, together with MyStatusListener, implements the user profile data collection, filtering users that post using one of the terms in "hashtags.txt"
 *
 *  @date 2018-08-01
 *  @version 1.0
 *  @author Thyago Mota
 */
public class MyFilterStream {

    public static void main(String[] args) throws IOException {

        // read properties files
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties prop = new Properties();
        prop.load(loader.getResourceAsStream("gun-control.properties"));

        // connect to MongoDB database
        MongoClient mongoClient = new MongoClient(prop.getProperty("DB_SERVER"), Integer.parseInt(prop.getProperty("DB_PORT")));
        MongoDatabase guncontrol = mongoClient.getDatabase(prop.getProperty("DB_NAME"));

        // Twitter authentication
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(prop.getProperty("FILTER_STREAM_CONSUMER_KEY"));
        cb.setOAuthConsumerSecret(prop.getProperty("FILTER_STREAM_CONSUMER_SECRET"));
        cb.setOAuthAccessToken(prop.getProperty("FILTER_STREAM_ACCESS_TOKEN"));
        cb.setOAuthAccessTokenSecret(prop.getProperty("FILTER_STREAM_ACCESS_TOKEN_SECRET"));
        // cb.setJSONStoreEnabled(true);
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        // add a listener to the stream
        twitterStream.addListener(new MyStatusListener(guncontrol));

        // create a filter query based on terms and start listening to the stream
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.track(Hashtags.getHashtags().toArray(new String[1]));
        twitterStream.filter(filterQuery);
    }
}
