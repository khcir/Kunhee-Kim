package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StagingArea implements Serializable {
    private HashMap<String, String> added_files;
    private List<String> removed_files;
    private HashMap<String, String> modified_files;

    public HashMap<String, String> getAdded_files() {
        return added_files;
    }

    public List<String> getRemoved_files() {
        return removed_files;
    }

    public HashMap<String, String> getModified_files() {
        return modified_files;
    }

    public StagingArea(){
        this.added_files = new HashMap<String, String>();
        this.removed_files = new ArrayList<String>();
        this.modified_files = new HashMap<String, String>();
    }

    public StagingArea(HashMap<String, String> added_files, List<String> removed_files, HashMap<String, String> modified_files,HashMap<String, String> final_staging_files ){
        this.added_files = added_files;
        this.removed_files = removed_files;
        this.modified_files = modified_files;
    }

    public void put(String filename, String value){
        added_files.put(filename,value);
    }

    public void clear(){
        added_files.clear();
        modified_files.clear();
        removed_files.clear();
    }
}
