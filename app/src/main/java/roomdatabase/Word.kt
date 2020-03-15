package roomdatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Здесь хранятся все сущности для бд, все поля, колонки, их обозначения

@Entity(tableName = "word_table")   // название - word_table
data class Word(@ColumnInfo(name = "word") val word: String,                 // название дневника
                @ColumnInfo(name = "description") val description : String,  // описание дневника
                @ColumnInfo(name = "date") val date : String,
                @ColumnInfo(name = "img") val img : String?
) {
    // Первичный ключ - id с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
}

@Entity(tableName = "note_table")
data class Note(@ColumnInfo(name = "note_name") val note : String,      // название заметки
                @ColumnInfo(name = "text") val text : String,           // текст заметки
                @ColumnInfo(name = "diaryId") val diaryId : Long,       // id дневника, к к-му привязана
                @ColumnInfo(name = "dateNote") val dateNote : String,
                @ColumnInfo(name = "imgNote") val imgNote : String?
) {
    // Первичный ключ - idNote с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var idNote : Long = 0
}
