package model;

import org.apache.commons.lang.StringUtils;

/**
 * This class models a state with an abbreviation and name.
 *
 * @date 2018-07-12
 * @version 1.0
 * @author Thyago Mota
 */
public class State {

    private String abbr, name;
    private double latitude, longitude;

    public State(String abbr, String name, double latitude, double longitude) {
        this.name = StringUtils.capitalize(name.toLowerCase());
        this.abbr = abbr.toUpperCase();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAbbr() {
        return abbr;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "State{" +
                "abbr='" + abbr + '\'' +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        State other = (State) obj;
        return abbr.equalsIgnoreCase(other.abbr) && name.equalsIgnoreCase(other.name);
    }

//    public static void main(String[] args) {
//        State state = new State("PA", "Pennsylvania");
//        System.out.println(state);
//    }
}
