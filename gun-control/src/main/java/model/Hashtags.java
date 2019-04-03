package model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Hashtags {

    private static List<String> hashtags = null;
    private static final String HASHTAGS_FILE = "hashtags.txt";

    private static void load() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Scanner sc = new Scanner(loader.getResourceAsStream(HASHTAGS_FILE));
        hashtags = new LinkedList<String>();
        while (sc.hasNextLine()) {
            String hashtag = sc.nextLine().toLowerCase();
            if (hashtag.length() == 0 || hashtag.charAt(0) == '#')
                continue;
            hashtags.add(hashtag);
        }
        sc.close();
    }

    public static boolean contains(final String hashtag) {
        if (hashtags == null)
            load();
        return hashtags.contains(hashtag.toLowerCase());
    }

    public static List<String> getHashtags() {
        if (hashtags == null)
            load();
        return hashtags;
    }
}
