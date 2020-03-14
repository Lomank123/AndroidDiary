package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_new_word.*

class NewWordActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_word)

        // обработчик нажатий на кнопку Save
        button_save.setOnClickListener {

            val replyIntent = Intent()

            // если поле пустое устанавливаем отриц. результат
            if (TextUtils.isEmpty(edit_word.text)) {
                // устанавливаем результат как RESULT_CANCELED (отменен)
                setResult(Activity.RESULT_CANCELED, replyIntent)
            }
            else
            {
                // создаем массив с названием и описанием дневника
                val word = arrayListOf(edit_word.text.toString(),
                    edit_descr.text.toString())

                // кладем то, что написано в editText в word и передаем по тегу EXTRA_REPLY (ниже)
                replyIntent.putExtra(EXTRA_REPLY, word)

                setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK
            }
            // завершаем работу с активити
            finish()
        }
        button_cancel_word1.setOnClickListener {

            // TODO: убрать этот кусок, вставить для другой кнопки выбора фото!!!
        //    val i1 = Intent(Intent.ACTION_PICK)
        //    i1.type = "image/*"
        //    startActivityForResult(i1, 4)

            // устанавливаем результат и завершаем работу с активити
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 4 && resultCode == Activity.RESULT_OK)
        {
            imageView5.setImageURI(data?.data)
        }

    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_REPLY = "com.example.android.wordlistsql.REPLY"
        const val EXTRA_IMAGE = "img"
    }
} // TODO: поправить выбор фото в XML файле, чтобы выглядело презентабельно