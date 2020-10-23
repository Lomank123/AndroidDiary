package roomdatabase

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.reflect.Type

@Database(entities = [Diary::class, Note::class, DailyListItem::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DiaryRoomDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao

    private class DiaryDatabaseCallback(private val scope: CoroutineScope) :
        RoomDatabase.Callback() {
        // Вызывается при открытии бд (1 раз за сеанс)
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.diaryDao())
                }
            }
        }

        // ф-ия для возможных начальных данных или другого функционала
        suspend fun populateDatabase(diaryDao: DiaryDao) {
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.

        @Volatile
        private var INSTANCE: DiaryRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DiaryRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryRoomDatabase::class.java,
                    "diary_database"
                ).addCallback(DiaryDatabaseCallback(scope))
                    //.fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
                // .fallbackToDestructiveMigration() применять только на этапе разработки, т.к. в
                // дальнейшем базу данных нельзя пересоздавать чтобы не потерять данные
            }
        }
    }
}

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

    @TypeConverter // note this annotation
    fun fromIntList(list : List<Int?>?): String? {
        if (list == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Int?>?>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter // note this annotation
    fun toIntList(listInt : String?): List<Int?>? {
        if (listInt == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Int?>?>() {}.type
        return gson.fromJson(listInt, type)
    }

}
