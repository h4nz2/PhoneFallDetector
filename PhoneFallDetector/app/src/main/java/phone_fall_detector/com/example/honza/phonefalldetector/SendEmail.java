package phone_fall_detector.com.example.honza.phonefalldetector;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by Honza on 04/05/2017.
 */

public class SendEmail extends AsyncTask {
    //Declaring Variables
    private Context context;
    private Session session;

    //Information to send email
    private String subject;
    private String message;
    private String emailTo;
    private String emailFrom;
    private String password;

    //Class Constructor
    public SendEmail(Context context, String emailFrom, String password, String emailTo, String subject, String message){
        //Initializing variables
        this.context = context;
        this.emailFrom = emailFrom;
        this.subject = subject;
        this.message = message;
        this.password = password;
        this.emailTo = emailTo;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        //Showing a success message
        Toast.makeText(context,"Message Sent",Toast.LENGTH_LONG).show();
    }

    @Override
    protected Object doInBackground(Object... params) {
        //Creating properties
        Properties props = new Properties();

        //Configuring properties for gmail
        //If you are not using gmail you may need to change the values
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        //Creating a new session
        session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    //Authenticating the password
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailFrom, password);
                    }
                });

        try {
            //Creating MimeMessage object
            MimeMessage mm = new MimeMessage(session);

            //Setting sender address
            try {
                mm.setFrom(new InternetAddress(emailFrom));
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            //Adding receiver
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
            //Adding subject
            mm.setSubject(subject);
            //Adding message
            mm.setText(message);

            //Sending email
            Transport.send(mm);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
