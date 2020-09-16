package com.example.hospiton;

public class Contacts {
    private String name,image;

    public Contacts()
    {

    }

    public Contacts(String name,String image)
    {
        this.name=name;
        this.image=image;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

}
