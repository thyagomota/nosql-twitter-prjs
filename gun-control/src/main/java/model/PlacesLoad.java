package model;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

/**
 * This script filters places with population >= POPULATION_MIN from the 2010 gazetter file, saving them in a file called "places.csv" with the following structure: name, type, state, latitude, longitude, and population.
 *
 * Source for gazetter file: http://www2.census.gov/geo/docs/maps-data/data/gazetteer/Gaz_places_national.zip
 *
 * @date 2018-07-10
 * @version 1.0
 * @author Thyago Mota
 */
public class PlacesLoad {

    public static final String GAZETTER_PLACES_2K          = "Gaz_places_national.txt";
    public static final String PLACES_FILE                 = "places.csv";
    public static final int    POPULATION_MIN              = 1500;

    public static void main(String[] args) throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Scanner in = new Scanner(loader.getResourceAsStream(GAZETTER_PLACES_2K));
        PrintStream out = new PrintStream(new FileOutputStream(PLACES_FILE));
        in.nextLine(); // ignoring 1st line
        out.println("name,type,state,latitude,longitude,population"); // writing 1st line
        int count = 0;
        long totalPop = 0;
        while (in.hasNextLine()) {
            String line = in.nextLine();
            String data[] = line.split("\t");
            int population = Integer.parseInt(data[6]);
            if (population < POPULATION_MIN)
                continue;
            String state = data[0].toLowerCase();
            state = state.trim();
            if (state.equals("pr"))
                continue;
            String name = data[3].toLowerCase();
            name = name.trim();
            String type = name.substring(name.lastIndexOf(" ") + 1);
            type = type.trim();
            if (type.equals("county") || type.equals("government"))
                continue;
            name = name.substring(0, name.lastIndexOf(" "));
            // remove anything after a comma from the name
            int pos = name.indexOf(",");
            if (pos != -1)
                name = name.substring(0, pos);
            name = name.trim();
            //int code = Integer.parseInt(data[2]);
            // get latitude and longitude
            String latitude = data[12].replaceAll(" ", "");
            String longitude = data[13].replaceAll(" ", "");
            out.println(name + "," + type + "," + state + "," + latitude + "," + longitude + "," + population);
            count++;
            totalPop += population;
        }
        in.close();
        out.close();
        System.out.println(count + " places loaded!");
        System.out.println("Total population: " + totalPop);
    }
}