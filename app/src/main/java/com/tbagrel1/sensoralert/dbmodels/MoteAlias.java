package com.tbagrel1.sensoralert.dbmodels;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Class representing an alias set for a specific mote.
 */
@Entity(tableName = "mote_aliases")
public class MoteAlias {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "name")
    @NonNull
    public String name;

    public MoteAlias(@NonNull String id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("MoteAlias { id = %s, name = %s }", id, name);
    }
}
