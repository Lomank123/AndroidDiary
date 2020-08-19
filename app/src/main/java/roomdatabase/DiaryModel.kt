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

    @ColumnInfo(name = "diary_list_name")
    var listName : String = "List name"            // Название списка дел

    override fun equals(other: Any?): Boolean {

        if(javaClass != other?.javaClass)
            return false

        other as Diary

        if(id != other.id)
            return false
        if(name != other.name)
            return false
        if(content != other.content)
            return false
        if(creationDate != other.creationDate)
            return false
        if(img != other.img)
            return false
        if(color != other.color)
            return false
        if(lastEditDate != other.lastEditDate)
            return false
        if(favorite != other.favorite)
            return false
        if(listName != other.listName)
            return false

        return true
    }
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

    override fun equals(other: Any?): Boolean {

        if(javaClass != other?.javaClass)
            return false

        other as Note

        if(id != other.id)
            return false
        if(name != other.name)
            return false
        if(content != other.content)
            return false
        if(creationDate != other.creationDate)
            return false
        if(img != other.img)
            return false
        if(color != other.color)
            return false
        if(lastEditDate != other.lastEditDate)
            return false
        if(favorite != other.favorite)
            return false

        return true
    }
}

@Entity(tableName = "daily_list_item_table")                                           // Название таблицы
data class DailyListItem(@ColumnInfo(name = "daily_list_item_name") var name : String,          // Название
                         @ColumnInfo(name = "daily_list_item_parent_id") val parentId : Long   // id дневника, к к-му привязана
) : Serializable
{
    // Первичный ключ - id с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0                               // id заметки

    @ColumnInfo(name = "daily_list_item_color")
    var color : String? = null                      // цвет

    @ColumnInfo(name = "daily_list_item_is_done")
    var isDone : Boolean = false                    // сделано ли дело

    override fun equals(other: Any?): Boolean {

        if(javaClass != other?.javaClass)
            return false

        other as DailyListItem

        if(id != other.id)
            return false
        if(name != other.name)
            return false
        if(color != other.color)
            return false

        return true
    }
}

/**
 * Дата-класс, в котором ключевая модель - модель Diary, а связные - все остальные
 * Т.е. при помощи полиморфной связи в этом классе соединены несколько моделей
 */
data class ExtendedDiary(@Embedded val diary : Diary,
                         @Relation(parentColumn = "id", entityColumn = "note_parent_id")
                         val notes : List<Note>,
                         @Relation(parentColumn= "id", entityColumn = "daily_list_item_parent_id")
                         val dailyListItems : List<DailyListItem>) : Serializable