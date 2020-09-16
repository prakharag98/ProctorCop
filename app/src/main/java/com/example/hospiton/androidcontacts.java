package com.example.hospiton;

public class androidcontacts {
    private String id;
    private String Name;
    private String Phone_Number;


    public androidcontacts(String id,String Name,String Phone_Number)
    {
        this.id=id;
        this.Name=Name;
        this.Phone_Number=Phone_Number;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return Name;
    }

    public String getPhone_Number() {
        return Phone_Number;
    }
}
