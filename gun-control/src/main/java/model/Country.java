package model;

import java.util.LinkedList;
import java.util.List;

/**
 * This class models a country with a two-letter abbreviation and a list of possible names.
 *
 * @date 2018-07-12
 * @version 1.0
 * @author Thyago Mota
 */
public class Country {

    private String abbr;
    private List<String> names;

    public Country(String abbr) {
        this.abbr = abbr.toUpperCase();
        names = new LinkedList<String>();
    }

    public void add(String name) {
        names.add(name);
    }

    public String getAbbr() {
        return abbr;
    }

    public List<String> getNames() {
        return names;
    }

    public boolean hasName(String name) {
        return names.contains(name);
    }

    @Override
    public String toString() {
        return "Country{" +
                "abbr='" + abbr + '\'' +
                ", names=" + names +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        Country other = (Country) obj;
        return abbr.equalsIgnoreCase(((Country) obj).getAbbr());
    }
}
