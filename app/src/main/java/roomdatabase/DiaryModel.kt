package roomdatabase

import androidx.room.*
import java.io.Serializable

// Здесь хранятся все модели(сущности) для бд, все поля, колонки, их обозначения

// Модель дневника
@Entity(tableName = "diary_table")                                              // название таблицы
data class Diary(@ColumnInfo(name = "diary_name") var diary_name: String,       // название дневника
                @ColumnInfo(name = "diary_content") var diary_content : String, // описание
                @ColumnInfo(name = "diary_date") var diary_date : String        // дата создания/изменения
) : Serializable
{
    // Первичный ключ - diary_id с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0                     // id дневника

    @ColumnInfo(name = "diary_img")
    var diaryImg : String? = null              // картинка

    @ColumnInfo(name = "diary_color")
    var color : String? = null            // цвет

    @ColumnInfo(name = "diary_is_favorite")
    var favorite : Boolean = false     // является ли избранным
}


// Модель заметки. Заметки хранятся в дневнике. К одному дневнику может быть привязано несколько
// заметок (связь один-ко-многим)
@Entity(tableName = "note_table")
data class Note(@ColumnInfo(name = "note_name") var note_name : String,
                @ColumnInfo(name = "note_content") var note_content : String,
                @ColumnInfo(name = "note_parent_id") val note_parent_id : Long,     // id дневника, к к-му привязана
                @ColumnInfo(name = "note_date") var note_date : String
) : Serializable
{
    // Первичный ключ - note_id с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var idNote : Long = 0              // id заметки

    @ColumnInfo(name = "note_img")
    var noteImg : String? = null       // картинка

    @ColumnInfo(name = "note_color")
    var color : String? = null     // цвет

}


// Дата-класс, в котором ключевая модель - модель Diary, а связные - все остальные
// Т.е. при помощи полиморфной связи в этом классе соединены несколько моделей
data class ExtendedDiary(@Embedded val diary : Diary,
                         @Relation(parentColumn = "id", entityColumn = "note_parent_id")
                         val notes : List<Note>) : Serializable