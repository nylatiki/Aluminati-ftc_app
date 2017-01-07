package org.firstinspires.ftc.teamcode;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by benlimpa on 12/2/16.
 */

public class MatchSpecificVars
{
    HashMap<String, Double> matchVars;

    private boolean scoreCapBall = false;
    private double startOffset = 0;
    private int startWait = 0;

    public MatchSpecificVars(String fileName)
    {
        matchVars = new HashMap<>();

        File directory = Environment.getExternalStorageDirectory();
        File moveFile = new File(directory, fileName);

        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(moveFile));
            String line;

            while ((line = reader.readLine()) != null) {

                if (!(line.contains("#") || line.length() < 3))
                {
                    // Parse matchVars instructions
                    //line = line.substring(0, line.length());
                    String[] keyPair = line.split("=");
                    if (keyPair[0].equals("scoreCapBall"))
                        scoreCapBall = Boolean.parseBoolean(keyPair[1]);
                    else if (keyPair[0].equals("startOffset"))
                        startOffset = Double.parseDouble(keyPair[1]);
                    else if (keyPair[0].equals("startWait"))
                        startWait = Integer.parseInt(keyPair[1]);
                }
            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean scoreCapBall() {return scoreCapBall;}
    public double getStartOffset() {return startOffset;}
    public int getStartWait() {return startWait;}

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
