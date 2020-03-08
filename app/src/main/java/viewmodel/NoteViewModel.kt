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

    // ViewModel поддерживает ссылку на репозиторий для получения данных.
    private val repository: NoteRepository
    // LiveData дают нам обновленные слова, когда они меняются.
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

    // добавляет запись, вызывая функцию в репозитории
    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    // удаляет запись, вызывая функцию в репозитории
    fun deleteNote(note : Note) = viewModelScope.launch{
        repository.deleteNote(note)
    }

    // обновляет запись, вызывая функцию в репозитории
    fun updateNote(note: Note) = viewModelScope.launch{
        repository.updateNote(note)
    }


}