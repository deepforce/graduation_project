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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import android.media.AudioFormat;
import android.media.AudioRecord;
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
    public static boolean recordering = false;
    private MediaRecorder recorder;
    Sensor sensorAcc, sensoGyros, sensoPress;
    SensorManager sm;
    WakeLock m_wklk;
    public static Thread thread;

    private static AudioRecord audioRecord;

    private static String currentFilePath;

    // 音频获取源
    private static int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准
    private static int sampleRateInHz = 44100;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private static int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
            channelConfig, audioFormat);




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
        class senThread implements Runnable {
            private MyThead myThead;
            public void run() {
                Log.d("DeviceSensorService", "executed at " + new Date().
                        toString());

//
                if (sm == null) {
                    sm = (SensorManager) getApplicationContext().getSystemService(
                            Context.SENSOR_SERVICE);
                }
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
//

                        initializeAudio();
                        recordering = true;
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
                        myThead = new MyThead();
                        Thread au_thread = new Thread(myThead);
                        au_thread.start();
                        Thread.sleep(30 * 1000);  // 延迟30秒
                        recordering = false;
                        sm.unregisterListener(mySensorListener);
                        Thread.sleep(10 * 60 * 1000); // 两次收集间隔时间
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                        //break;
                    }
                }
            }


            class MyThead implements Runnable {
                public void run()
                {
                    writeDateTOFile();
                    copyWaveFile(currentFilePath, currentFilePath.replace(".pcm", ".wav"));//给裸数据加上头文件
                }

            }




        }
        senThread mythread = new senThread();
        thread = new Thread(mythread);
        thread.start();
        return Service.START_STICKY;
    }

    private void initializeAudio() {


        String record_path = Environment.getExternalStorageDirectory()+ "/sdcard/data/" + Integer.toString(MainActivity.count) + "/records/";
        File file_record = new File(record_path);
        if(!file_record.exists()){
            file_record.mkdirs();
        }

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS ");
        String date = sDateFormat.format(new java.util.Date());

        currentFilePath = record_path + date + ".pcm";
        audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
        audioRecord.startRecording();

    }



    public void onDestroy() {
        Log.i(TAG, "Stop!");
        recordering = false;
        if (sm != null) {
            sm.unregisterListener(mySensorListener);
            mySensorListener = null;
        }

        if (m_wklk != null) {
            m_wklk.release();
            m_wklk = null;
        }



    };

    private static void writeDateTOFile() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        final byte[] audiodata = new byte[bufferSizeInBytes];
        FileOutputStream fos = null;
        try {
            File file = new File(currentFilePath);
            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (recordering == true) {
            final int readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                try {
                    fos.write(audiodata);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        try {
            fos.close();// 关闭写入流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 这里得到可播放的音频文件
    private static void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRateInHz;
        int channels = 2;
        long byteRate = 16 * sampleRateInHz * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
     * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
     * 自己特有的头文件。
     */
    private static void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                            long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

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