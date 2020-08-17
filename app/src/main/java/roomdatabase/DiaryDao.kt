package roomdatabase

import androidx.lifecycle.LiveData
import androidx.room.*

// Тут хранятся ВСЕ запросы к БД

@Dao
interface DiaryDao {

    // Diary queries

    // Выдает все записи в алфавитном порядке
    //@Query("SELECT * from diary_table ORDER BY diary_name ASC")
    //fun getAlphabetizedDiaries(): LiveData<List<Diary>>
    //
    // Выдает все записи
    //@Query("SELECT * from diary_table")
    //fun getDiaries(): LiveData<List<Diary>>

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
    @Query("DELETE FROM diary_table WHERE id = :diaryId")
    suspend fun deleteDiary(diaryId : Long)

    // Удалить ВСЕ записи
    @Query("DELETE FROM diary_table")
    suspend fun deleteAllDiaries()

    // Note queries

    // Выдает заметки
    //@Query("SELECT * from note_table")
    //fun getNotes(): LiveData<List<Note>>
    //
    // Выдает записи, относящиеся к конкретному дневнику
    //@Query("SELECT * from note_table WHERE note_parent_id = :diaryId")
    //fun getNotesFromDiary(diaryId : Long): List<Note>

    // Добавляет заметку
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note : Note)

    // Обновляет заметку
    @Update
    suspend fun updateNote(note : Note)

    // См. выше (функция deleteDiary)
    // удаляет все заметки, с note_parent_id таким же как и у дневника
    @Transaction
    @Query("DELETE FROM note_table WHERE note_parent_id = :diaryId")
    suspend fun deleteNotesFromDiary(diaryId : Long)

    // Удаляет заметку с выбранным note_id
    @Transaction
    @Query("DELETE FROM note_table WHERE id = :noteId")
    suspend fun deleteOneNote(noteId : Long)

    // удаляет ВСЕ заметки из ВСЕХ дневников
    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()

    // DailyListName queries

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyListName(dailyListName : DailyListName)

    @Update
    suspend fun updateDailyListName(dailyListName : DailyListName)

    @Transaction
    @Query("DELETE FROM daily_list_table WHERE id = :dailyListNameId")
    suspend fun deleteDailyListName(dailyListNameId : Long)

    @Query("DELETE FROM daily_list_table")
    suspend fun deleteAllDailyListNames()

    // DailyListItem queries

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyListItem(dailyListItem : DailyListItem)

    @Update
    suspend fun updateDailyListItem(dailyListItem : DailyListItem)

    @Transaction
    @Query("DELETE FROM daily_list_item_table WHERE daily_list_item_parent_id = :dailyListNameId")
    suspend fun deleteItemsFromDailyList(dailyListNameId : Long)

    @Transaction
    @Query("DELETE FROM daily_list_item_table WHERE id = :dailyListItemId")
    suspend fun deleteOneDailyListItem(dailyListItemId : Long)

    @Query("DELETE FROM daily_list_item_table")
    suspend fun deleteAllDailyListItems()

    // ExtendedDiary queries

    // Выдает все записи для дата-класса ExtendedDiary
    @Transaction
    @Query("SELECT * from diary_table")
    fun getExtendedDiaries() : LiveData<List<ExtendedDiary>>

    // DailyList queries

    @Transaction
    @Query("SELECT * from daily_list_table")
    fun getDailyLists() : LiveData<List<DailyList>>


}
