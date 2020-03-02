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

}