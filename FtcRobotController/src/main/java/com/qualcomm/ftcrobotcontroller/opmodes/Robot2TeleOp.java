package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.ftccommon.DbgLog;

/**
 * Created by BenL on 1/6/16.
 */
public class Robot2TeleOp extends Robot2Telemetry
{
    private int peopleServoPos;
    private boolean dpadLeft_old;

    private boolean leftTrigActive;
    private boolean rightTrigActive;

    private boolean x_old;
    private boolean b_old;
    
    private int[] encInitDrive;
    private int encDumpCount;

    public Robot2TeleOp()
    {
        super(false); // Do not use drive encoders
    }

    // Scale linear input to quartic for more accuracy in lower ranges
    double scaleInput(double input)
    {
        double scaledInput = 0;
        if (input == 0)
        {
            scaledInput = 0;
        }
        else if (input < 0)
        {
            scaledInput = -(Math.pow(Math.abs(input) + 0.2, 4) / 2 + 0.05);
            if (scaledInput < -1)
                scaledInput = -1;
        }
        else
        {
            scaledInput = (Math.pow(Math.abs(input) + 0.2, 4) / 2 + 0.05);
            if (scaledInput > 1)
                scaledInput = 1;
        }
        return scaledInput;
    }

    double deadband(double input)
    {
        if (input > -0.05 && input < 0.05)
            input = 0;
        return input;
    }

    // Takes x and y value of joystick, returns drivePower[leftDrive, rightDrive]
    private double[] getDrivePower(double x, double y)
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

    // Convert Degrees to motor encoder steps
    int degToEncoder(int degrees)
    {
        return (int) Math.round((double) degrees * 1450 / 360);
    }

    @Override
    public void init()
    {
        super.init();
        initTelemetry();

        // Set default values for different modes
        leftTrigActive = false;
        rightTrigActive = false;

        encInitDrive = new int[4];
        encDumpCount = 0;
        peopleServoPos = 2;
        x_old = false;
        b_old = false;
        dpadLeft_old = false;
    }
    @Override
    public void start()
    {
        super.start();
        setServoVal("claws", 0.4);
        //setServoVal("peoplePutter", 1.0);
    }
    @Override
    public void loop()
    {
        // Log Motor Positions
        /*
        if (gamepad1.b && !b_old) // Begin Log
        {
            encInitDrive[0] = getMotorPosition("frontLeftDrive");
            encInitDrive[1] = getMotorPosition("frontRightDrive");
            encInitDrive[2] = getMotorPosition("backLeftDrive");
            encInitDrive[3] = getMotorPosition("backRightDrive");
        }
        if (gamepad1.x && !x_old) // Record Log
        {
            encDumpCount++;
            DbgLog.msg("ENC Dump #" + encDumpCount);
            DbgLog.msg("ENC Dump - Front Left Drive: " + (getMotorPosition("frontLeftDrive") - encInitDrive[0]));
            DbgLog.msg("ENC Dump - Front Right Drive: " + (getMotorPosition("frontRightDrive") - encInitDrive[1]));
            DbgLog.msg("ENC Dump - Back Left Drive: " + (getMotorPosition("backLeftDrive") - encInitDrive[2]));
            DbgLog.msg("ENC Dump - Back Right Drive: " + (getMotorPosition("backRightDrive") - encInitDrive[3]));
        }*/

        // Change Modes
        if (gamepad1.dpad_up)
            setDriveDirection(RobotDirection.INTAKE);
        if (gamepad1.dpad_down)
            setDriveDirection(RobotDirection.WINCH);

        // People Control
        if (gamepad2.left_bumper)
            setServoVal("peoplePutter", getServoPos("peoplePutter")+0.01);
        else if (gamepad2.left_trigger > 0.2)
            setServoVal("peoplePutter", getServoPos("peoplePutter")-0.01);

        // Drive Control
        double[] drivePower = getDrivePower(-scaleInput(deadband(gamepad1.left_stick_x)),
                                            -scaleInput(deadband(gamepad1.left_stick_y)));

        setNonEncodedMotorPower("frontLeftDrive", drivePower[0]);
        setNonEncodedMotorPower("frontRightDrive", drivePower[1]);
        setNonEncodedMotorPower("backLeftDrive", drivePower[0]);
        setNonEncodedMotorPower("backRightDrive", drivePower[1]);

        // Brush Control
        if (gamepad1.right_bumper)
            setNonEncodedMotorPower("brush", 1.0);
        else
            setNonEncodedMotorPower("brush", -gamepad1.right_trigger);

        // Trigger Control

        if (gamepad1.x && !x_old)
            leftTrigActive = !leftTrigActive;
        if (gamepad1.b && !b_old)
            rightTrigActive = !rightTrigActive;

        if (leftTrigActive)
            setServoVal("leftTrigger", 0.73);
        else
            setServoVal("leftTrigger", 0.1);

        if (rightTrigActive)
            setServoVal("rightTrigger", 0.3);
        else
            setServoVal("rightTrigger", 0.85);

        // Box Slide Control
        if (gamepad2.x)
            setMotorTarget("boxSlide", getMotorTarget("boxSlide") / 1440f * 360 - 3);
        else if (gamepad2.b)
            setMotorTarget("boxSlide", getMotorTarget("boxSlide") / 1440f * 360 + 3);

        addTelemetry("Motor Target: ", String.valueOf(getMotorTarget("boxSlide")));

        // Winch Control
        setNonEncodedMotorPower("winch", gamepad2.left_stick_y);

        if (gamepad2.right_bumper )//&& !leftBmp_old)
            setServoVal("winchAngle", getServoPos("winchAngle")-0.001);
        else if (gamepad2.right_trigger > 0.2)// && leftTrig_old < 0.2)
            setServoVal("winchAngle", getServoPos("winchAngle")+0.001);

        // Claw Control
        if (gamepad1.y)
            setServoVal("claws", 0.4);
        else if (gamepad1.a)
            setServoVal("claws", 0.1);

        // Telemetry

        addTelemetry("Drive Direction", getDriveDirection().name());

        updateTelemetry();

        // Old Values
        x_old = gamepad1.x;
        b_old = gamepad1.b;
        
        dpadLeft_old = gamepad1.dpad_left;

        // Call loop in Robot Hardware
        super.loop();
    }
}
