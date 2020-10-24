package other

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lomank.diary.R
import roomdatabase.DailyListItem
import java.util.*

class NotificationCreator(context : Context) {

    private val mContext = context

    private var notificationTitle = ""
    private var notificationMessage = ""

    // sets title and message for notification
    fun setNotifyMessage(it : List<DailyListItem>){

        var notifyTitle = ""
        var notifyMessage = ""

        if(it.isNotEmpty()){
            val list = mutableListOf<Int>()
            if(it.size > 5){
                for(i in 0 until 5) {
                    var value = Random().nextInt(5)
                    while(list.contains(value))
                        value = Random().nextInt(5)
                    list.add(value)
                }
            } else {
                for(i in it.indices) {
                    list.add(i)
                }
            }

            notifyMessage = if(it.size > 1) {
                mContext.resources.getString(R.string.notify_message_plural) + "\n"
            } else {
                mContext.resources.getString(R.string.notify_message_single) + "\n"
            }

            // creating message with daily list items
            for(i in list.indices) {
                notifyMessage += "${i+1}) ${it[list[i]].name}" + "\n"
            }

            if(it.size > 5)
                notifyMessage += mContext.resources.getString(R.string.notify_message_ending_plural1) + " ${it.size - 5} " + mContext.resources.getString(R.string.notify_message_ending_plural2)

            // creating title
            notifyTitle = if(it.size > 1) {
                mContext.resources.getString(R.string.notify_title_plural) + " (${it.size}) " + mContext.resources.getString(R.string.notify_items_remain_plural)
            } else {
                mContext.resources.getString(R.string.notify_title_single)
            }
        }
        notificationTitle = notifyTitle
        notificationMessage = notifyMessage
        Log.e("notify", "${notifyTitle}\n${notifyMessage}")
        createNotification()
    }

    fun createNotifyChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFY_CHANNEL_ID, "Todo channel", NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(true)
            val manager = mContext.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification() {
        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(mContext)

        val intent = Intent(mContext, RemainderBroadcast::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(NOTIFY_EXTRA_MESSAGE_CODE, notificationMessage)
        intent.putExtra(NOTIFY_EXTRA_TITLE_CODE, notificationTitle)

        val pendingIntent = PendingIntent.getBroadcast(
            mContext,
            0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager =
            mContext.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

        if(notificationMessage != "" && prefs!!.getBoolean("enable_notifications", true)) {

            val calendar = GregorianCalendar.getInstance()
            val firstTime = calendar.timeInMillis
            Log.e("time", "firstTime: $firstTime")

            val scheduleTime = prefs.getString("notify_time", null)

            if(scheduleTime != null){
                // Date was changed by user
                val gson = Gson()
                val type = object : TypeToken<List<Int>>() {}.type
                val dateList : List<Int> = gson.fromJson(scheduleTime, type)

                calendar.set(Calendar.HOUR_OF_DAY, dateList[0])
                calendar.set(Calendar.MINUTE, dateList[1])
                calendar.set(Calendar.SECOND, dateList[2])
                calendar.set(Calendar.MILLISECOND, dateList[3])

                Log.e("time", "Changed by user: ${calendar.get(Calendar.HOUR_OF_DAY)}:" +
                        "${calendar.get(Calendar.MINUTE)}:" +
                        "${calendar.get(Calendar.SECOND)}:" +
                        "${calendar.get(Calendar.MILLISECOND)}")
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 17)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                Log.e("time", "Default: ${calendar.get(Calendar.HOUR_OF_DAY)}:" +
                        "${calendar.get(Calendar.MINUTE)}:" +
                        "${calendar.get(Calendar.SECOND)}:" +
                        "${calendar.get(Calendar.MILLISECOND)}")
            }

            var secondTime = calendar.timeInMillis

            if(firstTime >= secondTime){
                secondTime = calendar.timeInMillis + AlarmManager.INTERVAL_DAY
                Log.e("time", "secondTime: $secondTime")
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                secondTime,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Log.e("notify", "alarm has set")
        } else {
            alarmManager.cancel(pendingIntent)
            Log.e("notify", "alarm cancelled")
        }
    }

    companion object{
        const val NOTIFY_CHANNEL_ID = "4689"
        const val NOTIFY_EXTRA_MESSAGE_CODE = "3124"
        const val NOTIFY_EXTRA_TITLE_CODE = "5646"
    }
}