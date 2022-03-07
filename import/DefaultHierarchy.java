import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class DefaultHierarchy<T> implements TagHierarchy<T> {
    private String tagsetname;
    private TreeSet<T> tagset;
    //private TreeMap<T,Integer> tags;
    private int countTags=0;

    public DefaultHierarchy(String tagsetName){
        this.tagsetname = tagsetName;
        tagset = new TreeSet<>();
        //tags = new TreeMap<>();
    }
//idea: boolean stat to initiate extensive accounting

    @Override
    public void addToTagSet(T tag) {
        tagset.add(tag);
        countTags++;
        // if (!tags.containsKey(tag)){
        //     tags.put(tag,1);
        // } else {
        //     tags.put(tag, tags.get(tag)+1);
        // }
    }

    public void addToTagSet(List<T> tags) {
        for (T t : tags) {
            tagset.add(t);
            countTags++;
        }
    }

    @Override
    public void statistics(){ 
        System.out.println("Total "+tagsetname+" tags : "+countTags+"\t\tDifferent "+tagsetname+" tags: "+ tagset.size());
        String presentTags = "Present "+tagsetname+" tags: ";
        for (T item : tagset){
            presentTags += item+", ";
        }
        System.out.println(presentTags+"");

        //*keeping count of how often a tag appears
        // int number = 100;
        // System.out.println("If "+name+" appears more than "+number+" it is printed here:");
        // for (T tag : tags.keySet()) {
        //     if(tags.get(tag)>number){
        //         System.out.println(tag + ": "+ tags.get(tag));
        //     }
        // }
        // System.out.println();
    }

    @Override
    public String toString() {
        String header ="";
        header+= "{\"name\": \""+tagsetname+"\", \"children\": [";

        Boolean first = true;
        for (T tag : tagset) {
            if (first){
                header += "{\"name\": \""+tag+"\", \"children\": []}";
                first = false;
            }
            else {
                header += ",{\"name\": \""+tag+"\", \"children\": []}";
            }
        }
        header+="]}";
        return header;
    }
}
