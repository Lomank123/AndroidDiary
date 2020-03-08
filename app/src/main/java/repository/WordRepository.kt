package repository

import androidx.lifecycle.LiveData
import roomdatabase.Word
import roomdatabase.WordDao

class WordRepository(private val wordDao: WordDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    var allWords: LiveData<List<Word>> = wordDao.getWords()

    // получаем данные по запросу
    // allWords - тот самый репозиторий со всеми данными
    // в зависимости от изменений этот список тоже будет меняться

    // ВАЖНО: LiveData объекты постоянно активны и при изменениях в БД они сразу
    // же получают эту информацию,
    // т.е. нет необходимости перепроверять все вручную

    // Цепочка связанных LivaData переменных:
    // WordDao -> WordRepository -> WordViewModel -> MainActivity

    // вставляет новую запись в БД посредством вызова внутренней функции WordDao через другой поток
    suspend fun insertWord(word: Word) {
        wordDao.insertWord(word)
    }

    // удаляет дневник и ВСЕ записи в нем с помощью вызова 2-х запросов в WordDao
    suspend fun deleteWord(word: Word)
    {
        wordDao.deleteNotes(word.id) // Сначала удаляем ВСЕ заметки у этого дневника
        wordDao.deleteWord(word.id)  // Затем удаляем САМ дневник
    }
}