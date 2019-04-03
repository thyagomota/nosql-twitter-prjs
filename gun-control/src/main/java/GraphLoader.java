import model.Hashtags;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class GraphLoader {

    //    private static final String INPUT_FILE_NAME  = "users-1000.json";
    private static final String INPUT_FILE_NAME  = "users-oct1.json";
    private static final String OUTPUT_FILE_NAME = "neo4j-script.csv";

    public GraphLoader() throws FileNotFoundException {


        Scanner in = new Scanner(new FileInputStream(INPUT_FILE_NAME));
        PrintStream out = new PrintStream(new FileOutputStream(OUTPUT_FILE_NAME));
        while (in.hasNextLine()) {
            String line = in.nextLine();
            line = line.trim();
            try {
                JSONObject jsonObject = new JSONObject(line);
                String userScreenName = jsonObject.getString("screen_name");
                JSONArray timeline = jsonObject.getJSONArray("timeline");
                for (int i = 0; i < timeline.length(); i++) {
                    JSONObject tweet = (JSONObject) timeline.get(i);
                    JSONObject entities = tweet.getJSONObject("entities");
                    JSONArray hashtagsArray = entities.getJSONArray("hashtags");
                    boolean isGunControlTweet = false;
                    for (int j = 0; j < hashtagsArray.length(); j++) {
                        JSONObject hashtagObject = hashtagsArray.getJSONObject(j);
                        if (Hashtags.contains(hashtagObject.getString("text"))) {
                            isGunControlTweet = true;
                            break;
                        }
                    }
                    if (!isGunControlTweet)
                        continue;
                    if (tweet.has("retweeted_status")) {
                        JSONObject retweetStatus = tweet.getJSONObject("retweeted_status");
                        if (retweetStatus != null) {
                            JSONObject originalUser = retweetStatus.getJSONObject("user");
                            String originalUserScreenName = originalUser.getString("screen_name");

                            out.println(userScreenName + "," + originalUserScreenName);

//                            out.print("MERGE (a:User {name: '" + userScreenName + "'}) ");
//                            out.print("MERGE (b:User {name: '" + originalUserScreenName + "'}) ");
//                            out.println("CREATE (a)-[r1:retweets]->(b)");
//                            out.println("WITH 1 as dummy\n");
                        }
                    }
                }
            }
            catch (JSONException ex) {

            }
        }
        in.close();

//        out.println("MATCH (n)\nRETURN n;");
        out.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new GraphLoader();
    }
}