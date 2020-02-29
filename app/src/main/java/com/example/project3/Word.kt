package com.example.project3

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_table")
data class Word(@ColumnInfo(name = "word") val word: String,
                @ColumnInfo(name = "description") val description : String
)
{
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0

}

//@Entity(tableName = "note_table")
//data class Note(@PrimaryKey(autoGenerate = true) var id : String,
//                @ColumnInfo(name = "note_name") val note : String,
//                @ColumnInfo(name = "text") val text : String
//){}
