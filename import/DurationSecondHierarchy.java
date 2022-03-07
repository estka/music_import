import java.util.TreeMap;

public class DurationSecondHierarchy implements TagHierarchy<String>{
    private String parentname;
    private TreeMap<String,DefaultHierarchy<String>> hierarchy;
    private int countTags = 0;

    public DurationSecondHierarchy(String parent){ //String tagsetName, 
        this.parentname = parent;
        hierarchy = new TreeMap<>();
    }

    @Override
    public void addToTagSet(String tag) {
        System.out.println("Using string addToTagSet\ttag: "+tag);
        int intTag = Integer.valueOf(tag);
        addToTagSet(intTag);
    }

    //00:00-01:00
    //*00:00-00:10:00:10-00:20:..:
    public void addToTagSet(Integer tag) { // 
        countTags++;
        int seconds = tag%60; //mod to get remainder
        int slot = seconds/10;

        addToHierarchy(parentname+""+slot+"0", tag+"");
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
