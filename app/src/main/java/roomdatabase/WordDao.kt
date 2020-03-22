package roomdatabase

import androidx.lifecycle.LiveData
import androidx.room.*
import repository.NotesAndWords

// Тут хранятся ВСЕ запросы к БД
@Dao
interface WordDao {

    // WORDS

    // Выдает все записи в алфавитном порядке
    @Query("SELECT * from word_table ORDER BY word ASC")
    fun getAlphabetizedWords(): LiveData<List<Word>>

    // Выдает все записи
    @Query("SELECT * from word_table")
    fun getWords(): LiveData<List<Word>>

    // Добавить запись, onConflict - аргумент, отвечающий за одинаковые записи,
    // IGNORE позволяет одинаковые записи
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWord(word: Word)

    // Удалить ВСЕ записи
    @Query("DELETE FROM word_table")
    suspend fun deleteAll()

    // Обновляет дневник
    @Update
    suspend fun updateWord(word : Word)

    // удаляет 1 дневник (в WordRepository при удалении также используется deleteNotes для заметок)
    // таким образом удаляется дневник + все заметки, связанные с этим дневником
    @Transaction
    @Query("DELETE FROM word_table WHERE id = :wordId")
    suspend fun deleteWord(wordId: Long)


    // NOTES

    // Выдает заметки
    @Query("SELECT * from note_table")
    fun getNotes(): LiveData<List<Note>>

    // Добавляет заметку
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: Note)

    // удаляет ВСЕ заметки из ВСЕХ дневников
    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()

    // Удаляет заметку с выбранным id
    @Transaction
    @Query("DELETE FROM note_table WHERE idNote = :noteId")
    suspend fun deleteOneNote(noteId: Long)

    // См. выше (функция deleteWord)
    // удаляет все заметки, с diaryId таким же как и у дневника
    @Transaction
    @Query("DELETE FROM note_table WHERE diaryId = :wordId")
    suspend fun deleteNotes(wordId: Long)

    // обновляет заметку
    @Update
    suspend fun updateNote(note : Note)


    // NOTES AND WORDS

    // Выдает все записи для класса NotesAndWords (см. NoteRepository)
    @Transaction
    @Query("SELECT * from word_table ")
    fun getSomeNotes() : LiveData<List<NotesAndWords>>

}
