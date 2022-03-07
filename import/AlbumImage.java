import java.util.List;

public class AlbumImage {
    final String[] columns = {"image_id", "image_sort_no", "image_path"};
    
    private int image_id;
    private int image_sort_no;
    private String image_path;

    private List<String> semantic_labels; //from ImgNet

    private int missing_sort = 0;

    public AlbumImage(Integer release_id, List<String> input){
        image_id = Integer.valueOf(input.get(0));
        String img_sort_input = input.get(1);
        if(img_sort_input.length()<1){ // missing in 6966 cases
            img_sort_input="1";
            missing_sort++;
        }
        image_sort_no = Integer.valueOf(img_sort_input);
        image_path = "/"+release_id+"/"+image_id+".jpg";
    }
    public String toString(){
        String d = "||"; //delimiter
        return d+columns[0]+d+image_id+d+columns[1]+d+image_sort_no;//+d+columns[2]+d+image_path;
    }

    public Integer getMissingSort(){
        return missing_sort;
    }

    public String getImagePath(){
        return image_path;
    }
}
