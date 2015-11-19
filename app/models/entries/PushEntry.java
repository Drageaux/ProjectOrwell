package models.entries;

import models.LinkedAccount;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Austin on 11/17/2015.
 */
@Entity
@DiscriminatorValue("push")
public class PushEntry extends Entry {

    public String repositoryName ;

    public String pusherName ;

    public String repositoryURL ;

    public String commitURL ;

    public String commitMessage ;

    public static PushEntry create(long linkedAccountId, Date pushTime, String pusherName,
                                   String repositoryName, String commitURL, String repositoryURL, String commitMessage){
        //Find the LinkedAccount object using the id
        LinkedAccount linked = LinkedAccount.find.where().eq("providerUserId",""+linkedAccountId).findUnique() ;
        List<LinkedAccount> accounts = new ArrayList<LinkedAccount>();
        accounts.add(linked);

        // Create a new PushEntry with the default constructor provided through Entry.
        PushEntry push = new PushEntry() ;

        // Set all of the fields using the supplied values.
        push.setLinkedAccounts(accounts);
        // Instantaneous event, start time = end time.
        push.setStartTime(pushTime);
        push.setEndTime(pushTime);

        // These fields are PushEntry specific.
        push.setRepositoryName(repositoryName) ;
        push.setPusherName(pusherName) ;
        push.setCommitURL(commitURL) ;
        push.setRepositoryURL(repositoryURL) ;
        push.setCommitMessage(commitMessage) ;

        // Return the created PushEntry object (used to save to DB)
        return push ;
    }

    /* ******************************************************
     * * Getters and setters for PushEntry specific fields. *
     * ****************************************************** */
    public void setRepositoryName(String repositoryName){
        this.repositoryName = repositoryName ;
    }
    public String getRepositoryName(){
        return this.repositoryName ;
    }

    public void setPusherName(String pusherName){
        this.pusherName = pusherName ;
    }
    public String getPusherName(){
        return this.pusherName ;
    }

    public void setRepositoryURL(String repositoryURL){
        this.repositoryURL = repositoryURL ;
    }
    public String getRepositoryURL(){
        return this.repositoryURL ;
    }

    public void setCommitURL(String commitURL){
        this.commitURL = commitURL ;
    }
    public String getCommitURL(){
        return this.commitURL ;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }
    public String getCommitMessage(){
        return this.commitMessage ;
    }
}
