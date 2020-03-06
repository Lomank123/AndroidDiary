package viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import roomdatabase.Word
import roomdatabase.WordRoomDatabase
import kotlinx.coroutines.launch
import repository.WordRepository

// Class extends AndroidViewModel and requires application as a parameter.
class WordViewModel(application: Application) : AndroidViewModel(application) {

    // The ViewModel maintains a reference to the repository to get data.
    private val repository: WordRepository
    // LiveData gives us updated words when they change.
    val allWords: LiveData<List<Word>>

    init {
        // Gets reference to WordDao from WordRoomDatabase to construct
        // the correct WordRepository.

        // получаем данные из БД
        val wordsDao = WordRoomDatabase.getDatabase(application, viewModelScope).wordDao()

        // связываем репозиторий с этой переменной
        repository = WordRepository(wordsDao)
        // передаем данные из репозитория сюда (во ViewModel)
        allWords = repository.allWords

    }

    /**
     * The implementation of insert() in the database is completely hidden from the UI.
     * Room ensures that you're not doing any long running operations on
     * the main thread, blocking the UI, so we don't need to handle changing Dispatchers.
     * ViewModels have a coroutine scope based on their lifecycle called
     * viewModelScope which we can use here.
     */
    // Добавляет запись в БД посредством вызова внутренних ф-ий (см. WordRepository)
    fun insertWord(word: Word) = viewModelScope.launch {
        repository.insertWord(word)
    }

    fun deleteWord(word: Word) = viewModelScope.launch {
        repository.deleteWord(word)
    }


}   // TODO: comment WordViewModel class code