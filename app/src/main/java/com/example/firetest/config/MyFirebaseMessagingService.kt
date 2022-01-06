package com.example.firetest.config

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.firetest.R
import com.example.firetest.src.splash.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "FirebaseService"
    private val CHANNEL_ID = "Channel"
    private val CHANNEL_NAME = "Test Channel"

    override fun onNewToken(token: String) {
        Log.d(TAG, "new Token: $token")

        val pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("token", token).apply()
        editor.commit()

        Log.d("로그: ", "성공적으로 토큰을 저장함")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationManager = NotificationManagerCompat.from(
            applicationContext
        )
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 101, intent, PendingIntent.FLAG_ONE_SHOT)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var builder: NotificationCompat.Builder? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
            builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        } else {
            builder = NotificationCompat.Builder(applicationContext)
        }

        val title = remoteMessage.notification!!.title
        val body = remoteMessage.notification!!.body
        var myBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_push)

        val alarmStyle = NotificationCompat.BigPictureStyle()
            .bigPicture(myBitmap)

        builder!!.setStyle(alarmStyle)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(myBitmap)
            .setColor(ContextCompat.getColor(this,R.color.colorAccent))
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .priority = NotificationCompat.PRIORITY_DEFAULT


        val notification: Notification = builder.build()
        notificationManager.notify(1, notification)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(remoteMessage: RemoteMessage) {
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = "channel"
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(remoteMessage.data["body"].toString())
            .setContentText(remoteMessage.data["title"].toString())
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notice", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(uniId, notificationBuilder.build())
    }

    // 받은 토큰을 서버로 전송
    private fun sendRegistrationToServer(token: String){

    }

    // Firebase Cloud Messaging Server 가 대기중인 메세지를 삭제 시 호출
    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    // 메세지가 서버로 전송 성공 했을때 호출
    override fun onMessageSent(p0: String) {
        super.onMessageSent(p0)
    }

    // 메세지가 서버로 전송 실패 했을때 호출
    override fun onSendError(p0: String, p1: Exception) {
        super.onSendError(p0, p1)
    }

}