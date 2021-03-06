package phone_fall_detector.com.example.honza.phonefalldetector;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LinkedList measuredValues;
    private String email;
    private String password;

    private TextView viewX;
    private TextView viewY;
    private TextView viewZ;
    private TextView viewTotal;
    private ProgressBar progressBar;
    private TextView viewOldestValue;
    private Button button;
    private EditText textEmail;
    private EditText textPassword;

    Session session;
    EditText reciep, sub, msg;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        measuredValues = new LinkedList<Double>();

        //get all the views
        viewX = (TextView) findViewById(R.id.textView);
        viewY = (TextView) findViewById(R.id.textView2);
        viewZ = (TextView) findViewById(R.id.textView3);
        viewTotal = (TextView) findViewById(R.id.textView4);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        viewOldestValue = (TextView) findViewById(R.id.textView5);
        button = (Button) findViewById(R.id.button);
        textEmail = (EditText) findViewById(R.id.editTextEmail);
        textPassword = (EditText) findViewById(R.id.editTextPassword);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = textEmail.getText().toString();
                password = textPassword.getText().toString();
                textPassword.setText("");
                if (permissionGranted())
                    startAccelerometer();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        //display the values
        viewX.setText("X: " + Float.toString(event.values[0]));
        viewY.setText("Y: " + Float.toString(event.values[1]));
        viewZ.setText("Z: " + Float.toString(event.values[2]));
        viewTotal.setText("Total: " + Double.toString(
                getTotalAcceleration(
                    event.values[0],
                    event.values[1],
                    event.values[2])));

        //store the values in a queue (aka LinkedList)
        measuredValues.add(getTotalAcceleration(
                event.values[0],
                event.values[1],
                event.values[2]));

        viewOldestValue.setText("Before: " + Double.toString((Double) measuredValues.getFirst()));

        //check for a potential fall
        if(measuredValues.size() >= 50){
            if((double)measuredValues.getFirst() < 2.0 && (double)measuredValues.getLast() > 11.0){
                fallDetected();
            }
            else
                //keep the number of entries at 50
                measuredValues.removeFirst();
        }

    }

    /**
     *Informs the user via AlertDialog and starts the sendEmail function.
     */
    private void fallDetected() {
        measuredValues.clear();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setTitle("Fall detected");
        alertDialogBuilder.setNeutralButton("OK", null);
        alertDialogBuilder.show();

        //send the email here
        sendEmailWithLocation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Takes the acceleration value and calculates the total acceleration of the phone.
     * @param x Acceleration to X axis.
     * @param y Acceleration to Y axis.
     * @param z Acceleration to Z axis.
     * @return Total acceleration.
     */
    private double getTotalAcceleration(float x, float y, float z){
        return Math.sqrt(x*x + y*y + z*z);
    }

    /**
     * get the current location and call sendEmail()
     */
    private void sendEmailWithLocation(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        progressBar.setVisibility(View.VISIBLE);
        locationManager.requestSingleUpdate(criteria, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String message = "Your phone fell at/n Latitude: " +
                        Double.toString(location.getLatitude()) +
                        " Longitude: " +
                        Double.toString(location.getLongitude());
                String recipient = "jan.hric@hva.nl";
                String subject = "Phone fall";
                sendEmail(message, subject, recipient);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        }, null);

        //Snackbar.make(, "Updating location...", Snackbar.LENGTH_SHORT).show();

        return;
    }

    /**
     * Asks the user for permission or returns true if permission has already been granted.
     * @return true if the app already has the permission, false otherwise
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public Boolean permissionGranted(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startAccelerometer();
        }
    }

    private void startAccelerometer(){
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * This function sends en email.
     * @param message message to be sent
     * @param recipient recipient(s) to send the message to
     */
    private void sendEmail(String message, String subject, String recipient){
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        session = Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        RetreiveFeedTask task = new RetreiveFeedTask(message, subject, recipient);
        task.execute();
    }

    class RetreiveFeedTask extends AsyncTask<String, Void, String> {
        private String recipient;
        private String subject;
        private String messageText;

        public RetreiveFeedTask(String recipient, String subject, String messageText){
            this.recipient = recipient;
            this.subject = subject;
            this.messageText = messageText;
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(email));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                message.setSubject(subject);
                message.setContent(messageText, "text/html; charset=utf-8");
                Transport.send(message);
            } catch(MessagingException e) {
                e.printStackTrace();
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
        }
    }
}
