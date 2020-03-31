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

    // ViewModel поддерживает ссылку на репозиторий для получения данных.
    private val repository: WordRepository
    // LiveData дают нам обновленные слова, когда они меняются.
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