package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.hardware.DcMotorController;

/**
 * Created by BenL on 1/8/16.
 */
public class Robot2Auto extends Robot2Telemetry
{
    private int state;
    public Robot2Auto()
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
        setServoVal("peopleThrower", 0);
        state = 0; // beginning state
    }

    @Override
    public void loop()
    {
        switch (state)
        {
            case 0:
                setMotorTarget("leftDrive", 360);
                setMotorTarget("rightDrive", 360);
                if (!isDriveBusy())
                    state++;
                break;
            case 1:
                break;
        }
        super.loop();
    }
}
