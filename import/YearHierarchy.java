import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class YearHierarchy implements TagHierarchy<String> {
    private String tagsetname;
    private int missingYear;
    private int tagcount = 0;
    private TreeMap<Integer,DefaultHierarchy<Integer>> periode;
    private boolean missing = false;

    public YearHierarchy(String tagsetname){
        this.tagsetname = tagsetname;
        periode = new TreeMap<>();
    }
    
    @Override
    public void addToTagSet(String tag) {
        tagcount++;
        if(tag.equals("missing")){ //* enables browsing missing years
            missing = true;
            missingYear++;
            return;
        }
        int numYear = Integer.valueOf(tag);
        int numPeriode = numYear/10; //integer division -> floors...
        //System.out.println("Year: "+numYear+"\n Periode: "+numPeriode+"0's");
        if (!periode.containsKey(numPeriode)){
            periode.put(numPeriode,new DefaultHierarchy<>(numPeriode+"0s"));
        }
        periode.get(numPeriode).addToTagSet(numYear);
    }

    @Override
    public void statistics(){
        System.out.println("Total release years: "+tagcount+" Release years missing: "+missingYear+"\n");
        // for (int item : periode.keySet()) {
        //     //System.out.println("Decade: "+item+"0's");
        //     periode.get(item).statistics();
        // }
        // System.out.println();
    }

    @Override
    public String toString() {
        String hierarchy = "{\"name\": \""+tagsetname+"\", \"children\": [";
        boolean first = true;
        for (int parentTag : periode.keySet()) {
            if(!first) { hierarchy+=","; }
            if (first) { first = false; }
            hierarchy += periode.get(parentTag).toString();
        }
        if (missing){
            hierarchy+= ",{\"name\": \"missing\", \"children\": []}";
        }
        hierarchy += "]}";
        return hierarchy;
    }
}
