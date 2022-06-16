package com.dogukan.todolistapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Todo(
    @ColumnInfo(name = "title")
    val title : String,
    @ColumnInfo(name = "description")
    val description : String?,
    @ColumnInfo(name = "isDone")
    val isDone : Boolean,
    @PrimaryKey(autoGenerate = true)
    val id : Int?= null
)
