import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.PortUnreachableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.Arrays;

public class GenreHierarchy implements TagHierarchy<String> {
    private String tagsetname;
    private String hierarchyname;
    private TreeMap<String,DefaultHierarchy<String>> genres;
    private int tagcount = 0;

    private List<String> notfound;

    private List<String> Blues;
    private List<String> Classical;
    private List<String> Country;
    private List<String> Electronic;
    private List<String> EasyListening;
    private List<String> Folk;
    private List<String> HipHop;
    private List<String> Jazz;
    private List<String> Metal;   
    private List<String> RnB_and_Soul;
    private List<String> Rock;
    private List<String> Pop; 
    private List<String> Punk;
    private List<String> World;
    private List<String> Other;

    //https://en.wikipedia.org/wiki/List_of_music_genres_and_styles
    private String[] blues = {"bluesrock","blues","rhythmandblues","chicagoblues","countryblues","deltablues",
    "electricblues","britishblues","punkblues","modernblues","memphisblues","texasblues","harmonicablues","desertblues",
    "acousticblues","jazzblues","neworleansblues","soulblues","piedmontblues","polishblues","jumpblues","pianoblues",
    "swampblues","gospelblues","australianblues", "doowop"};
    private String[] classical = {"neoclassical", "classical", "contemporaryclassical","violin","classicalpiano", "acappella",
    "opera", "chineseopera", "baroque"};
    private String[] country = {"country","traditionalcountry","countryrock","countrypop","countryblues","countryrap",
    "alternativecountry","texascountry","contemporarycountry","outlawcountry","neotraditionalcountry","queercountry",
    "countrygospel","countryroad","countryboogie", "bluegrass","sertanejo"};
    private String[] electronic = {"electro","hiphouse","jazztronica","jazzhouse", "triphop", "indietronica","electronica",
    "newwave","house","dubstep","ambient", "dancehall", "electrohouse", "techno", "synthwave", "synthwave", "breakbeat",
    "downtempo", "newrave", "drumandbass", "bigbeat", "edm", "chillout", "industrial","dub", "trance","tropicalhouse",
    "chillwave", "lofi","rave","trancecore","witchhouse", "turntablism","electroswing","deephouse","progressivehouse",
    "darkwave", "glitchhop", "glitch","chicagohouse", "ebm","techhouse","darkambient", "neoclassicaldarkwave", "electroclash",
    "eurodance", "grime", "ukgarage", "bounce","breaks","drill","freestyle","frenchcore","hardstyle","jungle","noise","tekno",
    "trap","wave", "bit", "chillstep","folktronica", "psychedelictrance", "newage", "abstract", "ukfunky", "minimaltechno",
    "digitalhardcore", "intelligentdancemusic", "futurebass", "futuregarage", "tecnobrega", "hauntology", "mashup", "breakcore",
    "breakcore", "brostep", "wonky", "grindcore", "electrotrash", "drone", "filthstep", "darkelectro", "videogamemusic",
    "ambienttechno","vaporwave", "vocaltrance","liquidfunk", "alternativedance","nudisco", "nintendocore","minimalism","beachhouse"};
    private String[] easylisteing = {"easylistening", "sleep", "lounge"};
    private String[] folk = {"folk", "tar", "darkfolk", "indiefolk", "neofolk", "irishfolk", "volksmusik", "singersongwriter",
    "freakfolk", "antifolk", "celtic", "accordion", "psychedelicfolk", "nordicfolk"};
    private String[] hiphop = {"hiphop","conscioushiphop","alternativehiphop","experimentalhiphop","industrialhiphop",
    "undergroundhiphop","abstracthiphop","eastcoasthiphop","indiehiphop","hardcorehiphop","politicalhiphop","ukhiphop",
    "oldschoolhiphop","germanhiphop","psychedelichiphop","seattlehiphop","polishhiphop","christianhiphop","russianhiphop",
    "southernhiphop","irishhiphop","latinhiphop","swedishhiphop","spanishhiphop","detroithiphop","finnishhiphop","balkanhiphop",
    "norwegianhiphop","frenchhiphop","canadianhiphop","italianhiphop","mexicanhiphop","slovakhiphop","danishhiphop",
    "brazilianhiphop","jazzrap", "rap", "memerap", "emorap", "westcoastrap", "beats"};
    private String[] jazz = {"jazz","cooljazz","nujazz","acidjazz","vocaljazz","freejazz","jazzfusion","smoothjazz","jazzpiano",
    "jazzrock","electrojazz","jazzmetal","darkjazz","jazzfunk","swedishjazz","souljazz","spiritualjazz","contemporaryjazz",
    "jazzguitar","latinjazz","jazzpop","frenchjazz","sambajazz","neworleansjazz","jazztrumpet","gypsyjazz","experimentaljazz",
    "japanesejazz","ethiojazz","jazzclarinet","avantgardejazz","jazzblues","polishjazz","norwegianjazz","jazztrio","jazzsaxophone",
    "germanjazz","jazzaccordion","skajazz","dutchjazz","jazzdrums","indianjazz","jazzflute","dinnerjazz","jazzvibraphone",
    "jazzorgan","jazzviolin","italianjazz","turkishjazz","brazilianjazz","bossanovajazz","indiejazz","britishjazz","jazzorchestra",
    "swing","ragtime", "bigband", "bebop","newjackswing"};
    private String[] metal = {"alternativemetal","metal","numetal","thrashmetal","industrialmetal","metalcore",
    "progressivemetal","blackmetal","symphonicmetal","gothicmetal","melodicdeathmetal","vikingmetal","doommetal",
    "speedmetal","folkmetal","celticmetal","powermetal","progressivedeathmetal","deathmetal","glammetal","rapmetal","groovemetal",
    "brutaldeathmetal","sludgemetal","warmetal","ambientblackmetal","melodicmetal","postblackmetal","progressivemetalcore",
    "stonermetal","technicaldeathmetal","symphonicblackmetal","avantgardemetal","kawaiimetal","norwegianblackmetal",
    "atmosphericblackmetal","instrumentalprogressivemetal","christianmetalcore","melodicmetalcore","postmetal","melodicblackmetal",
    "funkmetal","epicblackmetal","germanthrashmetal","polishmetal","swedishblackmetal","jmetal","swedishmetal","symphonicpowermetal",
    "jazzmetal","depressiveblackmetal","southernmetal","progressiveblackmetal","cybermetal","christianmetal","greekblackmetal",
    "gothenburgmetal","symphonicdeathmetal","swedishdeathmetal","metallichardcore","orientalmetal","industrialblackmetal",
    "finnishmetal","neoclassicalmetal","polishdeathmetal","progressivepowermetal","progmetal","danishmetal","rawblackmetal",
    "polishblackmetal","technicalbrutaldeathmetal","melodicpowermetal","spanishfolkmetal","operametal","dronemetal","avantgardeblackmetal",
    "folkblackmetal","paganblackmetal","slamdeathmetal","instrumentalblackmetal","spanishmetal","norwegianmetal","finnishblackmetal",
    "experimentalblackmetal","italianmetal","germanmetal","gothicblackmetal","hungarianmetal","germanheavymetal","russiandeathmetal",
    "technicalmelodicdeathmetal","latinmetal","frenchblackmetal","germanmetalcore","finnishdoommetal","swedishheavymetal",
    "floridadeathmetal","brazilianmetal","germanblackmetal","dutchmetal","polishfolkmetal","dubmetal","slavicfolkmetal",
    "germanpaganmetal","finnishdeathmetal","icelandicblackmetal","russianmetal","greekmetal","uspowermetal","gothicsymphonicmetal",
    "australianmetalcore","frenchmetal","braziliandeathmetal","newwaveofthrashmetal","brazilianpowermetal","canadianmetal",
    "progressivetechnicaldeathmetal","vikingblackmetal","instrumentaldeathmetal","germandeathmetal","nordicfolkmetal","turkishmetal",
    "cascadianblackmetal","estonianmetal","danishblackmetal","unblackmetal","ukrainianblackmetal","brazilianthrashmetal",
    "belarusianmetal","icelandicmetal","canadianblackmetal","belgianblackmetal","chaoticblackmetal","texasmetal","finnishpowermetal",
    "metalnoirquebecois","melodicprogressivemetal","romanianmetal","blackspeedmetal","northcarolinametal","norwegiandeathmetal",
    "austrianblackmetal","hungarianblackmetal","rapmetalcore","numetalcore","indianmetal","australianthrashmetal","lithuanianmetal",
    "scifimetal","australianmetal","tolkienmetal","neometal","swedishpowermetal","retrometal","newyorkdeathmetal",
    "mexicanblackmetal","technicalblackmetal","germanpowermetal","swissmetal","symphonicmelodicdeathmetal","neuedeutscheharte",
    "nwobhm", "mathcore", "djent", "progressivedeathcore", "brutaldeathcore"};
    private String[] rnb_and_soul = {"soul", "disco", "rb", "funk", "motown", "gospel", "psychedelicsoul"};
    private String[] rock = {"rock","indierock","alternativerock","folkrock","classicrock","progressiverock","bluesrock",
    "hardrock","gothicrock","psychedelicrock","stonerrock","postrock","acousticrock","pianorock","artrock","southernrock",
    "industrialrock","glamrock","garagerock","spacerock","funkrock","softrock","rockandroll","electronicrock","christianrock",
    "krautrock","mathrock","rockabilly","experimentalrock","heartlandrock","instrumentalrock","countryrock","polishrock",
    "rocknacional","frenchrock","jrock","brazilianrock","noiserock","albumrock","modernrock","raprock","jazzrock","deathrock",
    "russianrock","symphonicrock","spanishrock","scottishrock","darkrock","instrumentalpostrock","sleazerock","dancerock","yachtrock",
    "acidrock","geekrock","futurerock","comedyrock","rockenespanol","suomirock","latinrock","janglerock","mexicanrock",
    "rockgaucho","rocksteady","polishalternativerock","germanrock","crackrocksteady","garagerockrevival","sambarock","avantrock",
    "rootsrock","norwegianrock","swamprock","christianhardrock","canadianrock","spanishindierock","reggaerock","trashrock",
    "latvianrock","loversrock","ukrainianrock","medievalrock","pubrock","modernhardrock","rockuruguayo","celticrock","ostrock",
    "rockbaiano","danishrock","neorockabilly","japanesepsychedelicrock","turkishrock","portugueserock","italianprogressiverock",
    "melodichardrock","krock","anadolurock","modernsouthernrock","finnishprogressiverock","slovenianrock","scottishindierock",
    "germanpunkrock","dronerock","australianrock","slovakrock","belgianrock","psychedelicspacerock","rockgoiano","czechrock",
    "atmosphericpostrock","dutchrock","brazilianindierock","chileanrock","swissrock","romanianrock","venezuelanrock","rockandaluz",
    "irishrock","pakistanirock","lithuanianrock","russianpunkrock","rockcatala","instrumentalmathrock","classicrussianrock",
    "folkrockitaliano","rockcristiano","wrock","japanesepostrock","germanrockabilly","indianrock","japaneseindierock","detroitrock",
    "indonesianrock","welshrock","pinoyrock","indorock","persianrock","rockinopposition","hungarianrock","colombianrock",
    "swedishrockabilly","psychedelicfolkrock","shoegaze", "grunge", "slowcore", "darkcabaret", "postgrunge"};
    private String[] pop = {"pop","indiepop","electropop","teenpop","powerpop","synthpop","britpop","psychedelicpop","dreampop","artpop",
    "poppunk","poprock","chamberpop","dancepop","indieelectropop","ambientpop","folkpop","noisepop","kpop","glitchpop",
    "sophistipop","baroquepop","bubblegumpop","alternativepop","sunshinepop","popdance","poprap","countrypop","futurepop",
    "swedishpop","janglepop","jpop","latinpop","tweepop","popsoul","experimentalpop","frenchpop","polishpop","bitpop",
    "irishpop","cpop","spanishindiepop","ukpop","bedroompop","popfolk","spanishpop","europop","russianpop","mexicanpop",
    "swedishindiepop","austropop","afropop","nederpop","koreanpop","alternativepoprock","italianpop","christianpop","jazzpop",
    "transpop","turkishpop","finnishpop","popnacional","germanpop","acousticpop","pophouse","popchileno","canadianpop",
    "indonesianpop","citypop","mathpop","danishpoprock","indiepoprock","shiverpop","hippop","kpopgirlgroup","spanishpoprock",
    "hyperpop","lebanesepop","popargentino","jpoprock","spaceagepop","danishpop","grungepop","garagepop","electropowerpop",
    "germanpoprock","persianpop","arabpop","vispop","funkpop","greekpop","chillpop","popping","powerpoppunk","cambodianpop",
    "germanindiepop","popromantico","poprb","brillbuildingpop","boypop","popelectronico","egyptianpop","newwavepop","portuguesepop",
    "desipop","icelandicpop","scandipop","swedishsynthpop","popambient","spanishelectropop","operaticpop","dutchpop", "eurovision",
    "boyband", "girlgroup", "chanson", "yeye"};
    private String[] punk = {"postpunk","poppunk","punk","folkpunk","dancepunk","hardcorepunk","horrorpunk","gypsypunk","celticpunk",
    "protopunk","skapunk","skatepunk","noisepunk","cyberpunk","garagepunk","steampunk","polishpunk","streetpunk","punkblues",
    "jpunk","artpunk","crustpunk","surfpunk","germanpunk","anarchopunk","glampunk","swedishpunk","elektropunk","psychedelicpunk",
    "emopunk","synthpunk","christianpunk","indiepunk","punknroll","greekpunk","cowpunk","russianpostpunk","acousticpunk",
    "fastmelodicpunk","swedishpostpunk","germanpunkrock","texaspunk","chicagopunk","finnishpunk","spanishpunk","brazilianpunk",
    "powerpoppunk","punkska","frenchpunk","dubpunk","italianpunk","russianpunkrock","chinesepunk","bostonpunk","punktuga", 
    "emo", "melodichardcore","riotgrrrl", "hardcore", "posthardcore", "oi", "queercore", "psychobilly", "emocore", "straightedge",
    };
    private String[] world = {"world", "worldfusion", "ska", "latin", "brega","flamenco","highlife","reggae","rumba",
    "soca", "bossanova", "rootsreggae", "gypsy", "reggaeton", "calypso", "samba", "tango", "salsa", "mpb", "anime",
    "hawaiian"};
    private String[] other = {"soundtrack", "experimental","birthday", "poetry","comedy", "sound", "cabaret", "spokenword", 
    "disney", "melancholia", "pirate", "water"};

    public GenreHierarchy(String tagsetname, String hierarchyname){
        this.tagsetname = tagsetname;
        this.hierarchyname = hierarchyname;
        genres = new TreeMap<>();
        notfound = new ArrayList<>();

        Blues = Arrays.asList(blues);
        Classical = Arrays.asList(classical);
        Country = Arrays.asList(country);
        Electronic = Arrays.asList(electronic);
        EasyListening = Arrays.asList(easylisteing);
        Folk = Arrays.asList(folk);
        HipHop = Arrays.asList(hiphop);
        Jazz = Arrays.asList(jazz);
        Metal = Arrays.asList(metal);
        RnB_and_Soul = Arrays.asList(rnb_and_soul);
        Rock = Arrays.asList(rock);
        Pop = Arrays.asList(pop);
        Punk = Arrays.asList(punk);
        World = Arrays.asList(world);
        Other = Arrays.asList(other);
    }
    
    @Override
    public void addToTagSet(String tag) {
        tagcount++;
        String thisgenre = "";

        if(Blues.contains(tag)){
            thisgenre = "Blues";
        }
        if(Classical.contains(tag)){
            thisgenre = "Classic";
        }
        if(Country.contains(tag)){
            thisgenre = "Country";
        }
        if(Electronic.contains(tag)){
            thisgenre = "Electronic";
        }
        if(EasyListening.contains(tag)){
            thisgenre = "EasyListening";
        }
        if(Folk.contains(tag)){
            thisgenre = "Folk";
        }
        if(HipHop.contains(tag)){
            thisgenre = "HipHop";
        }
        if(Jazz.contains(tag)){
            thisgenre = "Jazz";
        }
        if(Metal.contains(tag)){
            thisgenre = "Metal";
        }
        if(RnB_and_Soul.contains(tag)){
            thisgenre = "RnB_and_Soul";
        }
        if(Rock.contains(tag)){
            thisgenre = "Rock";
        }
        if(Pop.contains(tag)){
            thisgenre = "Pop";
        }
        if(Punk.contains(tag)){
            thisgenre = "Punk";
        }
        if(World.contains(tag)){
            thisgenre = "World";
        }
        if(Other.contains(tag)){
            thisgenre = "Other";
        }
        if(thisgenre.equals("")){
            notfound.add(tag);
        }
        
        if (!genres.containsKey(thisgenre)){
            genres.put(thisgenre,new DefaultHierarchy<>(thisgenre));
        }
        genres.get(thisgenre).addToTagSet(tag);
    }

    public void addToTagSet(List<String> genres){
        for (String genre : genres) {
            this.addToTagSet(genre);
        }
    }

    // public void printNotFound(){
    //     List<String> thislist =  notfound.stream().distinct().collect(Collectors.toList());
    //     for (String string : thislist) {
    //         System.out.println(string);
    //     }
    //     System.out.println(thislist.size());
    // }

    public void writeNotFoundToFile(){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("missinggenres.txt", false));
            List<String> thislist =  notfound.stream().distinct().collect(Collectors.toList());
            for (String genre : thislist) {
                writer.write(genre+"\n");
            }
            writer.close();
            System.out.println("NOT FOUND GENRES "+thislist.size());
            Integer genreCount = Blues.size()+Classical.size()+Country.size()+Electronic.size()+Folk.size()+HipHop.size()+Jazz.size()+Metal.size()+RnB_and_Soul.size()+Rock.size()+Pop.size()+Punk.size()+World.size()+Other.size();
            System.out.println("myGenres: "+genreCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void statistics(){
        // for (String item : continent.keySet()) {
        //     continent.get(item).statistics();
        // }
        int total = Blues.size(); //....all
        System.out.println(tagcount+" release countries scattered across "+total+" countries and "+genres.size()+" continents\n");
    }

    @Override
    public String toString() {
        //TagsetName:HierarchyName:RootParrent:Child:Child:Child:(...)
        String jsonStart = "{\"name\": \"Genre\",\"id\": -1,\"children\": [";
        String hierarchy = "{"+tagsetname+","+hierarchyname+","+hierarchyname;
        for (String parentTag : genres.keySet()) {
            hierarchy += ":"+parentTag;
        }
        hierarchy+="\n";

        for (String parentTag : genres.keySet()) {
            String leaf = tagsetname+":"+hierarchyname; //TagsetName:HierarchyName
            leaf += genres.get(parentTag).toString(); //:Parrent:Child:Child:Child:(...)
            hierarchy+=leaf;
        }
        return hierarchy;

    }
}
