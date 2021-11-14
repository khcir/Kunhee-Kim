package gitlet;

import java.io.File;
import java.util.HashMap;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if(args.length == 0){
            //TODO: fill in this line
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        Repository repository = new Repository();
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                repository.init();
                break;
            case "add":
                if(!Repository.GITLET_DIR.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    break;
                }
                // TODO: handle the `add [filename]` command
                repository.add(args[1]);
                break;
            case "commit":
                try{
                    if(!Repository.GITLET_DIR.exists()){
                        System.out.println("Not in an initialized Gitlet directory.");
                        break;
                    }
                    if(args[1].length() == 0){
                        System.out.println("Please enter a commit message");
                        break;
                    }
                    repository.commit(args[1]);
                    break;
                } catch (ArrayIndexOutOfBoundsException e){
                    System.out.println("Please enter a commit message");
                }
            case "checkout":
                if(!Repository.GITLET_DIR.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    break;
                }
                repository.checkout(args);
                break;
            case "log":
                if(!Repository.GITLET_DIR.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    break;
                }
                repository.log();
                break;
            case "rm":
                if(!Repository.GITLET_DIR.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    break;
                }
                repository.rm(args[1]);
                break;
            case "global-log":
                if(!Repository.GITLET_DIR.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    break;
                }
                repository.global_log();
                break;
            case "find":
                if(!Repository.GITLET_DIR.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    break;
                }
                repository.find(args[1]);
                break;
            case "status":
                if(!Repository.GITLET_DIR.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    break;
                }
                repository.status();
                break;
            case "branch":
                if(!Repository.GITLET_DIR.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    break;
                }
                repository.branch(args[1]);
                break;
            case "rm-branch":
                if(!Repository.GITLET_DIR.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    break;
                }
                repository.rm_branch(args[1]);
                break;
            case "reset":
                if(!Repository.GITLET_DIR.exists()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    break;
                }
                repository.reset(args[1]);
                break;
            // TODO: FILL THE REST IN
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }

}
