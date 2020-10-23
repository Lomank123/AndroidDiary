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
    // Обновляет лист из дневников
    @Update
    suspend fun updateListOfDiaries(list : List<Diary>)

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

    // Добавляет заметку
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertListNote(notes : List<Note>)

    // Обновляет заметку
    @Update
    suspend fun updateNote(note : Note)
    // Обновляет лист из заметок
    @Update
    suspend fun updateListOfNotes(list : List<Note>)

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

    // DailyListItem queries

    //@Query("SELECT * from daily_list_item_table WHERE daily_list_item_parent_id = :diaryId")
    //fun getDailyListItemsFromDiary(diaryId : Long): List<DailyListItem>

    //@Query("SELECT MAX(daily_list_item_order) FROM daily_list_item_table WHERE daily_list_item_parent_id = :diaryId")
    //suspend fun getOrderedItems(diaryId: Long) : Int

    @Update
    suspend fun updateListOfItems(list : List<DailyListItem>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyListItem(dailyListItem : DailyListItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertListItems(dailyListItems : List<DailyListItem>)

    @Update
    suspend fun updateDailyListItem(dailyListItem : DailyListItem)

    @Transaction
    @Query("DELETE FROM daily_list_item_table WHERE daily_list_item_parent_id = :diaryId")
    suspend fun deleteItemsFromDailyList(diaryId : Long)

    @Transaction
    @Query("DELETE FROM daily_list_item_table WHERE id = :dailyListItemId")
    suspend fun deleteOneDailyListItem(dailyListItemId : Long)

    @Query("DELETE FROM daily_list_item_table")
    suspend fun deleteAllDailyListItems()

    @Transaction
    @Query("SELECT * from daily_list_item_table WHERE daily_list_item_is_done = :value")
    fun getDailyListItems(value : Boolean=false) : LiveData<List<DailyListItem>>

    // ExtendedDiary queries

    // Выдает все записи для дата-класса ExtendedDiary
    @Transaction
    @Query("SELECT * from diary_table")
    fun getExtendedDiaries() : LiveData<List<ExtendedDiary>>
}
