package models.entries;

import models.LinkedAccount;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jrenner on 10/29/15.
 */
public class TaskEntry extends Entry {


    String type ;

    public TaskEntry(){
        //default constructor
    }

    public TaskEntry(LinkedAccount linkedAccount, Date startTime, Date endTime, String type){
        super(linkedAccount, startTime, endTime) ;
        this.type = type ;
    }

    /**
     *
     * @param linkedAccountId - The id of the linkedAccount associated with the TaskEntry being created
     * @param startTimeStr -    The start time of the TaskEntry: will be the creation time of the wunderlist task
     * @param endTimeStr -      The end time of the TaskEntry being created: will be the same as start time
     *                          if the type is creation.
     * @param type  -           The type of the TaskEntry being created: 'creation' or 'completion'
     */
    public static void create(long linkedAccountId, String startTimeStr, String endTimeStr, String type){
        //Find the LinkedAccount object using the id
        LinkedAccount linked = LinkedAccount.find.byId(linkedAccountId) ;

        //Format the date string to the Date object.
        //This is the format for Wunderlist: 2013-08-30T08:36:13.273Z
        DateFormat df = new SimpleDateFormat("yyyy-mm-ddTkk:mm:ss.zzzZ", Locale.ENGLISH);
        Date startTime = null, endTime = null ;
        try{
            startTime =  df.parse(startTimeStr);
            endTime = df.parse(endTimeStr) ;
        } catch(ParseException e){
            e.printStackTrace();
        }

        TaskEntry task = new TaskEntry(linked, startTime, endTime, type) ;
        task.save() ;

    }

}
