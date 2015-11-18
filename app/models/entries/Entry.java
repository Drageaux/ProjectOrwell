package models.entries;

import models.AppModel;
import models.LinkedAccount;
import play.data.format.Formats;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by jrenner on 10/29/15.
 */
@Entity
@Table(name = "entries")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "ENTRY_TYPE", discriminatorType = DiscriminatorType.STRING)
public class Entry extends AppModel {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToMany
    public List<LinkedAccount> linkedAccounts = new ArrayList<LinkedAccount>();

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date startTime;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date endTime;


    public static Finder<Long, Entry> find = new Finder<Long, Entry>(
            Long.class, Entry.class
    );


    public boolean isInstantaneous() {
        return startTime == endTime;
    }

    // Linked Account getter/setter
    public List<LinkedAccount> getLinkedAccounts(){
        return this.linkedAccounts ;
    }
    public void setLinkedAccounts(List<LinkedAccount> accts){
        this.linkedAccounts = accts ;
    }

    // Start Time getter/setter
    public Date getStartTime(){
        return this.startTime ;
    }
    public void setStartTime(Date time){
        this.startTime = time ;
    }

    // End Time getter/setter
    public Date getEndTime(){
        return this.endTime ;
    }
    public void setEndTime(Date time){
        this.endTime = time ;
    }

}
