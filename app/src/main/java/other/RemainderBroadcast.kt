package other

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lomank.diary.MainActivity
import com.lomank.diary.R

class RemainderBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("notify", "Received")
        val title = intent!!.getStringExtra(NotificationCreator.NOTIFY_EXTRA_TITLE_CODE)
        val message = intent.getStringExtra(NotificationCreator.NOTIFY_EXTRA_MESSAGE_CODE)
        val icon = BitmapFactory.decodeResource(context!!.resources, R.drawable.logo)

        val resultIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, NotificationCreator.NOTIFY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setContentTitle(title)
            //.setContentText(message)
            .setLargeIcon(icon)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notifyManager = NotificationManagerCompat.from(context)
        notifyManager.notify(NOTIFY_ID, builder.build())
    }

    companion object{
        const val NOTIFY_ID = 91
    }
}