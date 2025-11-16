package app.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a tag associated with a photo.
 * A tag consists of a type (e.g., "location", "person") and a value (e.g., "Paris", "Alice").
 * Tags are used to categorize and search for photos based on metadata.
 *
 * Tags are case-insensitive and are stored in lowercase for consistency.
 * 
 * @author Toma Takamatsu
 */
public class Tag implements Serializable{
    private static final long serialVersionUID = 1L;

    private String type;
    private String value;

    /**
     * Constructs a new Tag with the specified type and value.
     * Both inputs are trimmed and converted to lowercase for normalization.
     *
     * @param type  the tag type (e.g., "person", "location")
     * @param value the tag value (e.g., "bob", "new york")
     */
    public Tag(String type, String value){
        this.type = type.trim().toLowerCase();
        this.value = value.trim().toLowerCase();
    }

    /**
     * Returns the type of the tag.
     *
     * @return the tag type
     */
    public String getType(){
        return type;
    }

    /**
     * Returns the value of the tag.
     *
     * @return the tag value
     */
    public String getValue(){
        return value;
    }

    /**
     * Compares this tag to another for equality.
     * Two tags are equal if both their types and values match.
     *
     * @param o the object to compare to
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o){ // Method to check if two tags are equal
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return type.equals(tag.getType()) && value.equals(tag.getValue());
    }

    /**
     * Returns the tag as a string in the format type=value.
     *
     * @return the string representation of the tag
     */
    @Override
    public String toString(){ // Making a string converter in case we need to print
        return type + "=" + value;
    }

    /**
     * Returns the hash code of the tag based on its type and value.
     *
     * @return hash code for use in collections
     */
    @Override
    public int hashCode(){
        return Objects.hash(type, value);
    }
}
