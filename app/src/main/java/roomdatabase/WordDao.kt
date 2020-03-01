package roomdatabase

import androidx.lifecycle.LiveData
import androidx.room.*

// Тут хранятся ВСЕ запросы к БД
@Dao
interface WordDao {

    // Выдает все записи в алфавитном порядке
    @Query("SELECT * from word_table ORDER BY word ASC")
    fun getAlphabetizedWords(): LiveData<List<Word>>

    // Выдает все записи в порядке их поступления в БД
    @Query("SELECT * from word_table")
    fun getWords(): LiveData<List<Word>>

    // Добавить запись, onConflict - аргумент, отвечающий за одинаковые записи,
    // IGNORE позволяет одинаковые записи
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word)

    // Удалить ВСЕ записи
    @Query("DELETE FROM word_table")
    suspend fun deleteAll()

}

// TODO: добавить новые запросы для редактирования, удаления, создания и тд.