import java.util.TreeMap;

public class DurationHierarchy implements TagHierarchy<String> {
    private String tagsetname;
    private int missingYear;
    private int tagcount = 0;
    private TreeMap<Integer,DurationSecondHierarchy> deciSeconds;
    private boolean missing = false;

    public DurationHierarchy(String tagsetname){
        this.tagsetname = tagsetname;
        deciSeconds = new TreeMap<>();
    }
    
    @Override
    public void addToTagSet(String tag) {
        System.out.println("Using string addToTagSet\ttag: "+tag);
        int intTag = Integer.valueOf(tag);
        addToTagSet(intTag);
    }

    public void addToTagSet(Integer tag) {
        tagcount++;
        //duration always present
        int minute = tag/60; //integer division -> floors...

        //System.out.println("Year: "+numYear+"\n Periode: "+numPeriode+"0's");
        if (!deciSeconds.containsKey(minute)){
            if (minute < 10){
                //periode.put(minute,new DefaultHierarchy<>("0"+minute+":59"));
                deciSeconds.put(minute,new DurationSecondHierarchy("0"+minute+":"));
            } else {
                //periode.put(minute,new DefaultHierarchy<>(minute+":59"));
                deciSeconds.put(minute,new DurationSecondHierarchy(minute+":"));
            }
        }
        deciSeconds.get(minute).addToTagSet(tag);
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
        for (int parentTag : deciSeconds.keySet()) {
            if(!first) { hierarchy+=","; }
            if (first) { first = false; }
            hierarchy += deciSeconds.get(parentTag).toString();
        }
        if (missing){
            hierarchy+= ",{\"name\": \"missing\", \"children\": []}";
        }
        hierarchy += "]}";
        return hierarchy;
    }
}
    