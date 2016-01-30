package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.hardware.DcMotorController;

/**
 * Created by BenL on 1/8/16.
 */
public class RobotAuto extends RobotTelemetry
{
    private int state;
    public RobotAuto()
    {
        super(true); // Use drive encoders
    }

    @Override
    public void init()
    {
        initTelemetry();
    }

    @Override
    public void start()
    {
        state = 0; // beginning state
    }

    @Override
    public void loop()
    {
        switch (state)
        {
            case 0:
                setEncodedMotorPos("leftDrive", 1450);
                setEncodedMotorPos("rightDrive", 1450);
                state++;
                break;
            case 10:
                setControllerMode(-1, DcMotorController.DeviceMode.READ_ONLY); // set drive controllers to read mode
                state++;
                break;
            case 20:
                if (isControllerModeChanged(-1))
                {
                    state++;
                }
                break;
            case 30:
                if (!isDriveRunning())
                {
                    state++;
                }
                break;
        }
        super.loop();
        //sleep(60);
    }

    public static void sleep(long sleepTime)
    {
        long wakeupTime = System.currentTimeMillis() + sleepTime;

        while (sleepTime > 0)
        {
            try
            {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
            }
            sleepTime = wakeupTime - System.currentTimeMillis();
        }
    }   //sleep

}
