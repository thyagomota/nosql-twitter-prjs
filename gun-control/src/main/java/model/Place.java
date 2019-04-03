package model;

/**
 * This class models a place with a name, type, state, and population.
 *
 * @date 2018-07-12
 * @version 1.0
 * @author Thyago Mota
 */
public class Place implements Comparable<Place> {

    private String    name;
    private PlaceType type;
    private State     state;
    private double    latitude;
    private double    longitude;
    private int       population;

    public Place(String name, PlaceType type, State state, double latitude, double longitude, int population) {
        this.name       = name;
        this.type       = type;
        this.state      = state;
        this.latitude   = latitude;
        this.longitude  = longitude;
        this.population = population;
    }

    public Place(String name, State state, double latitude, double longitude) {
        this(name, PlaceType.unknown, state, latitude, longitude, 0);
    }

    public String getName() {
        return name;
    }

    public PlaceType getType() {
        return type;
    }

    public State getState() {
        return state;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getPopulation() {
        return population;
    }

    @Override
    public boolean equals(Object obj) {
        Place other = (Place) obj;
        return name.equalsIgnoreCase(other.getName()) && state.equals(other.getState());
    }

    @Override
    public String toString() {
        return "Place{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", state=" + state +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", population=" + population +
                '}';
    }

    public int compareTo(Place other) {
        return name.compareTo(other.name);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new Place(name, type, state, latitude, longitude, population);
    }

//    public static void main(String[] args) {
//        State state = new State("PA", "Pennsylvania");
//        Place place = new Place("Bethlehem", PlaceType.city, state, 75293);
//        System.out.println(place);
//    }
}