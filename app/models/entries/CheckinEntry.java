package models.entries;

import models.LinkedAccount;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Austin on 12/5/2015.
 * Model for entries created from Facebook check-ins
 */

@Entity
@DiscriminatorValue("checkin")
public class CheckinEntry extends Entry{

    String location ;

    String taggedFriends ;

    public CheckinEntry(){
        super() ;
    }

    public CheckinEntry(String loc, String friends){
        super() ;
        this.location = loc ;
        this.taggedFriends = friends ;
    }


    /***********************************************************
     * Create and return a new CheckinEntry object.
     ***********************************************************/
    public CheckinEntry create(long linkedAccountId, Date date, String loc, String friends){

        //Find the LinkedAccount object using the id
        LinkedAccount linked = LinkedAccount.find.where().eq("providerUserId",""+linkedAccountId).findUnique() ;
        List<LinkedAccount> accounts = new ArrayList<LinkedAccount>();
        accounts.add(linked);

        CheckinEntry entry = new CheckinEntry() ;

        //General entry fields
        entry.setStartTime(date) ;
        entry.setEndTime(date) ; // (start time) == (end time) because event is instantaneous
        entry.setLinkedAccounts(null) ;

        //Checkin specific fields
        entry.setLocation(loc) ;
        entry.setTaggedFriends(friends) ;
        return entry ;
    }


    /***********************************
     * Finder
     ***********************************/
    public static Finder<Long, CheckinEntry> find = new Finder<Long, CheckinEntry>(
            Long.class, CheckinEntry.class
    );

    /********************************************
     * Field getters & setters
     ********************************************/
    public void setLocation(String loc){
        this.location = loc ;
    }
    public String getLocation(){
        return this.location ;
    }

    public void setTaggedFriends(String friends){
        this.taggedFriends = friends ;
    }
    public String getTaggedFriends(){
        return this.taggedFriends ;
    }

}
