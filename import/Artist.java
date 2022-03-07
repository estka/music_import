import java.util.List;

public class Artist {
    final String[] columns = {"dr_artist_id", "dr_artist", "dr_artist_gender", "dr_artist_country"};
    
    private int id;
    private String name;
    private String gender;
    private String country;

    //all data can be missing - if so the artist object will not be created
    public Artist(List<String> input){
        id = Integer.valueOf(input.get(0));
        name = input.get(1);
        gender = input.get(2);
        country = input.get(3);
    }
    public String toString(){
        String d = "||"; //delimiter
        String res = "";
        //res += d+columns[0]+d+id; //* DR id not important
        //res += d+columns[1]+d+name; //*redundant
        res += d+columns[2]+d+gender;
        res += d+columns[3]+d+country;
        return res;
    }

    public Integer getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getCountry() {
        return country;
    }
}
