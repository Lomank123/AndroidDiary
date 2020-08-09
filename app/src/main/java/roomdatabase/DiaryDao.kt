package roomdatabase

import androidx.lifecycle.LiveData
import androidx.room.*

// Тут хранятся ВСЕ запросы к БД

@Dao
interface DiaryDao {

    // Diary queries

    // Выдает все записи в алфавитном порядке
    @Query("SELECT * from diary_table ORDER BY diary_name ASC")
    fun getAlphabetizedDiaries(): LiveData<List<Diary>>
    //
    // Выдает все записи
    @Query("SELECT * from diary_table")
    fun getDiaries(): LiveData<List<Diary>>

    // Добавить запись, onConflict - аргумент, отвечающий за одинаковые записи,
    // IGNORE позволяет одинаковые записи
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDiary(diary : Diary)

    // Обновляет дневник
    @Update
    suspend fun updateDiary(diary : Diary)

    // удаляет 1 дневник (в MainRepository при удалении также используется deleteNotes для заметок)
    // таким образом удаляется дневник + все заметки, связанные с этим дневником
    @Transaction
    @Query("DELETE FROM diary_table WHERE id = :diary_id")
    suspend fun deleteDiary(diary_id : Long)

    // Удалить ВСЕ записи
    @Query("DELETE FROM diary_table")
    suspend fun deleteAllDiaries()

    // Note queries

    // Выдает заметки
    @Query("SELECT * from note_table")
    fun getNotes(): LiveData<List<Note>>

    // Добавляет заметку
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note : Note)

    // Обновляет заметку
    @Update
    suspend fun updateNote(note : Note)

    // См. выше (функция deleteDiary)
    // удаляет все заметки, с note_parent_id таким же как и у дневника
    @Transaction
    @Query("DELETE FROM note_table WHERE note_parent_id = :diary_id")
    suspend fun deleteNotesFromDiary(diary_id : Long)

    // Удаляет заметку с выбранным note_id
    @Transaction
    @Query("DELETE FROM note_table WHERE idNote = :note_id")
    suspend fun deleteOneNote(note_id : Long)

    // удаляет ВСЕ заметки из ВСЕХ дневников
    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()

    // ExtendedDiary queries

    // Выдает все записи для дата-класса ExtendedDiary
    @Transaction
    @Query("SELECT * from diary_table ")
    fun getExtendedDiaries() : LiveData<List<ExtendedDiary>>

}
