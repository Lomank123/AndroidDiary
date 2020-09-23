package roomdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Diary::class, Note::class, DailyListItem::class], version = 2, exportSchema = false)
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
            // удалит все записи при перезапуске приложения (можно вынести в отдельную кнопку)
            //diaryDao.deleteAllDiaries()
            //diaryDao.deleteAllNotes()
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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
                // .fallbackToDestructiveMigration() применять только на этапе разработки, т.к. в
                // дальнейшем базу данных нельзя пересоздавать чтобы не потерять данные
            }
        }
    }
}
