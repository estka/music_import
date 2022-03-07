import java.util.*;

public class Album {
    final String[] columns = {"dr_release_id", "dr_release_title", "dr_artist_id", "dr_artist", "dr_artist_gender", "dr_artist_country", "dr_release_year", "dr_release_label", "dr_release_media", "dr_release_format", "dr_image_id", "dr_image_sort_no", "dr_image_path"};

    private int release_id;
    private String release_title; //always present
    private List<Artist> artists;
    private int release_year; //can be missing
    private Set<String> release_labels; //always present. can be duplicate -> using set
    private String release_media; //always present
    private String release_format; //can be missing
    private String img_id; //an album can have mulitple images
    private String img_sort_no;
    private String img_uri;

    private List<AlbumImage> images;

    private int missingCounter = 0; //statis?
    //TODO chech img path setup is the same as in spring

    // evt split input up i to lister
    public Album(List<String> input){
        release_id = Integer.valueOf(input.get(0));
        release_title = input.get(1);

        artists = new ArrayList<>();
        List<String> input_artist = input.subList(2, 6);
        if (input_artist.get(0).length() == 0){
            //System.out.print("MISSING ARTIST ID\t");
            if (input_artist.get(1).length()==0 && input_artist.get(3).length()==0 && input_artist.get(3).length()==0){
                //System.out.print("MISSING Artist 4 real\n");
            }
            missingCounter++;
            //artists.add(new Artist(List.of("-1","missing","missing","missing")));
        }
        else {
            String[] artist_id = input_artist.get(0).split(";");
            String[] name = input_artist.get(1).split(";");
            String[] gender = input_artist.get(2).split(";");
            String[] country = input_artist.get(3).split(";");
            for (int i = 0; i < artist_id.length; i++) {
                if (name.length > artist_id.length){
                    if (name.length!=gender.length){
                        //artist_id 2007457 , artist_name à;grumh
                        artists.add(new Artist(List.of(artist_id[i], "à;grumh", gender[i], country[i])));                    
                        continue;
                    }
                    // add to same artist_id
                    artists.add(new Artist(List.of(artist_id[i], name[i+1], gender[i+1], country[i+1])));                    
                }
                artists.add(new Artist(List.of(artist_id[i], name[i], gender[i], country[i])));
            }
        }
        var thisyear = input.get(6);
        if(thisyear.length()==0){
            release_year = -1; //missing year
        } else{
            release_year = Integer.valueOf(input.get(6));
        }
        //System.out.println("set "+ input.get(7).split(";").length);
        release_labels = new HashSet<>();
        //trim() because of "Disney;LucasArts	"
        release_labels.addAll(List.of(input.get(7).trim().split(";"))); //*may contain duplicates. Using Set
        release_media= input.get(8);
        release_format = input.get(9);
        if (release_format.length() == 0) {release_format = "missing";} //* can be missing

        images = new ArrayList<>();
        images.add(new AlbumImage(release_id, input.subList(10, 12)));

        //img_id = input.get(10);
        //img_sort_no = input.get(11);
        //img_uri = "/"+release_id+"/"+img_id+".jpg";

        //input[12] is image path at DR. It will not be used and is thrown away here..

    }

    public String getTitle() {
        return release_title;
    }

    public Integer getReleaseId(){
        return release_id;
    }

    public String getMedia(){
        return release_media;
    }

    public String getFormat(){
        return release_format;
    }

    public Set<String> getLabels(){
        return release_labels;
    }

    public Boolean missingArtist(){
        return artists.size() == 0;
    }

    public int getMissingCounter() {
        return missingCounter;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public String getArtistsNames() {
        String res = "";
        for (Artist artist : artists) {
            res += artist.getName()+" ";
        }
        return res;
    }

    public void addImage(List<String> img){
        //System.err.println("before "+images.size());
        //System.out.println("hello in artist "+img);
        images.add(new AlbumImage(release_id, img));
        //System.err.println("after "+images.size());
    }

    public int getMissingImgSortNo() {
        int missing = 0;
        for (AlbumImage img : images) {
            missing += img.getMissingSort();
        }
        return missing;
    }

    public Integer countImages() {
        return images.size();
    }

    public String toString(Boolean thumbnail){
        String d = "||"; //delimiter
        String ret = "";
        // if (thumbnail){ //*using spotify_album_cover_artwork
        //     ret += d+"thumbnail"+d;
        // }
        //ret += images.get(0).getImagePath(); //taking fist img in list as thumbnail
       
        //ret +=d+columns[0]+d+release_id+d+columns[1]+d+ release_title; //*redundant title

        for (Artist x : artists) {
            ret += x.toString();
        }
        
        //ret += d+columns[6]+d+release_year; //* using sp_release

        for (String label : release_labels) {
            ret += d+columns[7]+d+label;
        }
        ret += d+columns[8]+d+release_media+d+columns[9]+d+release_format;
        
        // *DR album cover images is not in use
        // for (AlbumImage img : images) {
        //     ret += img.toString();
        // }
        //ret += d+columns[10]+d+img_id+d+columns[11]+d+img_sort_no;
        return ret;
    }
}
