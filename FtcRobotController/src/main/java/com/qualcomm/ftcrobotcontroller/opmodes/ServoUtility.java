package com.qualcomm.ftcrobotcontroller.opmodes;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by BenL on 11/23/15.
 */

public class ServoUtility extends OpMode
{

    ServoController servoControl;
    Servo           servo;
    double          servoPos;
    boolean         oldX;
    boolean         oldY;
    boolean         oldLB;
    boolean         oldRB;
    double          interval;

    @Override
    public void init()
    {

        servo        = hardwareMap.servo.get("servo");
        servoControl = hardwareMap.servoController.get("ServoControl");

        servoPos = 0;
        interval = 0.1;
    }

    @Override
    public void loop()
    {

        // Change increment/decrement interval
        if (gamepad1.left_bumper && !oldLB)
            interval -= 0.05;
        else if (gamepad1.right_bumper && !oldRB)
            interval += 0.05;

        // Increment or decrement the servo position once
        if (gamepad1.x && !oldX)
            servoPos -= interval;
        else if (gamepad1.y && !oldY)
            servoPos += interval;

        // Set Servo Position to extrema
        if (gamepad1.dpad_down)
            servoPos = 0;
        else if (gamepad1.dpad_up)
            servoPos = 1;
        else if (gamepad1.dpad_right)
            servoPos = 0.5;

        if (servoPos < 0)
            servoPos = 0;
        else if (servoPos > 1)
            servoPos = 1;

        servo.setPosition(servoPos);

        oldX = gamepad1.x;
        oldY = gamepad1.y;
        oldLB = gamepad1.left_bumper;
        oldRB = gamepad1.right_bumper;

        telemetry.addData("Servo Value:  ", servoPos);
        telemetry.addData("Increment/Decrement Interval: ", interval);
    }

    @Override
    public void stop()
    {
        // nothing to stop
    }

    // Input scale from NXTTeleOp for more accuracy in lower ranges
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
