package com.qualcomm.ftcrobotcontroller.opmodes;

/**
 * Created by BenL on 1/6/16.
 */
public class Robot2TeleOp extends Robot2Telemetry
{
    private int peopleServoPos;
    private boolean dpadLeft_old;

    public Robot2TeleOp()
    {
        super(false); // Do not use drive encoders
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
        initTelemetry();

        // Set default values for different modes
        peopleServoPos = 2;
        dpadLeft_old = false;
    }
    @Override
    public void loop()
    {
        // Change Modes
        if (gamepad1.dpad_up)
            setDriveDirection(RobotDirection.INTAKE);
        if (gamepad1.dpad_down)
            setDriveDirection(RobotDirection.WINCH);

        // People Control
        if (gamepad1.dpad_left & !dpadLeft_old)
            peopleServoPos++;

        if (peopleServoPos > 3)
            peopleServoPos = 1;

        switch (peopleServoPos)
        {
            case 1:
                setServoVal("peopleThrower", 0);
                break;
            case 2:
                setServoVal("peopleThrower", 0.5);
                break;
            case 3:
                setServoVal("peopleThrower", 1);
                break;
        }

        // Drive Control
        double[] drivePower = getDrivePower(-scaleInput(gamepad1.left_stick_x), -scaleInput(gamepad1.left_stick_y));

        setNonEncodedMotorPower("frontLeftDrive", drivePower[0]);
        setNonEncodedMotorPower("frontRightDrive", drivePower[1]);
        setNonEncodedMotorPower("backLeftDrive", drivePower[0]);
        setNonEncodedMotorPower("backRightDrive", drivePower[1]);

        // Brush Control
        if (gamepad1.right_bumper)
            setNonEncodedMotorPower("brush", 0.4);
        else
            setNonEncodedMotorPower("brush", -gamepad1.right_trigger);

        // Box Slide Control
        if (gamepad1.x)
            setMotorTarget("boxSlide", getMotorTarget("boxSlide") / 1440f * 360 - 3);
        else if (gamepad1.b)
            setMotorTarget("boxSlide", getMotorTarget("boxSlide") / 1440f * 360 + 3);

        addTelemetry("Motor Target: ", String.valueOf(getMotorTarget("boxSlide")));

        // Winch Control
        setNonEncodedMotorPower("winch", gamepad1.right_stick_y);
        
        /*
         *  Servos
         */

        // Telemetry

        addTelemetry("Drive Direction", getDriveDirection().name());

        updateTelemetry();

        // Old Values
        dpadLeft_old = gamepad1.dpad_left;

        // Call loop in Robot Hardware
        super.loop();
    }
}
