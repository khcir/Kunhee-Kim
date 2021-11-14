package gitlet;

// TODO: any imports you need here

import jdk.jshell.execution.Util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String prev_hash;
    private String curr_hash;
    private String message;
    private String date;
    private String author;
    private HashMap<String, String> blobmaps; //<filename, hash>


    public String getPrev_hash() {
        return prev_hash;
    }

    public String getCurr_hash() {
        return curr_hash;
    }

    public HashMap<String, String> getBlobmaps() {
        return blobmaps;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getPrev_head() {
        return prev_hash;
    }

    /* TODO: fill in the rest of this class. */
    public Commit(String message, String author, HashMap<String, String> blobHashMap){
        String zeros_40;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i< 40;i++){
            sb.append("0");
        }
        zeros_40 = sb.toString();
        this.prev_hash= zeros_40;
        this.curr_hash = zeros_40;
        this.message = message;
        this.date = "00:00:00 UTC, Thursday, 1 January 1970";
        this.author = author;
        this.blobmaps = blobHashMap;
    }
    public Commit(String prev_hash, String message, String author, HashMap<String, String> blobHashMap){
        this.prev_hash= prev_hash;
        this.curr_hash = Utils.sha1(Utils.serialize(this));
        this.message = message;
        Date now = new Date();
        SimpleDateFormat simpleDateFormat
                = new SimpleDateFormat("EEE MMM dd HH:MM:ss yyyy Z", new Locale("en", "US"));
        this.date = simpleDateFormat.format(now);
        this.author = author;
        this.blobmaps = blobHashMap;
    }
}
