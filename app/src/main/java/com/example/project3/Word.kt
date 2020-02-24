package com.example.project3

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_table")
data class Word(@PrimaryKey @ColumnInfo(name = "word") val word: String) {
} // TODO: comment Word class code

@Entity(tableName = "pic_table")
data class Picture(val pic : String)
