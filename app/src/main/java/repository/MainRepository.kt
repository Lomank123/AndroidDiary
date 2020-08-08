package repository

import androidx.lifecycle.LiveData
import androidx.room.Embedded
import androidx.room.Relation
import roomdatabase.Note
import roomdatabase.Word
import roomdatabase.WordDao
import java.io.File
import java.io.Serializable

// класс, в котором описывается объект, содержащий данные о дневнике
// и о ВСЕХ его заметках, заметки помещены в список
data class NotesAndWords (@Embedded val word : Word,
                          @Relation(parentColumn = "id", entityColumn = "diaryId")
                          val notes : List<Note>) : Serializable

// класс репозитория
class MainRepository (private val wordDao : WordDao) {

    // получаем заметки по запросу
    var allNotesWords : LiveData<List<NotesAndWords>> = wordDao.getSomeNotes()

    // ВАЖНО: LiveData объекты постоянно активны и при изменениях в БД они сразу
    // же получают эту информацию,
    // т.е. нет необходимости перепроверять все вручную

    // Notes:

    // добавить заметку
    suspend fun insertNote(note : Note)
    {
        wordDao.insertNote(note)
    }

    // удалить заметку
    suspend fun deleteNote(note : Note)
    {
        wordDao.deleteOneNote(note.idNote)
    }

    // обновить заметку
    suspend fun updateNote(note : Note){
        wordDao.updateNote(note)
    }

    // Words:

    // вставляет новую запись в БД посредством вызова внутренней функции WordDao через другой поток
    suspend fun insertWord(word: Word) {
        wordDao.insertWord(word)
    }

    // удаляет дневник и ВСЕ записи в нем с помощью вызова 2-х запросов в WordDao
    suspend fun deleteWord(word: Word)
    {
        wordDao.deleteNotes(word.id) // Сначала удаляем ВСЕ заметки у этого дневника
        wordDao.deleteWord(word.id)  // Затем удаляем САМ дневник
    }

    // Обновляет дневник
    suspend fun updateWord(word : Word){
        wordDao.updateWord(word)
    }
}