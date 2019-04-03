package model;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Geocodes places to cities based on a location string. Returns all best matches.
 *
 * @date 2018-07-10
 * @version 1.0
 * @author Thyago Mota
 */
public class Geocoder {

    private static Geocoder geocoder;
    public static final String PLACES_FILE = "places.csv";
    public static final String STATES_FILE = "states.csv";
    private static final int LEVENSHTEIN_THRESHOLD = 1;
    private static final int LEVENSHTEIN_NOT_FOUND = -1;
    private Country country;
    private Set<State> states;
    private List<Place> places;

    /**
     * This method returns a reference to a Geocoder object
     * @return reference to a Geocoder object
     * @throws FileNotFoundException
     */
    public static Geocoder getInstance() throws FileNotFoundException {
        if (geocoder == null)
            geocoder = new Geocoder();
        return geocoder;
    }

    private Geocoder() throws FileNotFoundException {

        // set target country to US
        country = new Country("us");
        country.add("United States");
        country.add("United States of America");
        country.add("America");
        country.add("U.S.A.");

        // load states in US
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        states = new HashSet<State>();
        Scanner in = new Scanner(loader.getResourceAsStream(STATES_FILE));
        in.nextLine(); // ignoring 1st line
        // state,latitude,longitude,name
        while (in.hasNextLine()) {
            String line = in.nextLine();
            String data[] = line.split(",");
            String name = data[3];
            double latitude = Double.parseDouble(data[1]);
            double longitude = Double.parseDouble(data[2]);
            String abbr = data[0];
            State state = new State(abbr, name, latitude, longitude);
            states.add(state);
        }
        in.close();
        System.out.println(states);

        // load places (currently only US)
        places = new LinkedList<Place>();
        in = new Scanner(loader.getResourceAsStream(PLACES_FILE));
        in.nextLine(); // ignoring 1st line
        while (in.hasNextLine()) {
            String line = in.nextLine();
            String data[] = line.split(",");
            // name,type,state,latitude,longitude,population
            String name = data[0];
            PlaceType type = PlaceType.valueOf(data[1].replaceAll("[()]", "").toLowerCase());
            State state = getStateByAbbr(data[2]);
            double latitude = Double.parseDouble(data[3]);
            double longitude = Double.parseDouble(data[4]);
            int population = Integer.parseInt(data[5]);
            Place place = new Place(name, type, state, latitude, longitude, population);
            if (!places.contains(place))
                places.add(place);
        }
        in.close();
        Collections.sort(places);
//        System.out.println(places);
    }

    /**
     * This method returns a "State" object based on a state abbreviation String
     * @param abbr String
     * @return "State" object
     */
    private State getStateByAbbr(String abbr) {
        for (State state : states)
            if (state.getAbbr().equalsIgnoreCase(abbr))
                return state;
        return null;
    }

    /**
     * This method returns a "State" object based on a state name
     * @param name String
     * @return "State" object
     */
    private State getStateByName(String name) {
        for (State state : states)
            if (state.getName().equalsIgnoreCase(name))
                return state;
        return null;
    }

    /**
     * Given a location, this method return a "Place" object that best match the location using weighted probability based on population sizes
     * @param location String
     * @return best "Place" match
     */
    public Place probGeocode(String location) {
        List<Place> result = geocode(location);
        Place place = null;
        if (result != null) {
            int popSum = 0;
            for (Place p: result)
                popSum += p.getPopulation();
            int randomWeight = new Random().nextInt(popSum) + 1;
            for (Place p: result) {
                randomWeight -= p.getPopulation();
                if (randomWeight <= 0) {
                    place = p;
                    break;
                }
            }
        }
        return place;
    }

    /**
     * Given a location, this method returns a list of "Place" objects that match the location, null if no match is found.
     * @param location String
     * @return list of "Place" objects that match the location, null otherwise
     */
    public List<Place> geocode(String location) {
        location = location.toLowerCase();
        location = location.replaceAll("\\s+", " "); // remove double spaces
        location = location.trim();

        String token[] = location.split(",");

        // currently only accepts format "city, state"
        if (token.length > 2)
            return null;

        String name = token[0].trim();
        State state = null;
        LevenshteinDistance levenshtein = new LevenshteinDistance(LEVENSHTEIN_THRESHOLD);

        // if two levels...
        if (token.length == 2) {
            token[1] = token[1].trim();

            // if state has exactly 2 letters, assume it is abbreviated
            if (token[1].length() == 2) {
                state = getStateByAbbr(token[1]);
                if (state == null) {
                    // maybe it is in "city, us" format
                    if (!country.getAbbr().equalsIgnoreCase(token[1]))
                        return null;
                }
            }
            else {
                // if state is not abbreviated, try to find a match
                int best = LEVENSHTEIN_THRESHOLD + 1;
                for (State s : states) {
                    int d = levenshtein.apply(token[1], s.getName().toLowerCase());
                    if (d != LEVENSHTEIN_NOT_FOUND) {
                        if (state == null || (state != null && d < best)) {
                            state = s;
                            best = d;
                        }
                    }
                }
                // if couldn't find a match for the state, maybe it is in "city, country" format
                if (state == null) {
                    boolean found = false;
                    for (String usName : country.getNames()) {
                        int d = levenshtein.apply(token[1], usName.toLowerCase());
                        if (d != LEVENSHTEIN_NOT_FOUND) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        return null;
                }
            }

        } // end if (token.length == 2)
        // if it is one level, make sure it is not a U.S. or a state
        else {
            if (name.length() == 2) {
                if (getStateByAbbr(name) != null || country.getAbbr().equalsIgnoreCase(name))
                    return null;
            } else {
                // name is not abbreviated, try to find a matching name
                for (State s : states) {
                    int d = levenshtein.apply(name, s.getName().toLowerCase());
                    if (d != LEVENSHTEIN_NOT_FOUND)
                        return null;
                }
                for (String usName : country.getNames()) {
                    int d = levenshtein.apply(name, usName.toLowerCase());
                    if (d != LEVENSHTEIN_NOT_FOUND)
                        return null;
                }
            }
        } // end else

        // System.out.println("Search parameters: {name: " + name + ", state: " + state + "}");
        List<Place> result = null;
        int best = LEVENSHTEIN_THRESHOLD + 1;
        for (Place p : places) {
            if (state != null && !p.getState().equals(state))
                continue;
            int d = levenshtein.apply(name, p.getName().toLowerCase());
            if (d != LEVENSHTEIN_NOT_FOUND) {
                if (result == null || (result != null && d < best)) {
                    result = new LinkedList<Place>();
                    try {
                        result.add((Place) p.clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    best = d;
                } else if (d == best) {
                    try {
                        result.add((Place) p.clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } // end for
        return result;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Geocoder geocoder = Geocoder.getInstance();

        List<Place> result = geocoder.geocode("Bethlehem");
        System.out.println(result);
        System.out.println(result.size());

        for (int i = 0; i < 100; i++) {
            System.out.println(geocoder.probGeocode("Bethlehem"));
        }
//
//        Place place = geocoder.probGeocode("Betlehem");
//        System.out.println(place);
    }
}
