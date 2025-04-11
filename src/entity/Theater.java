package entity;

public class Theater {
    private int TheaterId;
    private String name;
    private String city;

    Theater(int theaterId, String name, String city){
        this.TheaterId = theaterId;
        this.name = name;
        this.city = city;
    }

    public int getTheaterId() {
        return TheaterId;
    }

    public void setTheaterId(int theaterId) {
        TheaterId = theaterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
