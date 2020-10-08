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

    fun updateListOfDiaries(list : List<Diary>) = viewModelScope.launch {
        repository.updateListOfDiaries(list)
    }

    // Notes:

    // добавляет запись, вызывая функцию в репозитории
    fun insertNote(note: Note) {
        viewModelScope.launch {
              repository.insertNote(note)
        }
    }

    fun insertListNote(notes: List<Note>) = viewModelScope.launch {
        repository.insertListNote(notes)
    }

    // удаляет запись, вызывая функцию в репозитории
    fun deleteNote(note : Note) = viewModelScope.launch{
        repository.deleteNote(note)
    }

    // обновляет запись, вызывая функцию в репозитории
    fun updateNote(note: Note) = viewModelScope.launch{
        repository.updateNote(note)
    }

    fun updateListOfNotes(list : List<Note>) = viewModelScope.launch {
        repository.updateListOfNotes(list)
    }

    // DailyListItems:

    // добавить заметку
    fun insertDailyListItem(dailyListItem : DailyListItem) = viewModelScope.launch{
        repository.insertDailyListItem(dailyListItem)
    }

    fun insertListItems(dailyListItems: List<DailyListItem>) = viewModelScope.launch {
        repository.insertListItems(dailyListItems)
    }

    // удалить заметку
    fun deleteOneDailyListItem(dailyListItem : DailyListItem) = viewModelScope.launch{
        repository.deleteOneDailyListItem(dailyListItem)
    }

    fun deleteDailyList(id : Long) = viewModelScope.launch {
        repository.deleteDailyList(id)
    }

    // обновить заметку
    fun updateDailyListItem(dailyListItem : DailyListItem) = viewModelScope.launch{
        repository.updateDailyListItem(dailyListItem)
    }



}