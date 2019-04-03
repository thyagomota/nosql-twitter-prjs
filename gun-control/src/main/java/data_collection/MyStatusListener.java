package data_collection;

import com.mongodb.*;
import com.mongodb.client.*;
import model.Geocoder;
import model.Hashtags;
import org.bson.*;
import twitter4j.*;
import java.io.*;
import java.util.*;

/**
 * Listener for the filter stream. This listener saves in all of the users caught by the filter in the "users" collection.
 *
 * @date 2018-08-01
 * @version 1.0
 * @author Thyago Mota
 */
public class MyStatusListener implements StatusListener {

    private MongoCollection usersCollection;
    private Geocoder        geocoder;

    public MyStatusListener(MongoDatabase guncontrol) throws FileNotFoundException {
        usersCollection = guncontrol.getCollection("users");
        geocoder = Geocoder.getInstance();
    }

    /**
     * This method is called automatically whenever a tweet matches one of the terms of interest.
     *
     * @param status
     */
    public void onStatus(Status status) {
        // if user is protected or language is not "en", ignore status
        User user = status.getUser();
        if (user.isProtected() || !user.getLang().equalsIgnoreCase("en"))
            return;

        // if user is not "explicitly" in US, ignore status
        List<model.Place> result = geocoder.geocode(user.getLocation());
        if (result == null)
            return;

        // make sure tweet has at least one of the target hashtags
        boolean foundHashtag = false;
        for (HashtagEntity hashtagEntity: status.getHashtagEntities()) {
            String hashtag = hashtagEntity.getText().toLowerCase();
            if (Hashtags.contains(hashtag)) {
                foundHashtag = true;
                break;
            }
        }
        if (!foundHashtag)
            return;

        // get user info as a doc
        Document doc = new Document();
        doc.append("id", user.getId());
        doc.append("screen_name", user.getScreenName());
        doc.append("name", user.getName());
        doc.append("description", user.getDescription());
        doc.append("location", user.getLocation());
        doc.append("created_at", user.getCreatedAt());
        doc.append("followers_count", user.getFollowersCount());
        doc.append("friends_count", user.getFriendsCount());
        doc.append("favourites_count", user.getFavouritesCount());
        doc.append("statuses_count", user.getStatusesCount());
        doc.append("lang", user.getLang());
        doc.append("time_zone", user.getTimeZone());
        doc.append("geo_enabled", user.isGeoEnabled());
        doc.append("verified", user.isVerified());
        doc.append("url", user.getURL());

        // try to insert the user into the "users" collection
        try {
            usersCollection.insertOne(doc);
        }
        catch (MongoException ex) {
            // you might get duplicate key errors, so just ignore them!
        }

        // check retweeted status; if tweet is a retweet, save original user info as well
        Status retweetedStatus = status.getRetweetedStatus();
        if (retweetedStatus != null) {

            User originalUser = retweetedStatus.getUser();

            // if original user is protected or language is not "en", ignore status
            if (originalUser == null || originalUser.isProtected() || !originalUser.getLang().equalsIgnoreCase("en"))
                return;

            // get original user info as a doc
            doc = new Document();
            doc.append("id", originalUser.getId());
            doc.append("screen_name", originalUser.getScreenName());
            doc.append("name", originalUser.getName());
            doc.append("description", originalUser.getDescription());
            doc.append("location", originalUser.getLocation());
            doc.append("created_at", originalUser.getCreatedAt());
            doc.append("followers_count", originalUser.getFollowersCount());
            doc.append("friends_count", originalUser.getFriendsCount());
            doc.append("favourites_count", originalUser.getFavouritesCount());
            doc.append("statuses_count", originalUser.getStatusesCount());
            doc.append("lang", originalUser.getLang());
            doc.append("time_zone", originalUser.getTimeZone());
            doc.append("geo_enabled", originalUser.isGeoEnabled());
            doc.append("verified", originalUser.isVerified());
            doc.append("url", originalUser.getURL());

            // try to insert the original user into the "users" collection
            try {
                usersCollection.insertOne(doc);
            }
            catch (MongoException ex) {
                // you might get duplicate key errors, so just ignore them!
            }
        }
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
        //System.out.println("Unexpected ERROR: " + e);
    }
}
