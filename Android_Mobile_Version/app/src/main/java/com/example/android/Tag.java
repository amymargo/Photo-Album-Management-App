package com.example.android;

import java.io.Serializable;
import java.util.Objects;

public class Tag implements Serializable{
    private static final long serialVersionUID = 1L;

    private String type;
    private String value;

    public Tag(String type, String value){
        this.type = type.trim().toLowerCase();
        this.value = value.trim();
    }

    public String getType(){
        return type;
    }
    public String getValue(){
        return value;
    }

    @Override
    public boolean equals(Object o){ // Method to check if two tags are equal
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return type.equals(tag.getType()) && value.equals(tag.getValue());
    }

    @Override
    public String toString(){ // Making a string converter in case we need to print
        return type + "=" + value;
    }

    @Override
    public int hashCode(){
        return Objects.hash(type, value);
    }
}
