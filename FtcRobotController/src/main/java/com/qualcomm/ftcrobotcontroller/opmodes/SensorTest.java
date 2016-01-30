package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;

/**
 * Created by BenL on 12/22/15.
 */
public class SensorTest extends OpMode
{

    ColorSensor sensor;
    DeviceInterfaceModule interfaceModule;

    @Override
    public void init()
    {
        interfaceModule = hardwareMap.deviceInterfaceModule.get("interfaceModule");

        //interfaceModule.setDigitalChannelMode(5, DigitalChannelController.Mode.OUTPUT);

        sensor = hardwareMap.colorSensor.get("colorSensor");
    }

    @Override
    public void loop()
    {
        int brightestColor = Math.max(Math.max(sensor.red(), sensor.green()), sensor.blue());

        if (brightestColor == sensor.red())
            telemetry.addData("Current Color: ", "RED");
        else if (brightestColor == sensor.green())
            telemetry.addData("Current Color: ", "GREEN");

        else if (brightestColor == sensor.blue())
            telemetry.addData("Current Color: ", "BLUE");

        telemetry.addData("Color Sensor: ", sensor.toString());
        telemetry.addData("Red: ", sensor.red());
        telemetry.addData("Green: ", sensor.green());
        telemetry.addData("Blue:  ", sensor.blue());
        telemetry.addData("Alpha: ", sensor.alpha());
    }

    @Override
    public void stop()
    {
        // nothing to stop
    }
}
