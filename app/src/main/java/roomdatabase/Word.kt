package roomdatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// Здесь хранятся все сущности для бд, все поля, колонки, их обозначения

@Entity(tableName = "word_table")   // название - word_table
data class Word(@ColumnInfo(name = "word") val word: String,                 // название дневника
                @ColumnInfo(name = "description") val description : String,  // описание дневника
                @ColumnInfo(name = "date") var date : String
) : Serializable
{
    // Первичный ключ - id с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0

    @ColumnInfo(name = "img")
    var img : String? = null

}

@Entity(tableName = "note_table")
data class Note(@ColumnInfo(name = "note_name") val note : String,      // название заметки
                @ColumnInfo(name = "text") var text : String,           // текст заметки
                @ColumnInfo(name = "diaryId") val diaryId : Long,       // id дневника, к к-му привязана
                @ColumnInfo(name = "dateNote") var dateNote : String
) : Serializable
{
    // Первичный ключ - idNote с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var idNote : Long = 0

    @ColumnInfo(name = "imgNote")
    var imgNote : String? = null
}
