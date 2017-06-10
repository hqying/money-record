package com.wordpress.jlvivit.moneyrecord;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.wordpress.jlvivit.moneyrecord.data.MoneyRecordDbHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Calendar;

public class Utility {

    private static final Calendar calendar = Calendar.getInstance();

    /*
     * Can't find a good Calendar method to do this for now. Working on it.
     */
    public static int getMaxDay(int year, int month) {
        Integer[] days31 = new Integer[] {1, 3, 5, 7, 8, 10, 12};
        Integer[] days30 = new Integer[] {4, 6, 9, 11};
        int maxDay;
        if (year == calendar.get(Calendar.YEAR) &&
                month == calendar.get(Calendar.MONTH)) {
            maxDay = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            if (Arrays.asList(days31).contains(month + 1)) {
                maxDay = 31;
            } else if (Arrays.asList(days30).contains(month + 1)) {
                maxDay = 30;
            } else if (month + 1 == 2) {
                maxDay = year % 4 == 0 ? 29 : 28;
            } else {
                throw new UnsupportedOperationException("Wrong month selection??!!??");
            }
        }
        return maxDay;
    }

    public static final String ACTION_FINISH_EXPORT = "com.wordpress.jlvivit.moneyrecord.ACTION_FINISH_EXPORT";

    public static String exportDbFile(Context context) {
        String savePath;
        File dbFile = context.getDatabasePath(MoneyRecordDbHelper.DATABASE_NAME);
        File outputFile;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            outputFile = new File(context.getExternalFilesDir(null), MoneyRecordDbHelper.DATABASE_NAME);
            try {
                savePath = outputFile.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Toast.makeText(context, "Can't export file to external storage.", Toast.LENGTH_LONG).show();
            return null;
        }
        if (dbFile.exists()) {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(dbFile);
                fos = new FileOutputStream(outputFile);
                FileChannel inputFileChannel = fis.getChannel();
                FileChannel outputFileChannel = fos.getChannel();
                inputFileChannel.transferTo(0, fis.available(), outputFileChannel);
                inputFileChannel.close();
                outputFileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return savePath;
    }
}
