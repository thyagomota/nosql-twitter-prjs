/**
 * nosql-twitter - Spanish Elections Project
 * This status listener saves tweets originated by "accounts" into polprt MongoDB database
 * @author Thyago Mota
 * @date Jan-8-2019
 */

package data_collection;

import com.mongodb.client.*;;
import twitter4j.*;
import java.io.*;

public class MyStatusListener implements StatusListener {

    private MongoCollection tweetsCollection;
    private String accounts[];

    public MyStatusListener(MongoDatabase polprt, String accounts[]) throws FileNotFoundException {
        tweetsCollection = polprt.getCollection("tweets");
        this.accounts = accounts;
    }

    public void onStatus(Status status) {
        // if user is protected
        User user = status.getUser();
        if (user.isProtected())
            return;

        // make sure tweet was originated by one of the target accounts
        String screenName = user.getScreenName();
        boolean found = false;
        for (int i = 0; i < accounts.length; i++)
            if (screenName.equalsIgnoreCase(accounts[i])) {
                found = true;
                break;
            }
        if (!found)
            return;

        // insert tweet into tweets collection
        tweetsCollection.insertOne(status);
    }

    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

    }

    public void onTrackLimitationNotice(int i) {

    }

    public void onScrubGeo(long l, long l1) {

    }

    public void onStallWarning(StallWarning stallWarning) {

    }

    public void onException(Exception e) {
        System.out.println("Unexpected ERROR: " + e);
    }
}