package repository

import androidx.lifecycle.LiveData
import roomdatabase.*

// класс репозитория
class MainRepository (private val diaryDao : DiaryDao) {

    // получаем записи по запросу
    var allExtendedDiaries : LiveData<List<ExtendedDiary>> = diaryDao.getExtendedDiaries()
    var allDailyListItems : LiveData<List<DailyListItem>> = diaryDao.getDailyListItems()

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
        diaryDao.deleteItemsFromDailyList(diary.id) // Удаляем все пункты списка дел
        diaryDao.deleteNotesFromDiary(diary.id) // Сначала удаляем ВСЕ заметки у этого дневника
        diaryDao.deleteDiary(diary.id)  // Затем удаляем САМ дневник
    }

    // Обновляет дневник
    suspend fun updateDiary(diary : Diary){
        diaryDao.updateDiary(diary)
    }

    suspend fun updateListOfDiaries(list : List<Diary>) {
        diaryDao.updateListOfDiaries(list)
    }

    // Notes:

    // добавить заметку
    suspend fun insertNote(note : Note)
    {
        diaryDao.insertNote(note)
    }

    suspend fun insertListNote(notes : List<Note>) {
        diaryDao.insertListNote(notes)
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

    suspend fun updateListOfNotes(list : List<Note>){
        diaryDao.updateListOfNotes(list)
    }

    // DailyListItems:

    // добавить заметку
    suspend fun insertDailyListItem(dailyListItem : DailyListItem)
    {
        diaryDao.insertDailyListItem(dailyListItem)
    }

    suspend fun insertListItems(dailyListItems : List<DailyListItem>) {
        diaryDao.insertListItems(dailyListItems)
    }

    // удалить заметку
    suspend fun deleteOneDailyListItem(dailyListItem : DailyListItem)
    {
        diaryDao.deleteOneDailyListItem(dailyListItem.id)
    }

    suspend fun deleteDailyList(id : Long){
        diaryDao.deleteItemsFromDailyList(id)
    }

    // обновить заметку
    suspend fun updateDailyListItem(dailyListItem : DailyListItem){
        diaryDao.updateDailyListItem(dailyListItem)
    }
}