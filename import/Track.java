import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

// evt rename to Song.java
public class Track {
    final String[] columns = {"id","sp_uri","sp_track_name","sp_track_duration","sp_track_popularity","sp_album_name","sp_artist_infos",
    "x","y","z","color","happiness_percentage","sadness_percentage","anger_percentage","fear_percentage","emotion_code","genre_1","genre_2","genre_3"};
    
    private int id;
    private String uri;
    private String name;
    private int duration;
    private int popularity;
    private String album_name;
    private Album DRalbum;
    private List<Artist> DRartists;
    private List<String> artist_names; //List<String>
    private int x;
    private int y;
    private int z;
    private String color;
    private int happiness_pct;
    private int sadness_pct;
    private int anger_pct;
    private int fear_pct;
    private String emotion_code; // enum [0/1/2/3]
    //private String genre1;
    //private String genre2;
    //private String genre3;
    private Set<String> genres;

    // now is class variable
    // using simpleDataFormat to ensure the right timestamp format
    private static String now = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Timestamp(System.currentTimeMillis()));
    private String timestamp;
    private String year;
    private String month;
    private String day;
    private String sp_image;

    private List<String> semantic_labels; //from Spotify + ImgNet

    private Boolean audioFeatures;
// [danceability, energy, key, loudness, mode, speechiness, acousticness, instrumentalness, liveness, valence, tempo, time_signature, duration_ms]
    private int danceability;
    private int energy;
    private int key;
    private int loudness;
    private int mode;
    private int speechiness;
    private int acousticness;
    private int instrumentalness;
    private int liveness;
    private int valence;
    private int tempo;
    private int time_signature;
    
    public Track(List<String> input){ //, String now_timestamp
        id = Integer.valueOf(input.get(0));
        uri = input.get(1);
        if(!input.get(2).trim().equals("")){ // because of 5no5xvUUu3BnzlHztKQq0D
            name = input.get(2);
        } else {name = "missing_tag";}
        duration = (int) Math.round(Integer.valueOf(input.get(3))/1000.0); //millisec to sec
        popularity = Integer.valueOf(input.get(4));
        if(!input.get(5).trim().equals("")){ // because of 5no5xvUUu3BnzlHztKQq0D
            album_name = input.get(5);
        } else {album_name = "missing_tag";}
        DRartists = new ArrayList<>();
        artist_names = new ArrayList<>();
        String input_artist = input.get(6);
        if(input_artist.contains(",")){
            List<String> list_input_artists = Arrays.asList(input_artist.split(","));
            list_input_artists.forEach(artist -> artist_names.add(artist.trim()));
        }
        else if (!input_artist.equals("")){ // because of 5no5xvUUu3BnzlHztKQq0D
        //else{
            artist_names.add(input_artist.trim());
        }
        //artist_names.forEach(System.out::println);
        //System.out.println("# artist_names: "+ artist_names.size());

        x = Integer.valueOf(input.get(7));
        y = Integer.valueOf(input.get(8));
        z = Integer.valueOf(input.get(9));
        color = input.get(10);
        happiness_pct = (int) Math.round(100*Float.valueOf(input.get(11))); //integer pct
        sadness_pct = (int) Math.round(100*Float.valueOf(input.get(12))); //integer pct
        anger_pct = (int) Math.round(100*Float.valueOf(input.get(13))); //integer pct
        fear_pct = (int) Math.round(100*Float.valueOf(input.get(14))); //integer pct
        // if(happiness_pct+sadness_pct+anger_pct+fear_pct>101){
        //     System.out.println("hello");
        //     if(Float.valueOf(input.get(11))+Float.valueOf(input.get(12))+Float.valueOf(input.get(13))+Float.valueOf(input.get(14))>1){
        //         System.out.println(happiness_pct+"\t"+sadness_pct+"\t"+anger_pct+"\t"+fear_pct);
        //         System.out.println(Float.valueOf(input.get(11))+"\t"+Float.valueOf(input.get(12))+"\t"+Float.valueOf(input.get(13))+"\t"+Float.valueOf(input.get(14)));
        //         System.out.println(Float.valueOf(input.get(11))+Float.valueOf(input.get(12))+Float.valueOf(input.get(13))+Float.valueOf(input.get(14)));
        //     }
        // }
        //System.out.println(input.get(11)+ "\tround: "+ happiness_pct+"\t"+input.get(12)+ "\tround: "+ sadness_pct+"\t"+input.get(13)+ "\tround: "+ anger_pct+"\t"+input.get(14)+ "\tround: "+ fear_pct);
        emotion_code = findEmotionName(input.get(15)); // enum [0/1/2/3]

        genres = new HashSet<>();
        for (String genre : input.subList(16, 19)){ //[beginIdx;[endIdx
            if (genre.length()>1){ genres.add(genre); }
        }
        // if (input.get(16).length()>1){
        //     genre1 = input.get(16);
        // }
        // if (input.get(17).length()>1){
        //     genre2 = input.get(17);
        // }
        // if (input.get(18).length()>1){
        //     genre3 = input.get(18);
        // }
        timestamp = now;
        year = ""; month= ""; day = ""; 
        sp_image = "";
        semantic_labels = new ArrayList<>();
        audioFeatures = false;
    }

    public String getUri(){
        return uri;
    }

    public Integer getDuration(){
        return duration;
    }

    public String getTrackName(){
        return name;
    }

    public String getAlbum_name() {
        return album_name;
    }
    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }
    public Album getDRalbum(){
        return this.DRalbum;
    }
    public void setDRalbum(Album album){
        this.DRalbum = album;
    }
    public List<Artist> getDRartists(){
        return this.DRartists;
    }
    public void addDRartist(Artist artist){
        this.DRartists.add(artist);
    }
    public List<String> getArtist_names() {
        return artist_names;
    }
    public void setArtist_names(List<String> artist_names) {
        this.artist_names = artist_names;
    }
    public Integer getY(){
        return y;
    }
    public String getEmotionCode(){
        return emotion_code;
    }
    public Set<String> getGenres(){
        return genres;
    }
    public String getNow() {
        return now;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp){
        this.timestamp = timestamp;
        year = timestamp.substring(0, 4);
        month = findNameOfMonth(timestamp.substring(5,7));
        day = timestamp.substring(8,10);
    }
    public String getYear(){
        return year;
    }
    public void setSpotifyImage(String img_url){
        this.sp_image = img_url;
    }
    public void addSemanticLabels(List<String> labels){
        this.semantic_labels.addAll(labels);
    }
    public void addAudioFeatures(List<String> audioFeatures){
        this.audioFeatures = true;
        this.danceability = (int) Math.round(100*Float.valueOf(audioFeatures.get(0)));
        this.energy = (int) Math.round(100*Float.valueOf(audioFeatures.get(1)));
        this.key = (int) Math.round(Float.valueOf(audioFeatures.get(2)));
        this.loudness = (int) Math.round(Float.valueOf(audioFeatures.get(3)));
        this.mode = (int) Math.round(Float.valueOf(audioFeatures.get(4)));
        this.speechiness = (int) Math.round(100*Float.valueOf(audioFeatures.get(5)));
        this.acousticness = (int) Math.round(100*Float.valueOf(audioFeatures.get(6)));
        this.instrumentalness = (int) Math.round(100*Float.valueOf(audioFeatures.get(7)));
        this.liveness = (int) Math.round(100*Float.valueOf(audioFeatures.get(8)));
        this.valence = (int) Math.round(100*Float.valueOf(audioFeatures.get(9)));
        this.tempo = (int) Math.round(Float.valueOf(audioFeatures.get(10)));
        this.time_signature = (int) Math.round(Float.valueOf(audioFeatures.get(11)));
    }

    //"# Format: FileName:TagSet:Tag:TagSet:Tag:(...)\n"
    public String toString(Boolean augmented){
        String d = "||"; //delimiter
        String MTBid = columns[0]+d+id+d;
        String beginColumn = columns[1]+d;

        // context dimensions
        String res = uri+d+columns[2]+d+name+d+columns[3]+d+duration+d+columns[4]+d+popularity+d+columns[5]+d+album_name;
        for (String artist : artist_names) { //can have mulitiple..
            res += d+columns[6]+d+artist;
        }

        // abstract dimensions
        //res += d+columns[7]+d+x+d+columns[8]+d+y+d+columns[9]+d+z+d+columns[10]+d+color;

        // emotion dimenssions
        res += d+columns[11]+d+happiness_pct+d+columns[12]+d+sadness_pct+d+columns[13]+d+anger_pct+d+columns[14]+d+fear_pct+d+columns[15]+d+emotion_code;

        // genres
        for(String genre : genres){
            res += d+"genre"+d+genre;
        }
        // if (genre1 != null) {
        //     res += d+"genre"+d+genre1;
        // }
        // if (genre2 != null) {
        //     res += d+"genre"+d+genre2;
        // }  
        // if (genre3 != null) {
        //     res += d+"genre"+d+genre3;
        // }
        //+",,genre,,"+genre2+",,genre,,"+genre3;
        
        // timestamp dimensions
        res += d+"timestamp"+d+timestamp;
        if (!timestamp.equals(now)){
                res += d+"year"+d+year;
                res += d+"month"+d+month;
                res += d+"day"+d+day;
        } else {
                //res += d+"year"+d+"missing"+d+"month"+d+"missing"+d+"day"+d+"missing";
        }
        if (!sp_image.equals("")){
            res += d+"thumbnail"+d+sp_image;
        }

        // image classifier dimension
        for (String label : semantic_labels){
            res += d+"semantic_tag"+d+label;
        }

        // audio featues
        if (audioFeatures){
            res+= d+"danceability"+d+danceability+d+"energy"+d+energy+d+"key"+d+key+d+"loudness"+d+loudness
                +d+"mode"+d+mode+d+"speechiness"+d+speechiness+d+"acousticness"+d+acousticness
                +d+"instrumentalness"+d+instrumentalness+d+"liveness"+d+liveness+d+"valence"+d+valence
                +d+"tempo"+d+tempo+d+"time_signature"+d+time_signature;
        }

        // augmented DR dimensions
        if (augmented){
            if (DRalbum!=null){ //augmented album
                res += DRalbum.toString(augmented);
            } else { //augmented artist
                for (Artist x : DRartists) {
                    res += x.toString();
                }
            }
        }
        res += "\n";
        return res;
    }

    public String findNameOfMonth(String monthNumber){
        String monthString = "";
        switch (monthNumber) {
            case "01":  monthString = "January";
                     break;
            case "02":  monthString = "February";
                     break;
            case "03":  monthString = "March";
                     break;
            case "04":  monthString = "April";
                     break;
            case "05":  monthString = "May";
                     break;
            case "06":  monthString = "June";
                     break;
            case "07":  monthString = "July";
                     break;
            case "08":  monthString = "August";
                     break;
            case "09":  monthString = "September";
                     break;
            case "10": monthString = "October";
                     break;
            case "11": monthString = "November";
                     break;
            case "12": monthString = "December";
                     break;
            default: {monthString = "Invalid month"; System.out.println(monthString);}
                     break;
        }
        //System.out.println(monthString);
        return monthString;
    }

    public String findEmotionName(String emotionNumber){
        String monthString = "";
        switch (emotionNumber) {
            case "0":  monthString = "happy";
                     break;
            case "1":  monthString = "sad";
                     break;
            case "2":  monthString = "anger";
                     break;
            case "3":  monthString = "fear";
                     break;
            default: {monthString = "Invalid emotion"; System.out.println(monthString);}
                     break;
        }
        //System.out.println(monthString);
        return monthString;
    }
}
