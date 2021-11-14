package gitlet;

import jdk.jshell.execution.Util;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Juchan
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commit");
    public static final File BLOB_DIR = join(GITLET_DIR, "blob");
    public static final File LOGS_DIR = join(GITLET_DIR, "logs");
    public static final File REFS_DIR = join(GITLET_DIR, "ref");
    public static final File STAGE_DIR = join(GITLET_DIR, "staging");


    private String head;
    private String headcommit;
    private StagingArea stagingArea;

    public void setStagingArea(StagingArea stagingArea) {
        this.stagingArea = stagingArea;
    }

    public Repository(){
        this.head = "master";
        this.headcommit = "";
        File stage = Utils.join(STAGE_DIR, "stage.txt");
        stagingArea = new StagingArea();
        if(stage.exists()){
            stagingArea = Utils.readObject(stage, stagingArea.getClass());
        }
        File head = Utils.join(REFS_DIR, "head.txt");
        if(head.exists()){
            this.head = Utils.readContentsAsString(head);
        }
    }
    public Repository(String head){
        this.head = head;
        this.stagingArea = new StagingArea();
    }

    // TODO: FILL THE REST IN
    public void init(){
        //make directories
        if(Repository.GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }

        Repository.GITLET_DIR.mkdir();
        Repository.COMMIT_DIR.mkdir();
        Repository.BLOB_DIR.mkdir();
        Repository.LOGS_DIR.mkdir();
        Repository.REFS_DIR.mkdir();
        Repository.STAGE_DIR.mkdir();

        //make head reference
        File ref_head = Utils.join(Repository.REFS_DIR, "head");
        ref_head.mkdir();
        //head reference of master
        File ref_head_master = Utils.join(ref_head, "master.txt");
        String zeros_40;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i< 40;i++){
            sb.append("0");
        }
        zeros_40 = sb.toString();
        Utils.writeContents(ref_head_master, zeros_40);
        File head = Utils.join(REFS_DIR, "head.txt");
        Utils.writeContents(head, "master");
        //make initial commit, log
        Commit initial = new Commit("initial commit", "user_default", new HashMap<>());
        initialCommit(initial);

        return;
    }

    private static String commit_log(Commit commit){
        return String.format("%s %s <%s> %s\t\tCommit : %s",
                commit.getPrev_head(), commit.getCurr_hash(),
                commit.getAuthor(), commit.getDate(), commit.getMessage());
    }

    public void add(String arg){
        // TODO: handle the `add [filename]` command
        String filename = arg;
        File curr_file = Utils.join(Repository.CWD, filename);
        if(!curr_file.exists()){
            System.out.println("File does not exists");
            return;
        }
        String add_title = Utils.sha1(Utils.readContentsAsString(Utils.join(Repository.CWD, filename)) +filename);
        File stage = Utils.join(STAGE_DIR, "stage.txt");
        File add = Utils.join(Repository.BLOB_DIR, add_title+".txt");
        if(add.exists()){
            if(stagingArea.getRemoved_files().contains(filename)){
                stagingArea.getRemoved_files().remove(filename);
                setStagingArea(stagingArea);
                stage = Utils.join(STAGE_DIR, "stage.txt");
                Utils.writeObject(stage, stagingArea);
                return;
            }
            return;
        }

        Utils.writeContents(add, Utils.readContentsAsString(curr_file));

        stagingArea.put(filename, add_title);
        setStagingArea(stagingArea);

        Utils.writeObject(stage, stagingArea);

    }

    public void initialCommit(Commit initial_commit){
        // TODO: handle the `add [filename]` command
        File ref_head = Utils.join(Repository.REFS_DIR, "head");
        File ref_head_master = Utils.join(ref_head, "master.txt");
        HashMap<String, String> updatedBlobMaps = new HashMap<>();

        Commit commit = initial_commit;
        String commit_hash = commit.getCurr_hash();
        //store commit into commit
        File addCommitToBlob = Utils.join(Repository.COMMIT_DIR, commit_hash+".txt");
        Utils.writeObject(addCommitToBlob, commit);

        //store commit into head
        Utils.writeContents(ref_head_master, commit_hash);

        //store commit into logs
        File logsForHead = Utils.join(Repository.LOGS_DIR, "HEAD.txt");
        Utils.writeContents(logsForHead, commit_log(commit));

        String zeros_40;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i< 40;i++){
            sb.append("0");
        }
        zeros_40 = sb.toString();
        commit = new Commit(zeros_40, "initial commit", "user_default", new HashMap<>());
        commit_hash = commit.getCurr_hash();
        //store commit into commit
        addCommitToBlob = Utils.join(Repository.COMMIT_DIR, commit_hash+".txt");
        Utils.writeObject(addCommitToBlob, commit);

        //store commit into head
        Utils.writeContents(ref_head_master, commit_hash);

        //store commit into logs
        File initLogsForHead = Utils.join(Repository.LOGS_DIR, "HEAD.txt");
        String pastContents = new String();
        if(initLogsForHead.exists()){
            pastContents = Utils.readContentsAsString(initLogsForHead);
        }
        Utils.writeContents(initLogsForHead,  pastContents + "\n"
                +commit_log(commit));

        File headCommit = Utils.join(REFS_DIR, "headcommit.txt");
        Utils.writeContents(headCommit, commit_hash);
    }

    public void commit(String message){
        // TODO: handle the `add [filename]` command
        if(stagingArea.getModified_files().isEmpty()
                &&stagingArea.getRemoved_files().isEmpty()
                &&stagingArea.getAdded_files().isEmpty()){
            System.out.println("No changes added to the commit");
            return;
        }

        File ref_head = Utils.join(Repository.REFS_DIR, "head");
        File currBranchFile = Utils.join(Repository.REFS_DIR, "headcommit.txt");

        File prev_commit_file = Utils.join(Repository.COMMIT_DIR, Utils.readContentsAsString(currBranchFile)+".txt");
        Commit prev_commit;
        HashMap<String, String> updatedBlobMaps = new HashMap<>();
        if(prev_commit_file.exists()){
            prev_commit= Utils.readObject(prev_commit_file, Commit.class);
            updatedBlobMaps = prev_commit.getBlobmaps();
        }
        updatedBlobMaps.putAll(stagingArea.getAdded_files());
        updatedBlobMaps.putAll(stagingArea.getModified_files());
        updatedBlobMaps.remove(stagingArea.getRemoved_files());

        Commit commit = new Commit(Utils.readContentsAsString(currBranchFile),
                message, "default author", updatedBlobMaps
        );

        String commit_hash = commit.getCurr_hash();
        //store commit into commit
        File addCommitToBlob = Utils.join(Repository.COMMIT_DIR, commit_hash+".txt");
        Utils.writeObject(addCommitToBlob, commit);

        //store commit into head
        Utils.writeContents(currBranchFile, commit_hash);

        File currBranchfiles = Utils.join(Repository.REFS_DIR, "head.txt");
        String currBranch = Utils.readContentsAsString(currBranchfiles)+".txt";
        currBranchFile = Utils.join(ref_head, currBranch);
        Utils.writeContents(currBranchFile, commit_hash);

        //store commit into logs
        File logsForHead = Utils.join(Repository.LOGS_DIR, "HEAD.txt");
        String pastContents = new String();
        if(logsForHead.exists()){
            pastContents = Utils.readContentsAsString(logsForHead);
        }
        Utils.writeContents(logsForHead,  pastContents + "\n"
                +commit_log(commit));
        //clear staging Area
        stagingArea.clear();
        File stage = Utils.join(STAGE_DIR, "stage.txt");
        Utils.writeObject(stage, stagingArea);
        File headCommit = Utils.join(REFS_DIR, "headcommit.txt");
        Utils.writeContents(headCommit, commit_hash);
    }

    public void checkout(String[] args){
        switch (args.length){
            //error message
            case 1:
                break;
            //branch
            case 2:
                String branchName = args[1];
                File refhead = Utils.join(Repository.REFS_DIR, "head");
                File currBranchFile = Utils.join(Repository.REFS_DIR, "head.txt");
                String currBranch = Utils.readContentsAsString(currBranchFile)+".txt";

                File headfile = Utils.join(refhead, currBranch);
                String headfileString = Utils.readContentsAsString(headfile);
                File headCommitFile = Utils.join(Repository.COMMIT_DIR, headfileString+".txt");
                Commit currCommit = Utils.readObject(headCommitFile, Commit.class);
                File head = Utils.join(REFS_DIR, "head.txt");
                this.head = Utils.readContentsAsString(head);
                if(branchName.equals(Utils.readContentsAsString(currBranchFile))){
                    System.out.println("No need to checkout the current branch");
                    return;
                }

                refhead = Utils.join(Repository.REFS_DIR, "head");
                File branchfile = Utils.join(refhead, branchName + ".txt");
                if(!branchfile.exists()) {
                    System.out.println("No such branch exists.");
                    return;
                }

                String branchfileString = Utils.readContentsAsString(branchfile);
                File branchCommitFile = Utils.join(Repository.COMMIT_DIR, branchfileString+".txt");
                Commit branchCommit = Utils.readObject(branchCommitFile, Commit.class);

                List<File> fileList = new ArrayList<>();
                for(File f : Repository.CWD.listFiles()){
                    if (f.getName().endsWith(".txt")) {
                        fileList.add(f);
                    }
                }

                for(File f : fileList){
                    if(branchCommit.getBlobmaps().containsKey(f.getName())
                    && !currCommit.getBlobmaps().containsKey(f.getName())){
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }
                }
                for(File f : fileList){
                    if(!branchCommit.getBlobmaps().containsKey(f.getName())
                            && currCommit.getBlobmaps().containsKey(f.getName())){
                        Utils.restrictedDelete(f);
                    }
                }

                for(String filename : branchCommit.getBlobmaps().keySet()){
                    File workingDirectoryFile = Utils.join(Repository.CWD, filename);
                    File blobFile = Utils.join(Repository.BLOB_DIR, branchCommit.getBlobmaps().get(filename) + ".txt");
                    Utils.writeContents(workingDirectoryFile, Utils.readContentsAsString(blobFile));
                }
                this.head = branchName;
                Utils.writeContents(head, this.head);
                break;
            //filename
            case 3:
                String filename = args[2];
                File ref_head = Utils.join(Repository.REFS_DIR, "headcommit.txt");
                String headCommitRef = Utils.readContentsAsString(ref_head);
                headCommitFile = Utils.join(Repository.COMMIT_DIR, headCommitRef + ".txt");
                Commit headCommit = Utils.readObject(headCommitFile, Commit.class);

                String blobHash = headCommit.getBlobmaps().get(filename);
                if(blobHash == null){
                    System.out.println("File does not exist in that commit.");
                    return;
                }
                File blobHashFile = Utils.join(Repository.BLOB_DIR, blobHash + ".txt");

                File destPath = Utils.join(Repository.CWD, filename);
                Utils.writeContents(destPath, Utils.readContentsAsString(blobHashFile));
                break;
            //commit-id, file name
            case 4:
                String commitID = args[1];
                if(commitID.length() < 40){
                    List<String> list = Utils.plainFilenamesIn(Repository.COMMIT_DIR);
                    boolean isExist = false;
                    for(String listname : list){
                        if(listname.startsWith(commitID)) {
                            commitID = listname;
                            isExist = true;
                            break;
                        }
                        else continue;
                    }
                    if(!isExist){
                        System.out.println("No commit with that id exists.");
                        return;
                    }
                    commitID = commitID.substring(0,commitID.length()-4);
                }

                if(!args[2].equals("--")){
                    System.out.println("Incorrect operands.");
                    return;
                }
                String fileNameInArgs4 = args[3];

                File CommitFile = Utils.join(Repository.COMMIT_DIR, commitID +".txt");
                if(!CommitFile.exists()){
                    System.out.println("No commit with that id exists.");
                    return;
                }
                Commit commit = Utils.readObject(CommitFile, Commit.class);

                String blobHashInArgs4 = commit.getBlobmaps().get(fileNameInArgs4);
                if(blobHashInArgs4 == null){
                    System.out.println("File does not exist in that commit.");
                    return;
                }

                File blobHashFileInArgs4 = Utils.join(Repository.BLOB_DIR, blobHashInArgs4 + ".txt");

                File destPathInArgs4 = Utils.join(Repository.CWD, fileNameInArgs4);
                Utils.writeContents(destPathInArgs4, Utils.readContentsAsString(blobHashFileInArgs4));
                break;
        }
    }

    public void log(){
        File ref_head = Utils.join(Repository.REFS_DIR, "headcommit.txt");

        String headCommitRef = Utils.readContentsAsString(ref_head);
        File headCommitFile = Utils.join(Repository.COMMIT_DIR, headCommitRef + ".txt");
        Commit headCommit = Utils.readObject(headCommitFile, Commit.class);

        Commit currCommit = headCommit;

        while(!(currCommit.getDate().equals("00:00:00 UTC, Thursday, 1 January 1970"))){
            System.out.println("===");
            System.out.println(logHelper(currCommit));
            String prevCommitHash = currCommit.getPrev_hash();
            File prevCommitFile = Utils.join(Repository.COMMIT_DIR, prevCommitHash + ".txt");
            currCommit = Utils.readObject(prevCommitFile, Commit.class);
        }
    }

    public String logHelper(Commit commit){

        return String.format("commit %s\nDate: %s\n%s\n",
                commit.getCurr_hash(), commit.getDate(), commit.getMessage());
    }

    public void rm(String arg){
        String filename = arg;

        List<File> fileList = new ArrayList<>();
        for(File f : Repository.CWD.listFiles()){
            if (f.getName().endsWith(".txt")) {
                fileList.add(f);
            }
        }
        //stage
        if(stagingArea.getAdded_files().containsKey(filename)){
            stagingArea.getAdded_files().remove(filename);
            setStagingArea(stagingArea);
            File stage = Utils.join(STAGE_DIR, "stage.txt");
            Utils.writeObject(stage, stagingArea);
            return;
        }

//        boolean istracked = false;
//        for(File f : fileList){
//            if(f.getName().equals(filename)){
//                if(!stagingArea.getRemoved_files().contains(filename)){
//                    stagingArea.getRemoved_files().add(filename);
//                    setStagingArea(stagingArea);
//                    File stage = Utils.join(STAGE_DIR, "stage.txt");
//                    Utils.writeObject(stage, stagingArea);
//                }
//                istracked = true;
//                break;
//            }
//        }

        //commit
        File ref_head = Utils.join(Repository.REFS_DIR, "headcommit.txt");

        String headCommitRef = Utils.readContentsAsString(ref_head);
        File headCommitFile = Utils.join(Repository.COMMIT_DIR, headCommitRef + ".txt");
        Commit headCommit = Utils.readObject(headCommitFile, Commit.class);

        if(headCommit.getBlobmaps().containsKey(filename)){
            if(!stagingArea.getRemoved_files().contains(filename)){
                stagingArea.getRemoved_files().add(filename);
                setStagingArea(stagingArea);
                File stage = Utils.join(STAGE_DIR, "stage.txt");
                Utils.writeObject(stage, stagingArea);
            }
            File toDeleteFile = Utils.join(Repository.CWD, filename);
            Utils.restrictedDelete(toDeleteFile);
        }
        //neither
        if(!stagingArea.getAdded_files().containsKey(filename)&&
                !headCommit.getBlobmaps().containsKey(filename)){
            System.out.println("No reason to remove the file.");
            return;
        }
    }

    public void global_log(){
        List<String> commitlist = Utils.plainFilenamesIn(Repository.COMMIT_DIR);
        for(String commitName : commitlist){
            if(commitName.equals("0000000000000000000000000000000000000000.txt")) continue;
            File currCommitFile = Utils.join(Repository.COMMIT_DIR, commitName);
            Commit currCommit = Utils.readObject(currCommitFile, Commit.class);
            System.out.println("===");
            System.out.println(logHelper(currCommit));
        }
    }

    public void find(String commit_message){
        List<String> commitlist = Utils.plainFilenamesIn(Repository.COMMIT_DIR);
        boolean isExist = false;

        for(String commitName : commitlist){
            if(commitName.equals("0000000000000000000000000000000000000000.txt")) continue;
            File currCommitFile = Utils.join(Repository.COMMIT_DIR, commitName);
            Commit currCommit = Utils.readObject(currCommitFile, Commit.class);
            if(currCommit.getMessage().equals(commit_message)){
                System.out.println(currCommit.getCurr_hash());
                isExist = true;
            }
        }
        if(!isExist) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    public void status(){
        //branches
        System.out.println("=== Branches ===");
        File ref_head = Utils.join(Repository.REFS_DIR, "head");

        File ref_headtxt = Utils.join(Repository.REFS_DIR, "head.txt");
        for(String filename : Utils.plainFilenamesIn(ref_head)){
            if(filename.substring(0,filename.length()-4).equals(Utils.readContentsAsString(ref_headtxt))){
                System.out.print('*');
            }

            System.out.println(filename.substring(0,filename.length()-4));
        }
        System.out.println();

        //staged
        System.out.println("=== Staged Files ===");
        List<String> staged = new ArrayList<>();
        for(String filename : stagingArea.getAdded_files().keySet()){
            staged.add(filename);
        }
        staged.stream().sorted().collect(Collectors.toList());
        for(String filename : staged){
            System.out.println(filename);
        }
        System.out.println();

        //removed
        System.out.println("=== Removed Files ===");
        List<String> removed = new ArrayList<>();
        for(String filename : stagingArea.getRemoved_files()){
            removed.add(filename);
        }
        removed.stream().sorted().collect(Collectors.toList());
        for(String filename : removed){
            System.out.println(filename);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();

//        //Modifications Not staged for commit
//        File ref_head = Utils.join(Repository.REFS_DIR, "head");
//        File ref_head_master = Utils.join(ref_head, "master.txt");
//        String headCommitRef = Utils.readContentsAsString(ref_head_master);
//        File headCommitFile = Utils.join(Repository.COMMIT_DIR, headCommitRef + ".txt");
//        Commit headCommit = Utils.readObject(headCommitFile, Commit.class);
//        if(headCommit.getBlobmaps().containsKey())

    }

    public void branch(String branchName){
        File ref_head = Utils.join(Repository.REFS_DIR, "head");
        File branch = Utils.join(ref_head, branchName + ".txt");
        if(branch.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        File headfile = Utils.join(ref_head, this.head+".txt");
        Utils.writeContents(branch, Utils.readContentsAsString(headfile));
    }

    public void rm_branch(String branchName){
        File currBranchFile = Utils.join(Repository.REFS_DIR, "head.txt");
        String currBranch = Utils.readContentsAsString(currBranchFile);
        if(branchName.equals(currBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        File ref_head = Utils.join(Repository.REFS_DIR, "head");
        File branch = Utils.join(ref_head, branchName + ".txt");
        if(!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        branch.delete();
    }

    public void reset(String commitID){
        if(commitID.length() < 40){
            List<String> list = Utils.plainFilenamesIn(Repository.COMMIT_DIR);
            boolean isExist = false;
            for(String listname : list){
                if(listname.startsWith(commitID)) {
                    commitID = listname;
                    isExist = true;
                    break;
                }
                else continue;
            }
            if(!isExist){
                System.out.println("No commit with that id exists.");
                return;
            }
            commitID = commitID.substring(0,commitID.length()-4);
        }
        File commitNewFile = Utils.join(COMMIT_DIR, commitID+".txt");
        if(!commitNewFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commitNew = Utils.readObject(commitNewFile, Commit.class);
        File ref_head = Utils.join(Repository.REFS_DIR, "head");
        File currBranch = Utils.join(Repository.REFS_DIR, "head.txt");
        String currBranchHead = Utils.readContentsAsString(currBranch);
        Utils.writeContents(Utils.join(ref_head, currBranchHead+".txt"), commitNew.getPrev_hash());

        List<File> fileList = new ArrayList<>();
        for(File f : Repository.CWD.listFiles()){
            if (f.getName().endsWith(".txt")) {
                fileList.add(f);
            }
        }


        Commit currCommit = Utils.readObject(
                Utils.join(COMMIT_DIR, Utils.readContentsAsString(
                        Utils.join(REFS_DIR, "headcommit.txt")) + ".txt")
                , Commit.class
        );

        for(File f : fileList){
            if(!currCommit.getBlobmaps().containsKey(f.getName()) && commitNew.getBlobmaps().containsKey(f.getName())){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        for(File f : fileList){
            if(!commitNew.getBlobmaps().containsKey(f.getName())
                    && currCommit.getBlobmaps().containsKey(f.getName())){
                Utils.restrictedDelete(f);
            }
        }

        for(String filename : commitNew.getBlobmaps().keySet()){
            File workingDirectoryFile = Utils.join(Repository.CWD, filename);
            File blobFile = Utils.join(Repository.BLOB_DIR, commitNew.getBlobmaps().get(filename) + ".txt");
            Utils.writeContents(workingDirectoryFile, Utils.readContentsAsString(blobFile));
        }
        Utils.writeContents(Utils.join(ref_head, "headcommit.txt"), commitNew.getPrev_hash());
    }

}
