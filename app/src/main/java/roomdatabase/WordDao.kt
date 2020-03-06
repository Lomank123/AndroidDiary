package roomdatabase

import androidx.lifecycle.LiveData
import androidx.room.*
import repository.NotesAndWords

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
    suspend fun insertWord(word: Word)

    // Удалить ВСЕ записи
    @Query("DELETE FROM word_table")
    suspend fun deleteAll()



// NOTES

    @Query("SELECT * from note_table ORDER BY note_name ASC")
    fun getAlphabetizedNotes(): LiveData<List<Note>>

    @Query("SELECT * from note_table")
    fun getNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: Note)

    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()

    @Transaction
    @Query("SELECT * from word_table ")
    fun getSomeNotes() : LiveData<List<NotesAndWords>>

    @Transaction
    @Query("DELETE FROM note_table WHERE idNote = :noteId")
    suspend fun deleteOneNote(noteId: Long)

    @Transaction
    @Query("DELETE FROM word_table WHERE id = :wordId")
    suspend fun deleteWord(wordId: Long)

    @Transaction
    @Query("DELETE FROM note_table WHERE diaryId = :wordId")
    suspend fun deleteNotes(wordId: Long)

   // @Query("SELECT * from note_table WHERE diaryId =:ID")
   // fun getNeededNotes(ID : Long) : LiveData<List<Note>>
}

// TODO: добавить новые запросы для редактирования, удаления, создания и тд.