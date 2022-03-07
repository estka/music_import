import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class AlphabeticHierarchy implements TagHierarchy<String> {
    private String tagsetname;
    private int tagcount = 0;
    //private HashMap<String,DefaultHierarchy<String>> alphabetslots; //[a-e]
    private TreeMap<String,SecondLevelHierarchy> alphabetslots; //[a-e]
    private DefaultHierarchy<String> missing;
    boolean missingvalues;
    private Set<String> difficult;

    public AlphabeticHierarchy(String tagsetname){
        this.tagsetname = tagsetname;
        alphabetslots = new TreeMap<>();
        missingvalues=false;
        difficult = new HashSet<>();
    }
    
    @Override
    public void addToTagSet(String tag) {
        tagcount++;
        String lowertag = tag.toLowerCase().trim();

        if(lowertag.contains("\"")){
            return;
            //lowertag.replace("\"", "\\\"");
        }
        if(lowertag.contains("\\")){
            return;
            //lowertag.replace("\\", "\\\\");
        }
        if(lowertag.contains("/")){
            return;
            //lowertag.replace("/", "\\/");
        }

        if(tag.equals("missing_tag")){
            if(!missingvalues){ //e.g DR-label always present -> no need for "missing" hierarchy
                missing = new DefaultHierarchy<>("missing_tag");
            }
            missingvalues=true;
            missing.addToTagSet(tag);
            return; 
        }
        //System.out.println("+"+lowertag+"+");
        char first = lowertag.charAt(0);
        //System.out.println("+"+first+"+");
        if (first=='ø'){ 
            String slot = "Y-Å"; //othervise ø goes to M-P ...
            addToHierarchy(slot, tag);
            return;
        }
        if (first=='ç'){ 
            String slot = "A-D"; //othervise ø goes to Y-Å ...
            addToHierarchy(slot, tag);
            return;
        }
        if (first=='à'){ 
            String slot = "A-D"; //othervise à goes to Q-T ...
            addToHierarchy(slot, tag); 
            return;
        }
        if(first >= '0' && first <= '9'){  //Character.isDigit(first)
            addToHierarchy("digit", tag); 
            return;
        }
        if (!Character.isLetterOrDigit(first)){
            addToHierarchy("symbol", tag);
            return;
        }

        int slot = ((first+3)/4)-25; //integer div -> floors...
        String slotname = "";
        switch(slot){
            case 0 : slotname = "A-D"; break;
            case 1 : slotname = "E-H"; break;
            case 2 : slotname = "I-L"; break;
            case 3 : slotname = "M-P"; break;
            case 4 : slotname = "Q-T"; break;
            case 5 : slotname = "U-X"; break;
            case 6 : slotname = "Y-Å"; break;
            case 21 : slotname = "M-P"; break; // μ-Ziq (pronounced "music" or mu-zik), Michael Paradinas 
            case 31 : slotname = "Q-T"; break; // ß, "ß" Baauer 2014
            case 32 : slotname = "A-D"; break; // á, ä
            case 33 : slotname = "Y-Å"; break; // æ, å
            case 34 : slotname = "E-H"; break; // é
            case 35 : slotname = "I-L"; break; // í, î
            case 36 : slotname = "M-P"; break; // ó
            case 37 : slotname = "M-P"; break; // ö, ø
            case 38 : slotname = "U-X"; break; // ú, ü
            default : difficult.add(tag); return;//break;// System.out.println(tag);
        }
        if(first=='À'||first=='à'){//||first=='µ'){
        // if(first=='á'||first=='ä'||first=='Å'||first=='æ'||first=='é'||first=='í'||first=='î'||
        // first=='ó'||first=='µ'||first=='ö'||first=='ø'||first=='ú'){
            System.out.println("first_char: "+first+"\n slot: "+slotname+"\t"+slot); 
            System.out.println(tag);
        }
//a:á(32):b:c:d:ä(32)  - e:å(33):f:æ(33):g:h   i:é(34):j:k:l  m:í(35):n:î(35):o:p  q:r:s:ó(36):t  
//u:µ(21):v:ö(37):w:ø(37):x:y z:ú(38)
//https://en.wikipedia.org/wiki/List_of_Unicode_characters
        addToHierarchy(slotname, tag);       
    }

    private void addToHierarchy(String parent, String tag){
        if (!alphabetslots.containsKey(parent)){
            alphabetslots.put(parent,new SecondLevelHierarchy(parent));
        }
        alphabetslots.get(parent).addToTagSet(tag); 
    }

    //could be improved
    public void statistics(){
        System.out.println(tagcount+" "+tagsetname.toLowerCase()+"s total scattered across "+alphabetslots.size()+" alphabetic slots\n");
        // for (String item : alphabetslots.keySet()) {
        //     alphabetslots.get(item).statistics();
        // }
        //alphabetslots.get("A-D").statistics();
    }

    public List<String> getDifficult(){
        List<String> result = new ArrayList<>(difficult);
        return result;
    }

    @Override
    public String toString() {
        String json = "{\"name\": \""+tagsetname+"\", \"children\": [";
        boolean first = true;
        for (String parentTag : alphabetslots.keySet()) {
            if(!first) { json+=","; }
            if (first) { first = false; }
            json += alphabetslots.get(parentTag).toString();
        }
        // if (missing){
        //     json+= ",{\"name\": \"missing\", \"children\": []}";
        // }
        if(missingvalues){
            json+= ",{\"name\": \"missing\", \"children\": [";
            json+=missing.toString();
            json+="]}";
        }

        json += "]}";
        return json;
    }
}
