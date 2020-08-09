package repository

import androidx.lifecycle.LiveData
import roomdatabase.DiaryDao
import roomdatabase.Note
import roomdatabase.ExtendedDiary
import roomdatabase.Diary

// класс репозитория
class MainRepository (private val diaryDao : DiaryDao) {

    // получаем записи по запросу
    var allExtendedDiaries : LiveData<List<ExtendedDiary>> = diaryDao.getExtendedDiaries()

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
        diaryDao.deleteOneNote(note.idNote)
    }

    // обновить заметку
    suspend fun updateNote(note : Note){
        diaryDao.updateNote(note)
    }
}