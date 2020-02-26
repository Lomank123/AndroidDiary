package com.example.project3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class WordRepository(private val wordDao: WordDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    var allWords: LiveData<List<Word>> = wordDao.getWords()

    // получаем данные по запросу
    // allWords - тот самый репозиторий со всеми данными
    // в зависимости от изменений этот список тоже будет меняться

    // ВАЖНО: насколько я понял, LiveData объекты постоянно активны и при изменениях в БД они сразу же получают эту информацию,
    // т.е. нет необходимости перепроверять все вручную
    // Цепочка связанных LivaData переменных: WordDao -> WordRepository -> WordViewModel -> MainActivity


    suspend fun insert(word: Word) {      // вставляет новую запись в БД посредством вызова внутренней функции WordDao через другой поток(?)
        wordDao.insert(word)
    }


}