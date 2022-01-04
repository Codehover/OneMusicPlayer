package com.example.onemusicplayer

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.onemusicplayer.databinding.ActivityFeedbackBinding
import java.net.Authenticator
import java.net.PasswordAuthentication
import java.util.*

class FeedbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Feedback"

        binding.sendFd.setOnClickListener {
            val feedbackMsg = binding.decFd.text.toString() + "\n" + binding.emailFd.text.toString()
            val subject = binding.topicFd.text.toString()
            val userName = "OneMusicPlayerApp" // this is just for sample
            val pass = "music one"   // fill correct mail and password here for working
            val con = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if ((feedbackMsg.isNotEmpty()) && (subject.isNotEmpty()) && (con.activeNetworkInfo?.isConnected == true)) {
               /* Thread {
                    try {
                        val properties = Properties()
                        properties["mail.smtp.auth"] = "true"
                        properties["mail.smtp.starttls.enable"] = "true"
                        properties["mail.smtp.host"] = "smtp.gmail.com"
                        properties["mail.smtp.port"] = "587"

                        val session = Session.getInstance(properties, object : Authenticator() {
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(userName, pass)
                            }
                        })
                        val mail = MimeMessage(session)
                        mail.subject = subject
                        mail.setText(feedbackMsg)
                        mail.setFrom(InternetAddress(userName))
                        mail.setRecipients(
                            Message.RecipientType.TO,
                            InternetAddress.parse(userName)
                        )
                        Transport.send(mail)

                    } catch (ex: Exception) {
                        Toast.makeText(this@FeedbackActivity, ex.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }

                }.start()
                Toast.makeText(this@FeedbackActivity, "Thanks for feedback!", Toast.LENGTH_SHORT)
                    .show()
                finish()*/
            } else Toast.makeText(
                this@FeedbackActivity,
                "Something went wrong!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}