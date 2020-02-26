package com.example.project3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao                        // Тут хранятся ВСЕ запросы к БД напрямую
interface WordDao {

    @Query("SELECT * from word_table ORDER BY word ASC") // Выдает все записи в алфавитном порядке
    fun getAlphabetizedWords(): LiveData<List<Word>>

    @Query("SELECT * from word_table") // Выдает все записи в порядке их поступления в БД
    fun getWords(): LiveData<List<Word>>

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Добавить запись
    suspend fun insert(word: Word)

    @Query("DELETE FROM word_table") // Удалить ВСЕ записи
    suspend fun deleteAll()

}

// TODO: добавить новые запросы для редактирования, удаления, создания и тд.