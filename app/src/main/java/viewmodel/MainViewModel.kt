package viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import repository.MainRepository
import roomdatabase.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // ViewModel поддерживает ссылку на репозиторий для получения данных
    private val repository: MainRepository
    // LiveData дают нам обновленные слова, когда они меняются
    val allExtendedDiaries: LiveData<List<ExtendedDiary>>

    init {
        // Gets reference to WordDao from WordRoomDatabase to construct
        // the correct MainRepository.

        // получаем данные из БД
        val diaryDao = DiaryRoomDatabase.getDatabase(application, viewModelScope).diaryDao()
        // связываем репозиторий с этой переменной
        repository = MainRepository(diaryDao)
        // передаем данные из репозитория во ViewModel
        allExtendedDiaries = repository.allExtendedDiaries
    }

    // Diaries:

    // Добавляет запись в БД посредством вызова внутренних ф-ий (см. MainRepository)
    fun insertDiary(diary: Diary) = viewModelScope.launch {
        repository.insertDiary(diary)
    }

    // Удаляет дневник и записи в нем, вызывая ф-ию в WordRepository
    fun deleteDiary(diary: Diary) = viewModelScope.launch {
        repository.deleteDiary(diary)
    }

    // обновляет запись, вызывая функцию в репозитории
    fun updateDiary(diary : Diary) = viewModelScope.launch{
        repository.updateDiary(diary)
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

    // DailyListItems:

    // добавить заметку
    fun insertDailyListItem(dailyListItem : DailyListItem) = viewModelScope.launch{
        repository.insertDailyListItem(dailyListItem)
    }

    // удалить заметку
    fun deleteOneDailyListItem(dailyListItem : DailyListItem) = viewModelScope.launch{
        repository.deleteOneDailyListItem(dailyListItem)
    }

    // обновить заметку
    fun updateDailyListItem(dailyListItem : DailyListItem) = viewModelScope.launch{
        repository.updateDailyListItem(dailyListItem)
    }

    fun updateListOfItems(list : List<DailyListItem>) = viewModelScope.launch {
        repository.updateListOfItems(list)
    }

}