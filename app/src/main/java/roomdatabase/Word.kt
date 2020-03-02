package roomdatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Здесь хранятся все сущности для бд, все поля, колонки, их обозначения

@Entity(tableName = "word_table")
data class Word(@ColumnInfo(name = "word") val word: String,
                @ColumnInfo(name = "description") val description : String
)
{
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0

}

@Entity(tableName = "note_table")
data class Note(@ColumnInfo(name = "note_name") val note : String,
                @ColumnInfo(name = "text") val text : String,
                @ColumnInfo(name = "diaryId") val diaryId : Long
){
    @PrimaryKey(autoGenerate = true)
    var idNote : Long = 0
    }
