import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class ThirdLevelHierarchy implements TagHierarchy<String>{
    private String parentname;
    private TreeMap<String,DefaultHierarchy<String>> hierarchy;
    private int countTags = 0;

    public ThirdLevelHierarchy(String parent){ //String tagsetName, 
        this.parentname = parent;
        hierarchy = new TreeMap<>();
    }

    //A-D
    //A:B:C:D
    //*Aa-Ad:Ae-Ah:..:Ba-Bd:Be-Bh:..:
    @Override
    public void addToTagSet(String tag) { //, 
        countTags++;
        String first = Character.toString(tag.charAt(0)).toUpperCase();
        char second ;

        if(tag.length()>1){
            second = Character.toLowerCase(tag.charAt(1));
        }else{
            second = ' '; //short words -> go to first[ ]
        }

        //second==':' gives minor problems..

        if (second=='ø'){ //othervise ø goes to m-p ...
            String slot = first+"y-"+first+"å";
            addToHierarchy(slot, tag);
            return;
        }
        if (second=='ì'){ // ì(34)
            String slot = first+"i-"+first+"l"; //othervise ì goes to e-h ...
            addToHierarchy(slot, tag);
            return;
        }
        if (second=='ð'){ //ð(35) Óðinn
            String slot = first+"a-"+first+"d"; //othervise ð goes to i-l ...
            addToHierarchy(slot, tag);
            return;
        }

        if(second >= '0' && second <= '9'){  //Character.isDigit(first)
            String slot=first+"0-"+first+"9";
            addToHierarchy(slot, tag); //Y0-Y9
            return;
        }

        if (!Character.isLetterOrDigit(second)){
            if(second=='\''){ //*to prevent sql string problems
                String slot=first+first+"[ ]-"+first+first+"*";
                addToHierarchy(slot, tag); 
                return;
            }
            String slot=first+"[ ]-"+first+"*";
            addToHierarchy(slot, tag); //Y[ ]-Y*
            return;
        }

        int secondslot = ((second+3)/4)-25; //integer div -> floors...
        String slotname = "";

        switch(secondslot){
            case 0 : slotname = first+"a-"+first+"d"; break;
            case 1 : slotname = first+"e-"+first+"h"; break;
            case 2 : slotname = first+"i-"+first+"l"; break;
            case 3 : slotname = first+"m-"+first+"p"; break;
            case 4 : slotname = first+"q-"+first+"t"; break;
            case 5 : slotname = first+"u-"+first+"x"; break;
            case 6 : slotname = first+"y-"+first+"å"; break;
            case 31 : slotname = first+"a-"+first+"d"; break; // à
            case 32 : slotname = first+"a-"+first+"d"; break; // á, ä, â, ã
            case 33 : slotname = first+"y-"+first+"å"; break; // æ, å
            case 34 : slotname = first+"e-"+first+"h"; break; // é, ê
            case 35 : slotname = first+"i-"+first+"l"; break; // í, î, ï
            case 36 : slotname = first+"m-"+first+"p"; break; // ó, ô
            case 37 : slotname = first+"m-"+first+"p"; break; // ö, ø, õ
            case 38 : slotname = first+"u-"+first+"x"; break; // ü, ú
            case 39 : slotname = first+"y-"+first+"å"; break; // ÿ, ý
        }
        // if(second=='à'||second=='ÿ'){
        //         System.out.println("first_char: "+second+"\n slot: "+slotname+"\t"+secondslot); 
        //         System.out.println(tag);
        //     }
// ï(35)í(35) áäâ(32)ã(32) æå éê(34) ü(38)ú(38) óøô(36)öõ(37) ý(39)ÿ(39)
// à(31) ð(35) ì(34)
//https://en.wikipedia.org/wiki/List_of_Unicode_characters
        addToHierarchy(slotname, tag);
    }

    private void addToHierarchy(String parent, String tag){
        if (!hierarchy.containsKey(parent)){
            hierarchy.put(parent,new DefaultHierarchy<>(parent));
        }
        hierarchy.get(parent).addToTagSet(tag); 
    }

    //could be improved
    @Override
    public void statistics(){ 
        System.out.println(countTags+"s in "+parentname+" scattered across "+hierarchy.size()+" alphabetic slots");
        // String presentslots = "";
        // for (String slot : hierarchy.keySet()) {
        //     presentslots += slot+" ";
        // }
        // System.out.println(presentslots);

        // for (String slot : hierarchy.keySet()) {
        //     hierarchy.get(slot).statistics();;
        // }
        //hierarchy.get("Ba-Bd").statistics();
    }


    @Override
    public String toString() {
        String json = "{\"name\": \""+parentname+"\", \"children\": [";
        boolean first = true;
        for (String parentTag : hierarchy.keySet()) {
            if(!first) { json+=","; }
            if (first) { first = false;}
            json += hierarchy.get(parentTag).toString();
        }
        // if (missing){
        //     json+= ",{\"name\": \"missing\", \"children\": []}";
        // }
        json += "]}";
        return json;
    }

}
