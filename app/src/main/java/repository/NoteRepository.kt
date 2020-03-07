package repository

import androidx.lifecycle.LiveData
import androidx.room.Embedded
import androidx.room.Relation
import roomdatabase.Note
import roomdatabase.Word
import roomdatabase.WordDao


data class NotesAndWords (@Embedded val word : Word,
                          @Relation(parentColumn = "id", entityColumn = "diaryId")
                          val notes : List<Note>)




class NoteRepository (private val wordDao : WordDao) {

    var allNotes : LiveData<List<NotesAndWords>> = wordDao.getSomeNotes()

    suspend fun insertNote(note : Note)
    {
        wordDao.insertNote(note)

    }

    suspend fun deleteNote(note : Note)
    {
        wordDao.deleteOneNote(note.idNote)


    }

    suspend fun deleteWord(word: Word)
    {
        wordDao.deleteNotes(word.id) // Сначала удаляем ВСЕ заметки у этого дневника
        wordDao.deleteWord(word.id)  // Затем удаляем САМ дневник
    }

    suspend fun updateNote(note : Note){
        wordDao.updateNote(note)
    }

}