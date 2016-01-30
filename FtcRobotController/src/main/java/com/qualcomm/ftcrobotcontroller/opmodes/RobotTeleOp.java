package com.qualcomm.ftcrobotcontroller.opmodes;

/**
 * Created by BenL on 1/6/16.
 */
public class RobotTeleOp extends RobotTelemetry
{
    private boolean manualRangle;
    private boolean manualPimp;
    private boolean pimpWheelAdjust;
    private boolean boxSide;

    private int pimpWheelZero;
    private int pimpWheelTarget;
    private int brushTime;

    private final int PIMP_INTERVAL = 520;

    public RobotTeleOp ()
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
        manualRangle = false;
        boxSide = RIGHT;
        manualPimp = false;
        pimpWheelAdjust = false;

        pimpWheelZero = 0;
        pimpWheelTarget = 0;
    }
    @Override
    public void loop()
    {

        // Change Modes:
        if (gamepad2.dpad_down)
            manualRangle = true;
        else if (gamepad2.dpad_up)
            manualRangle = false;

        if (gamepad2.dpad_right)
            boxSide = RIGHT;
        else if (gamepad2.dpad_left)
            boxSide = LEFT;

        if (gamepad1.x)
            manualPimp = false;
        else if (gamepad1.y)
            manualPimp = true;

        if (gamepad1.dpad_right)
            pimpWheelAdjust = true;
        else if (gamepad1.dpad_left)
            pimpWheelAdjust = false;

        if (manualRangle)
        {
            // Use Joysticks to control Rangles independently
            setNonEncodedMotorPower("rightRangle", -scaleInput(gamepad1.right_stick_y));
            setNonEncodedMotorPower("leftRangle", -scaleInput(gamepad1.left_stick_y));
        }
        else
        {
            // Use Joysticks to control the spools
            setNonEncodedMotorPower("rightSpool", scaleInput(gamepad2.right_stick_y));
            setNonEncodedMotorPower("leftSpool" , scaleInput(gamepad2.left_stick_y));

            // Control Rangles with buttons instead
            // Rangle Control
            if (gamepad2.a)
            {
                setNonEncodedMotorPower("rightRangle", 0.4);
                setNonEncodedMotorPower("leftRangle", 0.4);
            }
            else if (gamepad2.b)
            {
                setNonEncodedMotorPower("rightRangle", -0.4);
                setNonEncodedMotorPower("leftRangle", -0.4);
            }
            else
            {
                setNonEncodedMotorPower("rightRangle", 0);
                setNonEncodedMotorPower("leftRangle", 0);
            }
        }

        // Drive Control
        double[] drivePower = getDrivePower(-scaleInput(gamepad1.left_stick_x), -scaleInput(gamepad1.left_stick_y));
        setNonEncodedMotorPower("leftDrive", drivePower[0]);
        setNonEncodedMotorPower("rightDrive", drivePower[1]);

        // Pimp Wheel Control
        if (pimpWheelAdjust)
        {
            if (gamepad1.b)
            {
                pimpWheelZero += degToEncoder(1);
                pimpWheelTarget = pimpWheelZero;
            }
            else if (gamepad1.a)
            {
                pimpWheelZero -= degToEncoder(1);
                pimpWheelTarget = pimpWheelZero;
            }
        }
        else
        {
            if (manualPimp)
            {
                pimpWheelTarget = pimpWheelZero + degToEncoder(PIMP_INTERVAL);
            }
            else
            {
                if (Math.abs(drivePower[0] - drivePower[1]) > 0.5)
                    pimpWheelTarget = pimpWheelZero + degToEncoder(PIMP_INTERVAL);
                else
                {
                    pimpWheelTarget = pimpWheelZero;
                }
            }
        }

        setEncodedMotorPos("pimpWheel", pimpWheelTarget);
        
        // Brush Control
        if (brushTime > 0)
        {
            setNonEncodedMotorPower("brush", -0.3);
            brushTime--;
        }
        else
            setNonEncodedMotorPower("brush", 0);

        if (gamepad1.right_trigger > 0)
            setNonEncodedMotorPower("brush", gamepad1.right_trigger);
        else if (gamepad1.right_bumper)
            setNonEncodedMotorPower("brush", -0.3);
        
        /*
         *  Servos
         */
        if (boxSide == RIGHT)
        {
            if (gamepad2.x)
            {
                setServoVal("rightBox", R_BOX_DOWN);
            }
            else if (gamepad2.y)
            {
                setServoVal("rightBox", R_BOX_UP);
            }
        }
        else if (boxSide == LEFT)
        {
            if (gamepad2.x)
            {
                setServoVal("leftBox", L_BOX_DOWN);
            }
            else if (gamepad2.y)
            {
                setServoVal("leftBox", L_BOX_UP);
            }
        }

        // Claws
        if (gamepad1.left_bumper) // up
        {
            setServoVal("leftClaw", L_CLAW_UP);
            setServoVal("rightClaw", R_CLAW_UP);
        }
        else if (gamepad1.left_trigger > 0.1) // down
        {
            setServoVal("leftClaw", L_CLAW_DOWN);
            setServoVal("rightClaw", R_CLAW_DOWN);
        }

        // Brush Arm
        if (gamepad1.dpad_up) // up
        {
            setServoVal("leftBrushArm", L_BRUSH_BAR_UP);
            setServoVal("rightBrushArm", R_BRUSH_BAR_UP);
            brushTime = 3;
        }
        else if (gamepad1.right_stick_button)
        {
            setServoVal("leftBrushArm", L_BRUSH_BAR_MIDDLE);
            setServoVal("rightBrushArm", R_BRUSH_BAR_MIDDLE);
        }
        else if (gamepad1.dpad_down) // down
        {
            setServoVal("leftBrushArm", L_BRUSH_BAR_DOWN);
            setServoVal("rightBrushArm", R_BRUSH_BAR_DOWN);
        }

        // Telemetry
        addTelemetry("Manual Rangle Control", String.valueOf(manualRangle));

        if (boxSide == RIGHT)
            addTelemetry("Servo Box Side", "RIGHT");
        else
            addTelemetry("Servo Box Side", "LEFT");

        updateTelemetry();

        // Call loop in Robot Hardware
        super.loop();
    }
}
