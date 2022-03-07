import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.Arrays;

public class CountryHierarchy implements TagHierarchy<String> {
    private String tagsetname;
    private TreeMap<String,DefaultHierarchy<String>> continent;
    private int tagcount = 0;

    private List<String> Asia;
    private List<String> Africa;
    private List<String> Northamerica;
    private List<String> Southamerica;
    private List<String> Europe;
    private List<String> Oceanien;
    private List<String> Intercontinental;
    //https://en.wikipedia.org/wiki/List_of_sovereign_states_and_dependent_territories_by_continent
    private String[] asia = {"Afghanistan","Armenien","Azerbadjan","Bangladesh","Cambodia/kampuchea","Cypern",
    "Georgien","Hongkong","Indien","Indonesien","Irak","Iran","Israel","Japan","Jordan","Kazakhstan",
    "Kina","Kirgisistan","Kuwait","Libanon","Malaysia","Mongolske folkerep.","Nepal",
    "Nordvietnam","Oman","Pakistan","Palæstinensisk område","Philipinerne","Saudi-Arabien","Singapore","Sri lanka/ceylon",
    "Sydkorea","Syrien","Tadzhikistan","Taiwan","Thailand","Tibet","Tyrkiet","Uzbekistan","Vietnam"};
    private String[] africa = {"Algeriet","Angola","Benin","Botswana","Burkina Faso","Cameroun (Nord)","Cap verdiske øer",
    "Centralafr. Republik","Congo/brazzaville","Den Demokratiske Republik Congo","Elfenbenskysten","Eritrea",
    "Ethiopien","Gabon","Gambia","Ghana","Guinea","Guinea-bissau","Kenya","Liberia","Libyen","Madagascar","Malawi/nyasaland",
    "Mali","Marocco","Mauritania","Mauritius","Mozambique","Niger","Nigeria","Rwanda","Senegal","Somalia","Sudan",
    "Sydafrika","Tanzania","Togo","Tunis","Uganda","Zambia","Zanzibar","Zimbabwe","Ægypten"};
    private String[] northamerica = {"Bahamas","Barbados","Canada","Costa rica","Cuba","Dominikanske republik",
    "Grønland","Guadeloupe","Haiti","Hollandsk antiller","Honduras","Jamaica","Martinique","Mexico","Montserrat",
    "Panama","Puerto rico","Trinidad","U.S.A."};
    private String[] southamerica = {"Argentina","Bolivia","Brasilien","Chile","Colombia","Ecuador","Guyana",
    "Paraguay","Peru","Suriname","Uruguay","Venezuela"};
    private String[] europe = {"Albanien","Andorra","Belgien","Bosnien-Hercegovina","Bulgarien","Danmark","Estland",
    "Finland","Frankrig","Færøerne","Grækenland","Holland","Hviderusland","Irland","Island","Italien","Jugoslavien",
    "Kosovo","Kroatien","Letland","Lichtenstein","Litauen","Luxemburg","Makedonien","Malta","Moldova","Monaco",
    "Montenegro","Norge","Polen","Portugal","Rumænien","Rusland","Schweiz","Serbien","Slovakiet","Slovenien",
    "Spanien","Storbritannien","Sverige","Tjekkiet","Tjekkoslovakiet","Tyskland","Tyskland(øst)","U.S.S.R. (Rusland)",
    "Ukraine","Ungarn","Vatikanstaten","Østrig"};
    private String[] oceanien = {"Australien","New Zealand"};
    private String[] intercontinental = {"Multinational","Ukendt","mangler","missing"};

    public CountryHierarchy(String tagsetname){
        this.tagsetname = tagsetname;
        continent = new TreeMap<>();

        Asia = Arrays.asList(asia);
        Africa = Arrays.asList(africa);
        Northamerica = Arrays.asList(northamerica);
        Southamerica = Arrays.asList(southamerica);
        Europe = Arrays.asList(europe);
        Oceanien = Arrays.asList(oceanien);
        Intercontinental = Arrays.asList(intercontinental);
    }
    
    @Override
    public void addToTagSet(String tag) {
        tagcount++;
        String thiscontinent = "";

        if(Asia.contains(tag)){
            thiscontinent = "Asia";
        }
        if(Africa.contains(tag)){
            thiscontinent = "Africa";
        }
        if(Northamerica.contains(tag)){
            thiscontinent = "Northamerica";
        }
        if(Southamerica.contains(tag)){
            thiscontinent = "Southamerica";
        }
        if(Europe.contains(tag)){
            thiscontinent = "Europe";
        }
        if(Oceanien.contains(tag)){
            thiscontinent = "Oceanien";
        }
        if(Intercontinental.contains(tag)){
            thiscontinent = "Intercontinental";
        }
        
        if (!continent.containsKey(thiscontinent)){
            //continent.put(thiscontinent,new DefaultHierarchy<>(thiscontinent, true));
            continent.put(thiscontinent,new DefaultHierarchy<>(thiscontinent));
        }
        continent.get(thiscontinent).addToTagSet(tag);
    }

    @Override
    public void statistics(){
        // for (String item : continent.keySet()) {
        //     continent.get(item).statistics();
        // }
        int total = Asia.size()+Africa.size()+Northamerica.size()+Southamerica.size()+Europe.size()+Oceanien.size()+Intercontinental.size();
        System.out.println(tagcount+" release countries scattered across "+total+" countries and "+continent.size()+" continents\n");
    }

    @Override
    public String toString() {
        String hierarchy = "{\"name\": \""+tagsetname+"\", \"children\": [";
        boolean first = true;
        for (String parentTag : continent.keySet()) {
            if(!first) { hierarchy+=","; }
            if (first) { first = false; }
            hierarchy += continent.get(parentTag).toString();
        }
        hierarchy += "]}";
        return hierarchy;
    }
}
