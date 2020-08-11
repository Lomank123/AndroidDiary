package roomdatabase

import androidx.room.*
import java.io.Serializable

/**
 * Здесь хранятся все модели(сущности) для бд, все поля, колонки, их обозначения
 */

/**
 * Модель дневника
 */
@Entity(tableName = "diary_table")                                        // название таблицы
data class Diary(@ColumnInfo(name = "diary_name") var name: String,       // название дневника
                 @ColumnInfo(name = "diary_content") var content: String, // описание
                 @ColumnInfo(name = "diary_date") var lastEditDate : String       // дата последнего изменения
) : Serializable
{
    // Первичный ключ - id с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0                              // id дневника

    @ColumnInfo(name = "diary_img")
    var img : String? = null                       // картинка

    @ColumnInfo(name = "diary_color")
    var color : String? = null                     // цвет

    @ColumnInfo(name = "diary_is_favorite")
    var favorite : Boolean = false                 // является ли избранным

    @ColumnInfo(name = "diary_creation_date")
    var creationDate : String? = null              // Дата создания
}


/**
 * Модель заметки. Заметки хранятся в дневнике. К одному дневнику может быть привязано несколько
 * заметок (связь один-ко-многим)
 */
@Entity(tableName = "note_table")                                           // Название таблицы
data class Note(@ColumnInfo(name = "note_name") var name : String,          // Название
                @ColumnInfo(name = "note_content") var content : String,    // Текст
                @ColumnInfo(name = "note_parent_id") val parentId : Long,   // id дневника, к к-му привязана
                @ColumnInfo(name = "note_date") var lastEditDate : String           // Дата последнего изменения
) : Serializable
{
    // Первичный ключ - id с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0                               // id заметки

    @ColumnInfo(name = "note_img")
    var img : String? = null                        // картинка

    @ColumnInfo(name = "note_color")
    var color : String? = null                      // цвет

    @ColumnInfo(name = "note_is_favorite")
    var favorite : Boolean = false                  // является ли избранным

    @ColumnInfo(name = "note_creation_date")
    var creationDate : String? = null               // Дата создания
}

/**
 * Дата-класс, в котором ключевая модель - модель Diary, а связные - все остальные
 * Т.е. при помощи полиморфной связи в этом классе соединены несколько моделей
 */
data class ExtendedDiary(@Embedded val diary : Diary,
                         @Relation(parentColumn = "id", entityColumn = "note_parent_id")
                         val notes : List<Note>) : Serializable