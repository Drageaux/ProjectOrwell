package models.entries;

import models.LinkedAccount;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jrenner on 10/29/15.
 */

@Entity
@DiscriminatorValue("task")
public class TaskEntry extends Entry {

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
    public static TaskEntry create(long linkedAccountId, String taskName, String time, String type){
        //Find the LinkedAccount object using the id
        LinkedAccount linked = LinkedAccount.find.byId(linkedAccountId) ;

        //Format the date string to the Date object.
        //This is the format for Wunderlist: 2013-08-30T08:36:13.273Z
        DateFormat df = new SimpleDateFormat("yyyy-mm-dd kk:mm:ss.SSS", Locale.ENGLISH);
        Date startTime = null, endTime = null ;
        try{
            startTime =  df.parse(time);
        } catch(ParseException e){
            e.printStackTrace();
        }

        TaskEntry task = new TaskEntry() ;
        task.setStartTime(startTime);
        task.setEndTime(startTime);
        task.setLinkedAccount(linked);
        task.setTaskType(type);
        task.setTaskName(taskName);
        return task ;
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

    // Task name getter/setter
    public void setTaskName(String name) {
        this.taskName = name ;
    }
    public String getTaskName(){
        return this.taskName ;
    }


}
