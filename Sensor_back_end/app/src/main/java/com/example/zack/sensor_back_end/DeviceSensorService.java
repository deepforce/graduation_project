package com.example.zack.sensor_back_end;

/**
 * Created by Zack on 2017/9/9.
 */
/**
 * 加速度传感,陀螺仪，压力传感器，数据收集
 *
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import android.media.MediaRecorder;

public class DeviceSensorService extends Service {
//    public static final String ACTION = "com.example.zack.sensor_back_end.DeviceSensorService";
    private static final String TAG = "DeviceSensorService";
    private MediaRecorder recorder;
    Sensor sensorAcc, sensoGyros, sensoPress;
    SensorManager sm;
    WakeLock m_wklk;




    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Zack onStartCommand");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("DeviceSensorService", "executed at " + new Date().
                        toString());
                if (sm == null) {
                    sm = (SensorManager) getApplicationContext().getSystemService(
                            Context.SENSOR_SERVICE);
                }
                // 加速度感应器
                Sensor sensorAcc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                // 陀螺仪
                Sensor sensoGyros = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                // 压力
                Sensor sensoPress = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
                // 方向
                Sensor sensoOri = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
                // 磁场
                Sensor sensoMag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                // 重力
                Sensor sensoGra = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
                // 线性加速度
                Sensor sensoLinear = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                // 温度
                Sensor sensoTemp = sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
                // 光
                Sensor sensoLight = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
                while (true) {


                    try {
                    /*
                     * 最常用的一个方法 注册事件
                     * 参数1 ：SensorEventListener监听器
                     * 参数2 ：Sensor 一个服务可能有多个Sensor实现，此处调用getDefaultSensor获取默认的Sensor
                     * 参数3 ：模式 可选数据变化的刷新频率，采样率
                     * SENSOR_DELAY_FASTEST,100次左右
                     * SENSOR_DELAY_GAME,50次左右
                     * SENSOR_DELAY_UI,20次左右
                     * SENSOR_DELAY_NORMAL,5次左右
                     */
                        initializeAudio();

                        sm.registerListener(mySensorListener, sensorAcc,
                            SensorManager.SENSOR_DELAY_NORMAL); //以普通采样率注册监听器
                        sm.registerListener(mySensorListener, sensoGyros,
                                SensorManager.SENSOR_DELAY_NORMAL);
                        sm.registerListener(mySensorListener, sensoPress,
                                SensorManager.SENSOR_DELAY_NORMAL);
                        sm.registerListener(mySensorListener, sensoOri,
                                SensorManager.SENSOR_DELAY_NORMAL);
                        sm.registerListener(mySensorListener, sensoMag,
                                SensorManager.SENSOR_DELAY_NORMAL);
                        sm.registerListener(mySensorListener, sensoGra,
                                SensorManager.SENSOR_DELAY_NORMAL);
                        sm.registerListener(mySensorListener, sensoLinear,
                                SensorManager.SENSOR_DELAY_NORMAL);
                        sm.registerListener(mySensorListener, sensoTemp,
                                SensorManager.SENSOR_DELAY_NORMAL);
                        sm.registerListener(mySensorListener, sensoLight,
                                SensorManager.SENSOR_DELAY_NORMAL);

                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        m_wklk = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DeviceSensorService.class.getName());
                        m_wklk.acquire();


                        Thread.sleep(5000);  // 延迟5秒
                        recorder.stop();// 停止刻录
                        // recorder.reset(); // 重新启动MediaRecorder.
                        recorder.release(); // 刻录完成一定要释放资源

                        sm.unregisterListener(mySensorListener);
                        m_wklk.release();
//
                        Thread.sleep(10 * 60 * 1000); // 两次收集间隔时间
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                        break;
                    }

                }

            }
        }).start();



        return Service.START_STICKY;
    }

    private void initializeAudio() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS ");
        String date = sDateFormat.format(new java.util.Date());
        recorder = new MediaRecorder();// new出MediaRecorder对象
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置MediaRecorder的音频源为麦克风
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        // 设置MediaRecorder录制的音频格式
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        // 设置MediaRecorder录制音频的编码为amr
        String record_path = Environment.getExternalStorageDirectory()+ "/sdcard/records/";
        File file_record = new File(record_path);
        if(!file_record.exists()){
            file_record.mkdirs();
        }

        recorder.setOutputFile(record_path + date + ".amr");
        // 设置录制好的音频文件保存路径
        try {
            recorder.prepare();// 准备录制
            recorder.start();// 开始录制
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        Log.i(TAG, "Stop!");
        recorder.stop();// 停止刻录

        recorder.release(); // 刻录完成一定要释放资源
        if (sm != null) {
            sm.unregisterListener(mySensorListener);
            mySensorListener = null;
        }

        if (m_wklk != null) {
            m_wklk.release();
            m_wklk = null;
        }
        Thread.interrupted();

    };



    /*
     * SensorEventListener 接口的实现，需要实现两个方法
     * 方法1 onSensorChanged 当数据变化的时候被触发调用
     * 方法2 onAccuracyChanged 当获得数据的精度发生变化的时候被调用，比如突然无法获得数据时
     */
    private SensorEventListener mySensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (this) {
                int type = sensorEvent.sensor.getType();

                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS ");
                String date = sDateFormat.format(new java.util.Date());

                switch (type) {
                    case Sensor.TYPE_ACCELEROMETER://加速度
//                    acc_count++;
                        float X_lateral = sensorEvent.values[0];
                        float Y_longitudinal = sensorEvent.values[1];
                        float Z_vertical = sensorEvent.values[2];
                        MyLog.i("Accelerometer.txt", "Accelerometer", date + ", "
                                + X_lateral + ", " + Y_longitudinal + ", " + Z_vertical
                                + ";");

                        Log.i("sensor", "Accelerometer:"+ date + ", "
                                + X_lateral + ", " + Y_longitudinal + ", " + Z_vertical
                                + ";");

                        break;

                    case Sensor.TYPE_ORIENTATION://方向
//                    acc_count++;
                        float X_ori = sensorEvent.values[0];
                        float Y_ori = sensorEvent.values[1];
                        float Z_ori = sensorEvent.values[2];
                        MyLog.i("Orientation.txt", "Orientation", date + ", "
                                + X_ori + ", " + Y_ori + ", " + Z_ori
                                + ";");

                        Log.i("sensor", "Orientation:"+ date + ", "
                                + X_ori + ", " + Y_ori + ", " + Z_ori
                                + ";");

                        break;

                    case Sensor.TYPE_GYROSCOPE://陀螺仪
                        //                gyro_count++;
                        float X_laterals = sensorEvent.values[0];
                        float Y_longitudinals = sensorEvent.values[1];
                        float Z_verticals = sensorEvent.values[2];
                        MyLog.i("Gyproscope.txt", "Gyproscope", date + ", "
                                + X_laterals + ", " + Y_longitudinals + ", "
                                + Z_verticals + ";");
                        Log.i("sensor", "Gyproscope:"+ date + ", "
                                + X_laterals + ", " + Y_longitudinals + ", "
                                + Z_verticals + ";");
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD://电磁场
                        //                gyro_count++;
                        float X_magnet = sensorEvent.values[0];
                        float Y_magnet = sensorEvent.values[1];
                        float Z_magnet = sensorEvent.values[2];
                        MyLog.i("Magnetic.txt", "Magnetic_field", date + ", "
                                + X_magnet + ", " + Y_magnet + ", "
                                + Z_magnet + ";");
                        Log.i("sensor", "Magnetic_field:"+ date + ", "
                                + X_magnet + ", " + Y_magnet + ", "
                                + Z_magnet + ";");
                        break;

                    case Sensor.TYPE_GRAVITY://重力
                        //                gyro_count++;
                        float X_gravity = sensorEvent.values[0];
                        float Y_gravity = sensorEvent.values[1];
                        float Z_gravity = sensorEvent.values[2];
                        MyLog.i("Gravity.txt", "Gravity", date + ", "
                                + X_gravity + ", " + Y_gravity + ", "
                                + Z_gravity + ";");
                        Log.i("sensor", "Gravity:"+ date + ", "
                                + X_gravity + ", " + Y_gravity + ", "
                                + Z_gravity + ";");
                        break;

                    case Sensor.TYPE_LINEAR_ACCELERATION://线性加速
                        //                gyro_count++;
                        float X_linear = sensorEvent.values[0];
                        float Y_linear = sensorEvent.values[1];
                        float Z_linear = sensorEvent.values[2];
                        MyLog.i("Linear_acceleration.txt", "Linear_acceleration", date + ", "
                                + X_linear + ", " + Y_linear + ", "
                                + Z_linear + ";");
                        Log.i("sensor", "Linear_acceleration:"+ date + ", "
                                + X_linear + ", " + Y_linear + ", "
                                + Z_linear + ";");
                        break;

                    case Sensor.TYPE_AMBIENT_TEMPERATURE://线性加速
                        //                gyro_count++;
                        float temperature = sensorEvent.values[0];
                        MyLog.i("Temperature.txt", "Temperature", date + ", "
                                + temperature + ";");
                        Log.i("sensor", "Temperature:"+ date + ", "
                                + temperature + ";");
                        break;

                    case Sensor.TYPE_LIGHT://线性加速
                        //                gyro_count++;
                        float light = sensorEvent.values[0];
                        MyLog.i("Light.txt", "Light", date + ", "
                                + light + ";");
                        Log.i("sensor", "Light:"+ date + ", "
                                + light + ";");
                        break;

                    case Sensor.TYPE_PRESSURE://压力
                        //                pres_count++;
                        float X_lateralss = sensorEvent.values[0];
                        MyLog.i("Pressure.txt", "Pressure", date + "," + X_lateralss
                                + ";");
                        Log.i("sensor", "Pressure:"+ date + "," + X_lateralss
                                + ";");

                        break;
                    default:
                        break;
                }
            }
        }




        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i("sensor", "onAccuracyChanged-----sensor"+ sensor + ",acc:" + accuracy);

        }
    };
}