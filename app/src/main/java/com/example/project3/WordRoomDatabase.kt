package com.example.project3

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

    private class WordDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database -> // в параллельном(?) потоке выполнит ф-ию populateDatabase
                scope.launch {
                    populateDatabase(database.wordDao())
                }
            }
        }

        suspend fun populateDatabase(wordDao: WordDao) { // ф-ия для возможных начальных данных или другого функционала

            //wordDao.deleteAll()          // удалит все записи при перезапуске приложения (можно вынести в отдельную кнопку)

            val word = Word("Hello") // вручную добавляет слова в БД
            wordDao.insert(word)

        }
    }

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

            val instance = Room.databaseBuilder(context.applicationContext, WordRoomDatabase::class.java, "word_database")
                .addCallback(WordDatabaseCallback(scope))
                .build()

            INSTANCE = instance
            return instance

        }
    }


} // TODO: добавить комментариев для WordRoomDataBase

