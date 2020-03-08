package repository

import androidx.lifecycle.LiveData
import androidx.room.Embedded
import androidx.room.Relation
import roomdatabase.Note
import roomdatabase.Word
import roomdatabase.WordDao

// класс, в котором описывается объект, содержащий данные о дневнике
// и о ВСЕХ его заметках, заметки помещены в список
data class NotesAndWords (@Embedded val word : Word,
                          @Relation(parentColumn = "id", entityColumn = "diaryId")
                          val notes : List<Note>)

// класс репозитория
class NoteRepository (private val wordDao : WordDao) {

    // получаем заметки по запросу
    var allNotes : LiveData<List<NotesAndWords>> = wordDao.getSomeNotes()

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

}