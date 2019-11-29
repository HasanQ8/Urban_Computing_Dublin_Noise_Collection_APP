package com.hasanfakhra.noise_detector;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;


class FileUtil {


    public static final String LOCAL = "DublinSound";

    public static final String LOCAL_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator;

    public static final String REC_PATH = LOCAL_PATH + LOCAL + File.separator;

    static {
        File dirRootFile = new File(LOCAL_PATH);
        if (!dirRootFile.exists()) {
            dirRootFile.mkdirs();
        }
        File recFile = new File(REC_PATH);
        if (!recFile.exists()) {
            recFile.mkdirs();
        }
    }

    private FileUtil() {
    }



    public static File createFile(Context context, String fileName) {

        File myCaptureFile = new File(REC_PATH + fileName);
        if (myCaptureFile.exists()) {
            myCaptureFile.delete();
        }
        try {
            myCaptureFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myCaptureFile;
    }


}