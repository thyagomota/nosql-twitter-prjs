/**
 * nosql-twitter - Spanish Elections Project
 * This program compiles the words used by each one of the accounts, creating CSV files for further processing
 * @author Thyago Mota
 * @date Jan-8-2019
 */

package preprocessing;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

public class FilterWordsPerUser {

    private static final String RESOURCES_PATH   = "src/main/resources/";
    private static final String INPUT_FILE_NAME  = RESOURCES_PATH + "words_per_user.csv";
    private static final String SPANISH_WORDS    = RESOURCES_PATH + "spanish_words.txt";
    private static final String[] exceptionWords = { "a", "bajo", "contra", "desde", "en", "hacia", "para", "según", "sobre", "ante", "con", "de", "detrás", "entre", "hasta", "por", "sin", "tras", "este", "esta", "estos", "estas", "ese", "esa", "esos", "esas", "aquel", "aquella", "aquellos", "aquellas" };
    private static final int MIN_WORD_SIZE = 4;

    public static void main(String[] args) throws IOException {
        // read properties files
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties prop = new Properties();
        prop.load(loader.getResourceAsStream("spanish-elections.properties"));

        // load accounts
        String accounts[] = prop.getProperty("ACCOUNTS").split(",");

        // load prepositions as a set
        Set<String> exceptionWordsSet = new HashSet<String>();
        for (int i = 0; i < exceptionWords.length; i++)
            exceptionWordsSet.add(exceptionWords[i]);

        // I/O preps
        Scanner in = new Scanner(new FileInputStream(SPANISH_WORDS));
        Set<String> spanishWords = new HashSet<String>();
        while (in.hasNextLine())
            spanishWords.add(in.nextLine().toLowerCase());
        in.close();
        in = new Scanner(new FileInputStream(INPUT_FILE_NAME));
        in.nextLine(); // ignore 1st line

        PrintStream out[] = new PrintStream[accounts.length];
        for (int i = 0; i < accounts.length; i++)
            out[i] = new PrintStream(new FileOutputStream(RESOURCES_PATH + accounts[i] + "_words.csv"));

        // processing
        while (in.hasNextLine()) {
            String line = in.nextLine();
            String data[] = line.split(",");
            if (data.length != 3)
                continue;
            String screenName = data[0].toLowerCase();
            String word = data[1].toLowerCase();
            if (word.length() < MIN_WORD_SIZE)
                continue;
            if (exceptionWordsSet.contains(word))
                continue;
            if (!spanishWords.contains(word))
                continue;
            int count = Integer.parseInt(data[2]);
            int i = 0;
            while (i < accounts.length)
                if (accounts[i].equals(screenName))
                    break;
                else
                    i++;
            if (i == accounts.length)
                continue;
            out[i].println(word + ";" + count);
        }

        in.close();
        for (int i = 0; i < accounts.length; i++)
            out[i].close();
    }
}