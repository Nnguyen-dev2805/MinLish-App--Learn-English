package com.example.minlishapp_learnenglish.core.email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object SmtpConfig {
    const val HOST = "smtp.gmail.com"
    const val PORT = "587"
    const val USERNAME = "devtech.codes@gmail.com"
    const val PASSWORD = "tdkizirxyehlymrc"
}

object EmailSender {
    suspend fun sendOtpEmail(toEmail: String, otp: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val props = Properties()
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.host"] = SmtpConfig.HOST
            props["mail.smtp.port"] = SmtpConfig.PORT

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SmtpConfig.USERNAME, SmtpConfig.PASSWORD)
                }
            })

            val message = MimeMessage(session)
            message.setFrom(InternetAddress(SmtpConfig.USERNAME))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
            message.subject = "MinLish - Reset Password OTP"
            message.setText("Your OTP code is: $otp\nExpires in 5 minutes.")

            Transport.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
