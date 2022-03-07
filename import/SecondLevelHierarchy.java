import java.util.HashMap;
import java.util.TreeMap;

import javax.crypto.spec.ChaCha20ParameterSpec;

import java.util.HashSet;
import java.util.Set;

public class SecondLevelHierarchy implements TagHierarchy<String>{
    private Set<String> firstchars;
    //private Set<Character> secondchars;

    private String parentname;
    private TreeMap<String,ThirdLevelHierarchy> hierarchy;
    private int countTags = 0;

    public SecondLevelHierarchy (String parent){ 
        firstchars = new HashSet<>();
        //secondchars = new HashSet<>();
        this.parentname = parent;
        hierarchy = new TreeMap<>();
    }

    //A-D
    //*A:B:C:D
    //Aa-Ad:Ae-Ah:..:Ba-Bd:Be-Bh:..:
    @Override
    public void addToTagSet(String tag) { 
        countTags++;
        String first = (tag.charAt(0)+"").toUpperCase();

        firstchars.add(first);
        // if(tag.length()>1){
        //     if(Character.isLetter(tag.charAt(1))){ //used for finding present chars
        //         secondchars.add(Character.toLowerCase(tag.charAt(1)));
        //     }
        // }
        if(first.equals("\'")){ //*to prevent sql string problems
            String first_sql_friendly = first+first;
            addToHierarchy(first_sql_friendly, tag);
            return;
        }

        addToHierarchy(first, tag);
    }

    private void addToHierarchy(String parent, String tag){
        if (!hierarchy.containsKey(parent)){
            hierarchy.put(parent,new ThirdLevelHierarchy(parent));
        }
        hierarchy.get(parent).addToTagSet(tag); 
    }

    //could be improved
    @Override
    public void statistics(){ 
        System.out.println(countTags+" "+parentname+" scattered across "+hierarchy.size()+" letters");
        // for (String item : hierarchy.keySet()) {
        //     hierarchy.get(item).statistics();
        // }
        // System.out.println();
        //hierarchy.get("B").statistics();
    }

    void printPresentChars(){
        String allfirstchars = "";
        for (String character : firstchars) {
            allfirstchars+=character;
        }
        System.out.println(parentname+"\t"+allfirstchars);
        // String allsecondchars="";
        // for (Character c : secondchars) {
        //     allsecondchars+=c;
        // }
        // System.out.println(allsecondchars);
    }

    @Override
    public String toString() {
        String json = "{\"name\": \""+parentname+"\", \"children\": [";
        boolean first = true;
        for (String parentTag : hierarchy.keySet()) {
            if(!first) { json+=","; }
            if (first) { first = false; }
            json += hierarchy.get(parentTag).toString();
        }
        // if (missing){
        //     json+= ",{\"name\": \"missing\", \"children\": []}";
        // }
        json += "]}";
        return json;
    }
    
}
