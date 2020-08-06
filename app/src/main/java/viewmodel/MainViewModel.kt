package viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import repository.MainRepository
import repository.NotesAndWords
import roomdatabase.Note
import roomdatabase.Word
import roomdatabase.WordRoomDatabase

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // ViewModel поддерживает ссылку на репозиторий для получения данных.
    private val repository: MainRepository
    // LiveData дают нам обновленные слова, когда они меняются.
    val allNotesWords: LiveData<List<NotesAndWords>>

    init {
        // Gets reference to WordDao from WordRoomDatabase to construct
        // the correct NoteRepository.

        // получаем данные из БД
        val wordsDao = WordRoomDatabase.getDatabase(application, viewModelScope).wordDao()
        // связываем репозиторий с этой переменной
        repository = MainRepository(wordsDao)
        // передаем данные из репозитория сюда (во ViewModel)
        allNotesWords = repository.allNotesWords
    }

    // Notes:

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


    // Words:

    // Добавляет запись в БД посредством вызова внутренних ф-ий (см. WordRepository)
    fun insertWord(word: Word) = viewModelScope.launch {
        repository.insertWord(word)
    }

    // Удаляет дневник и записи в нем, вызывая ф-ию в WordRepository
    fun deleteWord(word: Word) = viewModelScope.launch {
        repository.deleteWord(word)
    }

    // обновляет запись, вызывая функцию в репозитории
    fun updateWord(word : Word) = viewModelScope.launch{
        repository.updateWord(word)
    }
}