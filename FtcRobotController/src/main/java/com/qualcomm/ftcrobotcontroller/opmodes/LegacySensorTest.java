package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.LegacyModule;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

/**
 * Created by BenL on 7/23/16.
 */
public class LegacySensorTest extends OpMode
{
    UltrasonicSensor ultraSensor;
    LegacyModule legacyMod;

    @Override
    public void init()
    {
        legacyMod = hardwareMap.legacyModule.get("legacyMod");
        ultraSensor = hardwareMap.ultrasonicSensor.get("ultraSensor");
    }

    @Override
    public void start()
    {

    }

    @Override
    public void loop()
    {
        telemetry.addData("Status: ", ultraSensor.status());
        telemetry.addData("Level: ", ultraSensor.getUltrasonicLevel());
    }
}
