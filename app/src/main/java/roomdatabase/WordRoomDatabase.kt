package roomdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Annotates class to be a Room Database with a table (entity) of the Word class

@Database(entities = [Word::class], version = 1, exportSchema = false)
abstract class WordRoomDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    private class WordDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback()
    {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.wordDao())
                }
            }
        }   // в параллельном(?) потоке выполнит ф-ию populateDatabase

        // ф-ия для возможных начальных данных или другого функционала
        suspend fun populateDatabase(wordDao: WordDao) {

            // удалит все записи при перезапуске приложения (можно вынести в отдельную кнопку)
            wordDao.deleteAll()

            // вручную добавляет слова в БД
            val word = Word(word = "Sample word", description = "Sample description")
            wordDao.insert(word)

        }
    }

 //   val migration_1_2 = object : Migration(1, 2) {
 //       override fun migrate(database: SupportSQLiteDatabase) {
 //           database.execSQL("ALTER TABLE Word ADD COLUMN description TEXT NOT NULL DEFAULT ''")
 //       }
 //   }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.

        @Volatile
        private var INSTANCE: WordRoomDatabase? = null

        fun getDatabase(context: Context, scope:CoroutineScope): WordRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            val instance = Room.databaseBuilder(context.applicationContext,
                WordRoomDatabase::class.java, "word_database")
                .addCallback(WordDatabaseCallback(scope))
                // ОСТОРОЖНО!! УДАЛЯЕТ ВСЕ ДАННЫЕ ИЗ БД И ПЕРЕСОЗДАЕТ ЕЕ
                //.fallbackToDestructiveMigration()
                .build()

            INSTANCE = instance
            return instance

        }
    }


} // TODO: добавить комментариев для WordRoomDataBase

