package roomdatabase

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.lang.reflect.Type


/**
 * Здесь хранятся все модели(сущности) для бд, все поля, колонки, их обозначения
 */

/**
 * Модель дневника
 */
class Converters {
    @TypeConverter // note this annotation
    fun fromStringList(list : List<String?>?): String? {
        if (list == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<String?>?>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter // note this annotation
    fun toStringList(listString : String?): List<String?>? {
        if (listString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<String?>?>() {}.type
        return gson.fromJson(listString, type)
    }
}

@Entity(tableName = "diary_table")                                        // название таблицы
data class Diary(
    @ColumnInfo(name = "diary_name") var name: String        // название дневника
) : Serializable
{
    // Первичный ключ - id с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0                              // id дневника

    @ColumnInfo(name = "diary_img")
    var img : String? = null                       // картинка

    @ColumnInfo(name = "diary_color")
    var color : Int? = null                        // цвет

    @ColumnInfo(name = "diary_is_favorite")
    var favorite : Boolean = false                 // является ли избранным

    @ColumnInfo(name = "diary_creation_date")
    var creationDate : String? = null              // Дата создания

    @ColumnInfo(name = "diary_list_name")
    var listName : String? = null                  // Название списка дел

    @ColumnInfo(name = "diary_date")
    var lastEditDate : String? = null              // дата последнего изменения

    @ColumnInfo(name = "diary_content")
    var content: String? = null                    // описание

    @ColumnInfo(name = "diary_isExpanded")
    var isExpanded : Boolean = false               // развернута ли доп. информация

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
        if(isExpanded != other.isExpanded)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (img?.hashCode() ?: 0)
        result = 31 * result + (color ?: 0)
        result = 31 * result + favorite.hashCode()
        result = 31 * result + (creationDate?.hashCode() ?: 0)
        result = 31 * result + (listName?.hashCode() ?: 0)
        result = 31 * result + (lastEditDate?.hashCode() ?: 0)
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + isExpanded.hashCode()
        return result
    }
}


/**
 * Модель заметки. Заметки хранятся в дневнике. К одному дневнику может быть привязано несколько
 * заметок (связь один-ко-многим)
 */
@Entity(tableName = "note_table")                                           // Название таблицы
data class Note(
    @ColumnInfo(name = "note_name") var name: String,          // Название
) : Serializable
{
    // Первичный ключ - id с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0                               // id заметки

    @ColumnInfo(name = "note_content")
    var content : String = ""                       // содержимое

    @ColumnInfo(name = "note_parent_id")
    var parentId : Long? = null                     // id дневника, к к-му привязана

    @ColumnInfo(name = "note_img")
    var img : String? = null                        // картинка

    @ColumnInfo(name = "note_images")
    var images : List<String?>? = null

    @ColumnInfo(name = "note_color")
    var color : Int? = null                         // цвет

    @ColumnInfo(name = "note_is_voice")
    var voice : Boolean = false                     // есть ли голосовая заметка

    @ColumnInfo(name = "note_is_favorite")
    var favorite : Boolean = false                  // является ли избранным

    @ColumnInfo(name = "note_date")
    var lastEditDate : String? = null               // Дата последнего изменения

    @ColumnInfo(name = "note_creation_date")
    var creationDate : String? = null               // Дата создания

    @ColumnInfo(name = "note_isExpanded")
    var isExpanded : Boolean = false                // развернута ли доп. информация

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
        if(voice != other.voice)
            return false
        if(isExpanded != other.isExpanded)
            return false
        if (images != other.images)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + (parentId?.hashCode() ?: 0)
        result = 31 * result + (img?.hashCode() ?: 0)
        result = 31 * result + (images?.hashCode() ?: 0)
        result = 31 * result + (color ?: 0)
        result = 31 * result + voice.hashCode()
        result = 31 * result + favorite.hashCode()
        result = 31 * result + (lastEditDate?.hashCode() ?: 0)
        result = 31 * result + (creationDate?.hashCode() ?: 0)
        result = 31 * result + isExpanded.hashCode()
        return result
    }
}

@Entity(tableName = "daily_list_item_table")                                           // Название таблицы
data class DailyListItem(
    @ColumnInfo(name = "daily_list_item_name") var name: String,          // Название
    @ColumnInfo(name = "daily_list_item_parent_id") val parentId: Long   // id дневника, к к-му привязана
) : Serializable
{
    // Первичный ключ - id с авто-генерацией ключей
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0                               // id заметки

    @ColumnInfo(name = "daily_list_item_color")
    var color : Int? = null                      // цвет

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

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + parentId.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (color ?: 0)
        result = 31 * result + isDone.hashCode()
        return result
    }
}

/**
 * Дата-класс, в котором ключевая модель - модель Diary, а связные - все остальные
 * Т.е. при помощи полиморфной связи в этом классе соединены несколько моделей
 */
data class ExtendedDiary(
    @Embedded val diary: Diary,
    @Relation(parentColumn = "id", entityColumn = "note_parent_id")
    val notes: List<Note>,
    @Relation(parentColumn = "id", entityColumn = "daily_list_item_parent_id")
    val dailyListItems: List<DailyListItem>
) : Serializable