package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

/**
 * Created by BenL on 10/7/15.
 */
public class KendraTankDrive extends OpMode
{

    DcMotorController.DeviceMode    devMode;
    DcMotorController               motorControl;
    DcMotor                         leftMotor;
    DcMotor                         rightMotor;
    int                             loops;

    @Override
    public void init()
    {
        leftMotor = hardwareMap.dcMotor.get("leftMotor");
        rightMotor = hardwareMap.dcMotor.get("rightMotor");
        motorControl = hardwareMap.dcMotorController.get("MotorController1");

        leftMotor.setChannelMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);

        rightMotor.setChannelMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
        rightMotor.setDirection(DcMotor.Direction.REVERSE);
        motorControl.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
    }

    @Override
    public void loop()
    {
        if (devMode == DcMotorController.DeviceMode.WRITE_ONLY)
        {
            double[] drivePower = getDrivePower(-scaleInput(gamepad1.left_stick_x), -scaleInput(gamepad1.left_stick_y));

            telemetry.addData("Left Drive: ", drivePower[0]);
            telemetry.addData("Right Drive: ", drivePower[1]);
            leftMotor.setPower(drivePower[0]);
            rightMotor.setPower(drivePower[1]);
        }

        if (devMode == DcMotorController.DeviceMode.READ_ONLY)
        {
            telemetry.addData("Left Drive Power: ", leftMotor.getPower());
            telemetry.addData("Left Drive Power: ", leftMotor.getPower());
        }
/*
        if ((loops % 17) == 0)
        {
            motorControl.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_ONLY);

        }
        */
        devMode = motorControl.getMotorControllerDeviceMode();
        loops++;
    }

    @Override
    public void stop()
    {
        // nothing to stop
    }

    private double[] getDrivePower(double x, double y) // Takes x and y value of joystick, returns drivePower[leftDrive, rightDrive]
    {
        double[] drivePower = new double[2];

        /*
         *  The Following algorithm is Kendra's Joystick to Tank Drive Conversion (http://home.kendra.com/mauser/Joystick.html)
         */

        double rightPlusLeft     = (100 - Math.abs(x * 100)) * y + (y * 100);
        double rightMinusLeft    = (100 - Math.abs(y * 100)) * x + (x * 100);

        drivePower[0] = (rightPlusLeft-rightMinusLeft)/200; // left drive power
        drivePower[1] = (rightPlusLeft+rightMinusLeft)/200; // right drive power

        // Make sure that the power is not out of bounds
        for (int index = 0; index <= 1; index++)
        {
            if (Math.abs(drivePower[index]) > 1.0)
            {
                drivePower[index] = 1f;
            }
        }

        return drivePower;
    }

    // Input scale from NXTTeleop for more accuracy in lower ranges
    double scaleInput(double dVal)
    {
        double[] scaleArray = { 0.0, 0.05, 0.09, 0.10, 0.12, 0.15, 0.18, 0.24,
                0.30, 0.36, 0.43, 0.50, 0.60, 0.72, 0.85, 1.00, 1.00 };

        // get the corresponding index for the scaleInput array.
        int index = (int) (dVal * 16.0);

        // index should be positive.
        if (index < 0) {
            index = -index;
        }

        // index cannot exceed size of array minus 1.
        if (index > 16) {
            index = 16;
        }

        // get value from the array.
        double dScale = 0.0;
        if (dVal < 0) {
            dScale = -scaleArray[index];
        } else {
            dScale = scaleArray[index];
        }

        // return scaled value.
        return dScale;
    }
}
