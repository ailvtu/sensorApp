package com.inbot.module.padbotSensor;

import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by ailvtu on 15-11-16.
 */
public class writeData {
    boolean isSD = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    boolean can_write = false;
    File file;
    public void CreateFile(String filename){
        File sd  = Environment.getExternalStorageDirectory();
        can_write = sd.canWrite();
        System.out.println(can_write);
        file = new File(Environment.getExternalStorageDirectory()+"/"+"AATestData"+"/",filename);
    }
    public void CreateFiles(){
        String FilePath = Environment.getExternalStorageDirectory()+"/"+"AATestData"+"/";
        File Dir = new File(FilePath);
        if(!Dir.exists())
            Dir.mkdir();
    }
    public void WriteData(String msg,int flag){
        if(isSD){
            try{
                if(can_write){
                    FileWriter fw = new FileWriter(file,true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    if(flag==1)
                    {
                        bw.write(msg + " ");
                        bw.flush();
                        bw.close();

                    }
                    if (flag==2){
                        bw.write(msg + "\n");
                        bw.flush();
                        bw.close();

                    }
                    // bw.write("/n");

//                    System.out.println("write");
                }
                else {
                    Log.i("PadbotAPP","WriteData error");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public String  GetTime(){
        Time t = new Time();
        t.setToNow();
        int year = t.year;
        int month = t.month+1;
        int day = t.monthDay;
        int hour = t.hour;
        int minu = t.minute;
        int second = t.second;
        String str = month+"-"+day+" "+hour+":"+minu+":"+second;
        return  str;
    }


}
