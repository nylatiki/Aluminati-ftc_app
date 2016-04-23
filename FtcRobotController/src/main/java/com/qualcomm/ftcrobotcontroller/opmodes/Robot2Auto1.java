package com.qualcomm.ftcrobotcontroller.opmodes;

import android.os.Environment;

import com.qualcomm.ftccommon.DbgLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by BenL on 1/8/16.
 */
public class Robot2Auto1 extends Robot2Telemetry
{
    // 540 = 180 turn
    // 270 = one robot length

    // Movement
    private HashMap<String, Double> moveVals;

    // Variables
    protected enum TeamColor {RED_LEFT, BLUE_RIGHT}
    private int state;
    private int waitVal;
    private TeamColor SIDE;
    private ColorComponent teamColor;

    private boolean encoderReset;
    //private final TeamColor SIDE;
    public Robot2Auto1(TeamColor side)
    {
        super(true); // Use drive encoders
        SIDE = side;
    }

    @Override
    public void init()
    {
        waitVal = 0;
        moveVals = new HashMap<String, Double>();

        loadMovement();

        switch (SIDE)
        {
            case RED_LEFT:
                teamColor = ColorComponent.RED;

                // Invert turns
                moveVals.put("turn1", -moveVals.get("turn1"));
                moveVals.put("turn2", -moveVals.get("turn2"));
                moveVals.put("turn5", -moveVals.get("turn5"));
                moveVals.put("turn7", -moveVals.get("turn7"));
                break;
            default:
            case BLUE_RIGHT:
                teamColor = ColorComponent.BLUE;
                break;
        }
        //SIDE = side;

        initTelemetry();
        //resetDrive(); // Start by resetting encoders
    }

    @Override
    public void start()
    {
        setDriveDirection(RobotDirection.WINCH);
        setServoVal("peoplePutter", 1);
        setServoVal("claws", 0.65);
        setServoVal("leftTrigger", 0.1);
        setServoVal("rightTrigger", 0.75);
        state = 0; // beginning state

    }

    @Override
    public void loop()
    {
        DbgLog.msg("AutoLoop: " + state);
        switch (state) {
            case 0: // init stuff
                if (isDriveDone())
                    state++;
                break;
            case 1: // Move Forward
                setMotorTarget("frontLeftDrive", moveVals.get("initForward"));
                setMotorTarget("frontRightDrive", moveVals.get("initForward"));
                setMotorTarget("backLeftDrive", moveVals.get("initForward"));
                setMotorTarget("backRightDrive", moveVals.get("initForward"));
                state++;
                break;
            case 2: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 3:
                setMotorTarget("frontLeftDrive", moveVals.get("turn1"));
                setMotorTarget("frontRightDrive", -moveVals.get("turn1"));
                setMotorTarget("backLeftDrive", moveVals.get("turn1"));
                setMotorTarget("backRightDrive", -moveVals.get("turn1"));
                state++;
                break;
            case 4: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 5:
                setMotorTarget("frontLeftDrive", moveVals.get("moveAcrossField"));
                setMotorTarget("frontRightDrive", moveVals.get("moveAcrossField"));
                setMotorTarget("backLeftDrive", moveVals.get("moveAcrossField"));
                setMotorTarget("backRightDrive", moveVals.get("moveAcrossField"));
                state++;
                break;
            case 6: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 7:
                setMotorTarget("frontLeftDrive", moveVals.get("turn2"));
                setMotorTarget("frontRightDrive", -moveVals.get("turn2"));
                setMotorTarget("backLeftDrive", moveVals.get("turn2"));
                setMotorTarget("backRightDrive", -moveVals.get("turn2"));
                state++;
                break;
            case 8: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 9:
                setMotorTarget("frontLeftDrive", moveVals.get("moveToButton"));
                setMotorTarget("frontRightDrive", moveVals.get("moveToButton"));
                setMotorTarget("backLeftDrive", moveVals.get("moveToButton"));
                setMotorTarget("backRightDrive", moveVals.get("moveToButton"));
                state++;
                break;
            case 10: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 11:
                setServoVal("peoplePutter", 0);
                state++;
                break;
            case 12:
                if (getServoCurrentPos("peoplePutter") < 0.1)
                    waitVal++;
                if (waitVal > 300)
                    state++;
                break;
            case 13:
                setMotorTarget("frontLeftDrive", -270);
                setMotorTarget("frontRightDrive", -270);
                setMotorTarget("backLeftDrive", -270);
                setMotorTarget("backRightDrive", -270);
                state++;
                break;
            case 14: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 15:
                setMotorTarget("frontLeftDrive", 270);
                setMotorTarget("frontRightDrive", -270);
                setMotorTarget("backLeftDrive", 270);
                setMotorTarget("backRightDrive", -270);
                state++;
                break;
            case 16: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state = 99;
                }
                break;
            case 17:
                setMotorTarget("frontLeftDrive", 270);
                setMotorTarget("frontRightDrive", 270);
                setMotorTarget("backLeftDrive", 270);
                setMotorTarget("backRightDrive", 270);
                state++;
                break;
            case 18: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 19:
                setMotorTarget("frontLeftDrive", -270);
                setMotorTarget("frontRightDrive", 270);
                setMotorTarget("backLeftDrive", -270);
                setMotorTarget("backRightDrive", 270);
                state++;
                break;
            case 20: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            /*
            case 14:
                //setMotorTarget("frontLeftDrive", moveVals.get("peopleBack"));
                //setMotorTarget("frontRightDrive", moveVals.get("peopleBack"));
                //setMotorTarget("backLeftDrive", moveVals.get("peopleBack"));
                //setMotorTarget("backRightDrive", moveVals.get("peopleBack"));
                state++;
                break;
            case 15: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 16:
                if (Math.max(getColor(ColorComponent.BLUE), getColor(ColorComponent.RED)) == getColor(teamColor))
                    state = 50;
                else
                    state++;
                break;
            case 17: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 18:
                setMotorTarget("frontLeftDrive", -moveVals.get("back4"));
                setMotorTarget("frontRightDrive", -moveVals.get("back4"));
                setMotorTarget("backLeftDrive", -moveVals.get("back4"));
                setMotorTarget("backRightDrive", -moveVals.get("back4"));
                state++;
                break;
            case 19: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 20:
                setMotorTarget("frontLeftDrive", moveVals.get("turn5"));
                setMotorTarget("frontRightDrive", -moveVals.get("turn5"));
                setMotorTarget("backLeftDrive", moveVals.get("turn5"));
                setMotorTarget("backRightDrive", -moveVals.get("turn5"));
                state++;
                break;
            case 21: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 22:
                setMotorTarget("frontLeftDrive", moveVals.get("forward6"));
                setMotorTarget("frontRightDrive", moveVals.get("forward6"));
                setMotorTarget("backLeftDrive", moveVals.get("forward6"));
                setMotorTarget("backRightDrive", moveVals.get("forward6"));
                state++;
                break;
            case 23: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 24:
                setMotorTarget("frontLeftDrive", -moveVals.get("turn7"));
                setMotorTarget("frontRightDrive", moveVals.get("turn7"));
                setMotorTarget("backLeftDrive", -moveVals.get("turn7"));
                setMotorTarget("backRightDrive", moveVals.get("turn7"));
                state++;
                break;
            case 25: // Check if Drive is busy
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 26:
                setMotorTarget("frontLeftDrive", moveVals.get("forward8"));
                setMotorTarget("frontRightDrive", moveVals.get("forward8"));
                setMotorTarget("backLeftDrive", moveVals.get("forward8"));
                setMotorTarget("backRightDrive", moveVals.get("forward8"));
                state = 50;
                break;

            case 50: // Correct Button
                if (isDriveDone())
                {
                    resetDrive();
                    state++;
                }
                break;
            case 51:
                setMotorTarget("frontLeftDrive", moveVals.get("btnPress"));
                setMotorTarget("frontRightDrive", moveVals.get("btnPress"));
                setMotorTarget("backLeftDrive", moveVals.get("btnPress"));
                setMotorTarget("backRightDrive", moveVals.get("btnPress"));
                state++;
                break;
                */
        }


        DbgLog.msg("Front Left Target: " + String.valueOf(getMotorTarget("frontLeftDrive")));
        DbgLog.msg("Front Right Target: " + String.valueOf(getMotorTarget("frontRightDrive")));
        DbgLog.msg("Back Left Target: " + String.valueOf(getMotorTarget("backLeftDrive")));
        DbgLog.msg("Back Right Target: " + String.valueOf(getMotorTarget("backRightDrive")));

        DbgLog.msg("-----------------------------------------------------------------------");

        super.loop();
    }

    private void loadMovement()
    {
        File directory = Environment.getExternalStorageDirectory();
        File moveFile = new File(directory, "move.txt");

        HashMap<String, Integer> movementVals;

        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(moveFile));
            String line;

            while ((line = reader.readLine()) != null) {

                if (!(line.contains("#") || line.length() < 3))
                {
                    // Parse movement instructions
                    //line = line.substring(0, line.length());
                    String[] keyPair = line.split("=");
                    moveVals.put(keyPair[0], Double.parseDouble(keyPair[1]));
                }
            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private void resetDrive()
    {
        resetEncoders("driveMotors");
        //encoderReset = true;
    }
}
