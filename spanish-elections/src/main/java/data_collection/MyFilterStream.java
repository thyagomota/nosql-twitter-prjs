/**
 * nosql-twitter - Spanish Elections Project
 * This filter stream configures a real time Twitter data crawler
 * @author Thyago Mota
 * @date Jan-8-2019
 */

package data_collection;

import com.mongodb.*;
import com.mongodb.client.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.io.*;
import java.util.*;

public class MyFilterStream {

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

        // Twitter authentication
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(prop.getProperty("FILTER_STREAM_CONSUMER_KEY"));
        cb.setOAuthConsumerSecret(prop.getProperty("FILTER_STREAM_CONSUMER_SECRET"));
        cb.setOAuthAccessToken(prop.getProperty("FILTER_STREAM_ACCESS_TOKEN"));
        cb.setOAuthAccessTokenSecret(prop.getProperty("FILTER_STREAM_ACCESS_TOKEN_SECRET"));
        // cb.setJSONStoreEnabled(true);
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        // add a listener to the stream
        twitterStream.addListener(new MyStatusListener(polprt, accounts));

        // create a filter query based on terms and start listening to the stream
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.track(accounts);
        twitterStream.filter(filterQuery);
    }
}
