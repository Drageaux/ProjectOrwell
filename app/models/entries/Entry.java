package models.entries;

import com.avaje.ebean.Model;
import models.AppModel;
import models.LinkedAccount;
import play.data.format.Formats;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.Date;


/**
 * Created by jrenner on 10/29/15.
 */
@Entity
@Table(name = "entries")
@Inheritance(strategy = InheritanceType.JOINED)
public class Entry extends AppModel {

    @Id
    public Long id;

    @ManyToMany
    public LinkedAccount linkedAccount;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date startTime;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date endTime;

    public boolean isInstantaneous() {
        return startTime == endTime;
    }

}
