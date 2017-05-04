package phone_fall_detector.com.example.honza.phonefalldetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float[] gravity;
    private float[] linear_acceleration;

    private TextView viewX;
    private TextView viewY;
    private TextView viewZ;
    private TextView viewTotal;

    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gravity = new float[3];
        linear_acceleration = new float[3];

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        viewX = (TextView) findViewById(R.id.textView);
        viewY = (TextView) findViewById(R.id.textView2);
        viewZ = (TextView) findViewById(R.id.textView3);
        viewTotal = (TextView) findViewById(R.id.textView4);

    }

    @Override
    public void onSensorChanged(SensorEvent event){
        viewX.setText("X: " + Float.toString(event.values[0]));
        viewY.setText("Y: " + Float.toString(event.values[1]));
        viewZ.setText("Z: " + Float.toString(event.values[2]));
        /*viewTotal.setText("Total: " + Double.toString(
                Math.sqrt(event.values[0]*event.values[0] +
                        event.values[1]*event.values[1] +
                        event.values[2]*event.values[2])));*/
        viewTotal.setText(Integer.toString(count));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
