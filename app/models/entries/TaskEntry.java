package models.entries;

import models.LinkedAccount;
import org.joda.time.DateTimeZone;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by jrenner on 10/29/15.
 */

@Entity
@DiscriminatorValue("task")
public class TaskEntry extends Entry {

    @Column(name = "task_id")
    public String taskId;

    @Column(name="task_type")
    public String taskType ;

    @Column(name="list_name")
    public String listName ;

    @Column(name="task_name")
    public String taskName ;

    /**
     *
     * @param linkedAccountId - The id of the linkedAccount associated with the TaskEntry being created
     * @param time -    The start time of the TaskEntry: will be the creation time of the wunderlist task
     * @param endTimeStr -      The end time of the TaskEntry being created: will be the same as start time
     *                          if the type is creation.
     * @param type  -           The type of the TaskEntry being created: 'created' or 'completed'
     */
    public static TaskEntry create(long linkedAccountId, String taskId, String taskName, String time, String type){
        //Find the LinkedAccount object using the id
        LinkedAccount linked = LinkedAccount.find.where().eq("providerUserId",""+linkedAccountId).findUnique() ;
        List<LinkedAccount> accounts = new ArrayList<LinkedAccount>();
        accounts.add(linked);

        Date startTime = timeZoneAdjust(time) ;

        TaskEntry task = new TaskEntry();
        task.setTaskId(taskId);
        task.setStartTime(startTime);
        task.setEndTime(startTime);
        task.setLinkedAccounts(accounts);
        task.setTaskType(type);
        task.setTaskName(taskName);
        return task ;
    }

    public static Finder<Long, TaskEntry> find = new Finder<Long, TaskEntry>(
            Long.class, TaskEntry.class
    );

    //Format the date string to the Date object and adjust for current time zone.
    //This is the format for Wunderlist: 2013-08-30T08:36:13.273Z
    public static Date timeZoneAdjust(String raw_time){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS", Locale.ENGLISH);
        Date date = null ;
        try{
            // This will parse and convert the provided UTC time to the current time zone time.
            date = new Date(DateTimeZone.getDefault().convertUTCToLocal(df.parse(raw_time).getTime())) ;
        } catch(ParseException e){
            e.printStackTrace();
        }
        return date ;
    }

    // Task type getter/setter
    public void setTaskType(String taskType){
        //Make sure that the input is valid.
        if(taskType.equals("created") || taskType.equals("completed")){
            this.taskType = taskType ;
        }
    }
    public String getTaskType(){
        return this.taskType ;
    }

    public String getTaskId() { return this.taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    // Task name getter/setter
    public void setTaskName(String name) {
        this.taskName = name ;
    }

    public void setEndTime(String end_time){
        this.endTime = timeZoneAdjust(end_time) ;
    }
    public String getTaskName(){
        return this.taskName ;
    }
    
}
