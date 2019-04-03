/**
 * nosql-twitter - Spanish Elections Project
 * This program compiles the number of tweets sent by each one of the accounts on a daily basis, creating CSV files for further processing
 * @author Thyago Mota
 * @date Jan-8-2019
 */

package preprocessing;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

public class TweetsPerDayTimeSeries {

    private static final String RESOURCES_PATH   = "src/main/resources/";
    private static final String INPUT_FILE_NAME  = RESOURCES_PATH + "tweets_per_day.csv";
    private static final String OUTPUT_FILE_NAME = RESOURCES_PATH + "tweets_per_day_time_series.csv";

    public static void main(String[] args) throws IOException {
        // read properties files
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties prop = new Properties();
        prop.load(loader.getResourceAsStream("spanish-elections.properties"));

        // load accounts
        String accounts[] = prop.getProperty("ACCOUNTS").split(",");

        // I/O preps
        Scanner in = new Scanner(new FileInputStream(INPUT_FILE_NAME));
        PrintStream out = new PrintStream(new FileOutputStream(OUTPUT_FILE_NAME));

        // ignore 1st line
        in.nextLine();

        // output header line
        out.print("year,month,day");
        for (int i = 0; i < accounts.length; i++)
            out.print("," + accounts[i]);
        out.println();

        // actual processing
        int accounts_count[] = new int[accounts.length];
        int currYear, currMonth, currDay, year, month, day;
        currYear = currMonth = currDay = year = month = day = 0;
        while (in.hasNextLine()) {
            String line = in.nextLine();
            String data[] = line.split(",");
            year = Integer.parseInt(data[0]);
            month = Integer.parseInt(data[1]);
            day = Integer.parseInt(data[2]);
            String screenName = data[3].toLowerCase();
            int count = Integer.parseInt(data[4]);
            // 1st date?
            if (currYear == 0 && currMonth == 0 && currDay == 0) {
                currYear = year;
                currMonth = month;
                currDay = day;
                for (int i = 0; i < accounts.length; i++)
                    accounts_count[i] = 0;
            }
            // same date?
            if (currYear == year && currMonth == month && currDay == day) {
                int i = 0;
                while (i < accounts.length)
                    if (accounts[i].equals(screenName))
                        break;
                    else
                        i++;
                accounts_count[i] = count;
            }
            // date changed
            else {
                out.print(currYear + "," + currMonth + "," + currDay);
                for (int i = 0; i < accounts.length; i++)
                    out.print("," + accounts_count[i]);
                out.println();
                currYear = year;
                currMonth = month;
                currDay = day;
                for (int i = 0; i < accounts.length; i++)
                    accounts_count[i] = 0;
                int i = 0;
                while (i < accounts.length)
                    if (accounts[i].equals(screenName))
                        break;
                    else
                        i++;
                accounts_count[i] = count;
            }
        }
        out.print(currYear + "," + currMonth + "," + currDay);
        for (int i = 0; i < accounts.length; i++)
            out.print("," + accounts_count[i]);
        out.println();

        in.close();
        out.close();
    }
}