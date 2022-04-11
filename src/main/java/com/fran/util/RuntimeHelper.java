package com.fran.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class RuntimeHelper {

    private static RuntimeHelper mInstance;

    public static RuntimeHelper getInstance() {
        if (mInstance == null)
            synchronized (RuntimeHelper.class) {
                if (mInstance == null)
                    mInstance = new RuntimeHelper();
            }

        return mInstance;
    }

    //    执行DOS命令,exe等
    public void run(String command) {
        Runtime run = Runtime.getRuntime();
        try {
            Utils.log(command);
            if (System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS"))
                command = "cmd /c " + command;

            Process process = run.exec(command);
            InputStream reader = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(reader));
            String ss;
            while ((ss = bufferedReader.readLine()) != null) {
                Utils.log(ss);
            }
            if (process.waitFor() == 0) {
//                    Utils.log("执行成功");
            } else {
                Utils.log("执行失败: " + process.waitFor());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
