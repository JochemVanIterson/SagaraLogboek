package nl.audioware.sagaralogboek.Libraries;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileHandler {
    public final String readFromFile(File file) {

        String contents = "";

        try {
            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }

            contents = new String(bytes);
        }
        catch (FileNotFoundException e) {
            Log.e("FileHandler", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("FileHandler", "Can not read file: " + e.toString());
        }

        return contents;
    }
}
