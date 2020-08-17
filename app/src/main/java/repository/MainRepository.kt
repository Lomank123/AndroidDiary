package repository

import androidx.lifecycle.LiveData
import roomdatabase.*

// класс репозитория
class MainRepository (private val diaryDao : DiaryDao) {

    // получаем записи по запросу
    var allExtendedDiaries : LiveData<List<ExtendedDiary>> = diaryDao.getExtendedDiaries()
    val allDailyLists : LiveData<List<DailyList>> = diaryDao.getDailyLists()

    // ВАЖНО: LiveData объекты постоянно активны и при изменениях в БД они сразу
    // же получают эту информацию,
    // т.е. нет необходимости перепроверять все вручную

    // Diaries:

    // вставляет новую запись в БД посредством вызова внутренней функции DiaryDao через другой поток
    suspend fun insertDiary(diary: Diary) {
        diaryDao.insertDiary(diary)
    }

    // удаляет дневник и ВСЕ записи в нем с помощью вызова 2-х запросов в DiaryDao
    suspend fun deleteDiary(diary: Diary)
    {
        diaryDao.deleteNotesFromDiary(diary.id) // Сначала удаляем ВСЕ заметки у этого дневника
        diaryDao.deleteDiary(diary.id)  // Затем удаляем САМ дневник
    }

    // Обновляет дневник
    suspend fun updateDiary(diary : Diary){
        diaryDao.updateDiary(diary)
    }

    // Notes:

    // добавить заметку
    suspend fun insertNote(note : Note)
    {
        diaryDao.insertNote(note)
    }

    // удалить заметку
    suspend fun deleteNote(note : Note)
    {
        diaryDao.deleteOneNote(note.id)
    }

    // обновить заметку
    suspend fun updateNote(note : Note){
        diaryDao.updateNote(note)
    }

    // DailyListNames:

    suspend fun insertDailyListName(dailyListName : DailyListName)
    {
        diaryDao.insertDailyListName(dailyListName)
    }

    suspend fun deleteDailyListName(dailyListName : DailyListName)
    {
        diaryDao.deleteItemsFromDailyList(dailyListName.id)
        diaryDao.deleteDailyListName(dailyListName.id)
    }

    suspend fun updateDailyListName(dailyListName : DailyListName){
        diaryDao.updateDailyListName(dailyListName)
    }

    // DailyListItems:

    // добавить заметку
    suspend fun insertDailyListItem(dailyListItem : DailyListItem)
    {
        diaryDao.insertDailyListItem(dailyListItem)
    }

    // удалить заметку
    suspend fun deleteOneDailyListItem(dailyListItem : DailyListItem)
    {
        diaryDao.deleteOneDailyListItem(dailyListItem.id)
    }

    // обновить заметку
    suspend fun updateDailyListItem(dailyListItem : DailyListItem){
        diaryDao.updateDailyListItem(dailyListItem)
    }

}