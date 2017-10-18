package com.example.zack.sensor_back_end;

import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity
{
    private Button startBtn;
    private Button stopBtn;
    public static boolean flag_stop;
    public static int count;

    private CreateUserPopWin createUserPopWin;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (Button) findViewById(R.id.startBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        //添加监听器
        startBtn.setOnClickListener(listener);
        stopBtn.setOnClickListener(listener);

    }
    //启动监听器
    private OnClickListener listener=new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Intent intent=new Intent(MainActivity.this, DeviceSensorService.class);
            switch (v.getId())
            {
                case R.id.startBtn:
                    System.out.println("Start DeviceSensor service...");
                    calcount();
                    new AlertDialog.Builder(MainActivity.this).setTitle("消息")//设置对话框标题
                            .setMessage("开始收集！")//设置显示的内容
                    .show();//在按键响应事件中显示此对话框
                    flag_stop = true;
                    startService(intent);
                    break;
                case R.id.stopBtn:
                    showEditPopWin();
                    System.out.println("Stop DeviceSensor service...");
                    flag_stop = false;
                    stopService(intent);
                    break;
                default:
                    break;
            }
        }
    };

    public void showEditPopWin() {
        createUserPopWin = new CreateUserPopWin(this,onClickListener);
        createUserPopWin.showAtLocation(findViewById(R.id.main_view), Gravity.CENTER, 0, 0);
    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_save_pop:

                    String name1 = createUserPopWin.text_name.getText().toString().trim();
                    String mobile1 = createUserPopWin.text_mobile.getText().toString().trim();
                    String info1 = createUserPopWin.text_info.getText().toString().trim();


                    System.out.println(name1+"——"+mobile1+"——"+info1);
                    writeUserprofile(name1, mobile1, info1);

                    createUserPopWin.dismiss();
                    finish();
//                    Thread.interrupted();
//                    DeviceSensorService.thread.interrupt();
//                    DeviceSensorService.thread.stop();
                    break;
            }
        }
    };

    private void calcount() {
        for (count = 1; count <= 1000; count++) {
            String record_path = Environment.getExternalStorageDirectory() + "/sdcard/data/";
            File file_record = new File(record_path + Integer.toString(count) + "/");
            if (!file_record.exists()) {
                file_record.mkdirs();
                break;
            }

        }
    }

    private void writeUserprofile(String name, String mobile, String info) {
        String record_path = Environment.getExternalStorageDirectory() + "/sdcard/data/";
        File file_record = new File(record_path + Integer.toString(count) + "/Userprofile.txt");
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS ");
        String date = sDateFormat.format(new java.util.Date());
        try {

            FileWriter filerWriter = new FileWriter(file_record, true);//后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write("姓名:" + name);
            bufWriter.newLine();
            bufWriter.write("手机:" + mobile);
            bufWriter.newLine();
            bufWriter.write("标签:" + info);
            bufWriter.newLine();
            bufWriter.write("时间:" + date);
            bufWriter.close();
            filerWriter.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



    }
}