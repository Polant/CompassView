package com.example.compassview.activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;

import com.example.compassview.R;
import com.example.compassview.view.CompassView;


public class MainActivity extends ActionBarActivity {

    private static final int LAYOUT = R.layout.main;

    //Значения аклклерометра.
    private float[] aValues = new float[3];
    //Значение магнитометра.
    private float[] mValues = new float[3];

    private CompassView compassView;
    private SensorManager sensorManager;
    private int rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        compassView = (CompassView)this.findViewById(R.id.compassView);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        rotation = display.getRotation();

        updateOrientation(new float[]{0, 0, 0});
    }

    private void updateOrientation(float[] values) {
        if (compassView != null){
            compassView.setBearing(values[0]);
            compassView.setPitch(values[1]);
            compassView.setRoll(-values[2]);
            compassView.invalidate();
        }
    }

    private float[] calculateOrientation() {
        float[] values = new float[3];
        float[] inR = new float[9];
        float[] outR = new float[9];

        //Определяю матрицу поворота.
        SensorManager.getRotationMatrix(inR, null, aValues, mValues);

        //Переопределяю координаты на основе естественного положения устройства.
        int xAxis = SensorManager.AXIS_X;
        int yAxis = SensorManager.AXIS_Y;

        switch (rotation){
            case Surface.ROTATION_90:
                xAxis = SensorManager.AXIS_Y;
                yAxis = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                yAxis = SensorManager.AXIS_MINUS_Y;
                break;
            case Surface.ROTATION_270:
                xAxis = SensorManager.AXIS_MINUS_Y;
                yAxis = SensorManager.AXIS_X;
                break;
            default:
                break;
        }
        SensorManager.remapCoordinateSystem(inR, xAxis, yAxis, outR);

        //Получаю текущую, уточненную ориентацию.
        SensorManager.getOrientation(outR, values);
        //Переведу результат в градусы.
        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);

        return values;
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                aValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mValues = event.values;
            }

            updateOrientation(calculateOrientation());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    @Override
    protected void onResume() {
        super.onResume();

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(sensorEventListener,
                accelerometer, SensorManager.
                        SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorEventListener,
                magneticField,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //---------------------Menu---------------------------//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
