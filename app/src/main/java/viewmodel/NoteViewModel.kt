package viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import repository.NoteRepository
import repository.NotesAndWords
import roomdatabase.Note
import roomdatabase.WordRoomDatabase

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    // LiveData gives us updated words when they change.
    val allNotes: LiveData<List<NotesAndWords>>

    init {
        // Gets reference to WordDao from WordRoomDatabase to construct
        // the correct WordRepository.

        // получаем данные из БД
        val wordsDao = WordRoomDatabase.getDatabase(application, viewModelScope).wordDao()

        // связываем репозиторий с этой переменной
        repository = NoteRepository(wordsDao)
        // передаем данные из репозитория сюда (во ViewModel)
        allNotes = repository.allNotes

    }




    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun deleteNote(note : Note) = viewModelScope.launch{
        repository.deleteNote(note)
    }


}