package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

/**
 * Created by BenL on 10/7/15.
 */
public class MainTeleOp extends OpMode
{
    // Controllers
    DcMotorController               motorControl1;
    DcMotorController               motorControl2;
    DcMotorController               motorControl3;
    DcMotorController               motorControl4;

    ServoController                 servoControl;

    // Motors
    DcMotor     leftDrive;
    DcMotor     rightDrive;

    DcMotor     pimpWheel;

    DcMotor     leftSpool;
    DcMotor     rightSpool;

    DcMotor     leftRangle;
    DcMotor     rightRangle;

    DcMotor     brush;

    // Servos
    Servo       leftBoxServo;
    Servo       rightBoxServo;

    Servo       leftClawServo;
    Servo       rightClawServo;

    Servo       leftBrushArmServo;
    Servo       rightBrushArmServo;

    double      leftBoxServoPos;
    double      rightBoxServoPos;

    double      leftClawServoPos;
    double      rightClawServoPos;

    double      leftBrushArmServoPos;
    double      rightBrushArmServoPos;

    int brushTime;
    int pimpWheelZero; // Default PimpWheel Position
    int pimpWheelTarget;

    // States
    boolean     manualMode;
    boolean     manualPimp;
    boolean     pimpWheelAdjust;
    boolean     fieldOrientation;

    // Constants
    final boolean RIGHT = true;
    final boolean LEFT = false;

    final int PIMP_INTERVAL = 525;

    final double L_BOX_UP = 0;
    final double L_BOX_DOWN = 0.6;
    final double R_BOX_UP = 0.6;
    final double R_BOX_DOWN = 0;

    final double L_CLAW_UP = 0.1;
    final double L_CLAW_DOWN = 0.75;
    final double R_CLAW_UP = 1;
    final double R_CLAW_DOWN = 0.30;

    final double L_BRUSH_BAR_UP = 0.25;
    final double L_BRUSH_BAR_MIDDLE = 0.625;
    final double L_BRUSH_BAR_DOWN = 1.0;
    final double R_BRUSH_BAR_UP = 0.50;
    final double R_BRUSH_BAR_MIDDLE = 0.325;
    final double R_BRUSH_BAR_DOWN = 0.15;

    @Override
    public void init()
    {
        // States
        pimpWheelAdjust = false;
        manualMode = false;
        fieldOrientation = RIGHT;
        pimpWheelTarget = 0;
        pimpWheelZero = 0;
        brushTime = 0;

        // Controllers
        motorControl1 = hardwareMap.dcMotorController.get("MotorControl1");
        motorControl2 = hardwareMap.dcMotorController.get("MotorControl2");
        motorControl3 = hardwareMap.dcMotorController.get("MotorControl3");
        motorControl4 = hardwareMap.dcMotorController.get("MotorControl4");

        servoControl = hardwareMap.servoController.get("ServoControl");

        motorControl1.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
        motorControl2.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
        motorControl3.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
        motorControl4.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);

        // Motors
        rightSpool = hardwareMap.dcMotor.get("rightSpool");
        leftSpool = hardwareMap.dcMotor.get("leftSpool");

        leftRangle = hardwareMap.dcMotor.get("leftRangle");
        rightRangle = hardwareMap.dcMotor.get("rightRangle");
        rightRangle.setDirection(DcMotor.Direction.REVERSE);

        leftDrive = hardwareMap.dcMotor.get("leftDrive");
        rightDrive = hardwareMap.dcMotor.get("rightDrive");
        rightDrive.setDirection(DcMotor.Direction.REVERSE);

        pimpWheel = hardwareMap.dcMotor.get("pimpWheel");
        pimpWheel.setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
        pimpWheel.setPower(1);

        brush = hardwareMap.dcMotor.get("brush");

        // Servos

        leftBoxServo = hardwareMap.servo.get("leftBoxServo");
        rightBoxServo = hardwareMap.servo.get("rightBoxServo");

        leftBrushArmServo = hardwareMap.servo.get("leftBrushArmServo");
        rightBrushArmServo = hardwareMap.servo.get("rightBrushArmServo");

        leftClawServo = hardwareMap.servo.get("leftClawServo");
        rightClawServo = hardwareMap.servo.get("rightClawServo");
    }

    @Override
    public void start()
    {

        // Set Servos to default positions
        leftBoxServoPos = L_BOX_UP;
        rightBoxServoPos = R_BOX_UP;

        leftClawServoPos = L_CLAW_UP;
        rightClawServoPos = R_CLAW_UP;

        leftBrushArmServoPos = L_BRUSH_BAR_DOWN;
        rightBrushArmServoPos = R_BRUSH_BAR_DOWN;
    }

    @Override
    public void loop()
    {


        // Change Modes:
        if (gamepad2.dpad_down)
        {
            manualMode = true;
        }
        else if (gamepad2.dpad_up)
        {
            manualMode = false;
        }

        if (gamepad2.dpad_right)
            fieldOrientation = RIGHT;
        else if (gamepad2.dpad_left)
            fieldOrientation = LEFT;

        if (fieldOrientation == RIGHT)
            telemetry.addData("Field Orientation: ", "right");
        else if (fieldOrientation == LEFT)
            telemetry.addData("Field Orientation: ", "left");

        if (gamepad1.x)
            manualPimp = false;
        else if (gamepad1.y)
            manualPimp = true;

        if (gamepad1.dpad_right)
            pimpWheelAdjust = true;
        else if (gamepad1.dpad_left)
            pimpWheelAdjust = false;

        telemetry.addData("Manual Pimp Wheel Adjust: ", pimpWheelAdjust);
        telemetry.addData("Pimp Wheel Zero: ", pimpWheelZero);

        // Switch Between controlling the rangles and the spools
        if (!manualMode)
        {
            // Rangle Control
            if (gamepad2.a)
            {
                rightRangle.setPower(-0.4);
                leftRangle.setPower(-0.4);
            }
            else if (gamepad2.b)
            {
                rightRangle.setPower(0.4);
                leftRangle.setPower(0.4);
            }
            else
            {
                rightRangle.setPower(0);
                leftRangle.setPower(0);
            }

            // Spool Control
            rightSpool.setPower(scaleInput(gamepad2.right_stick_y));
            leftSpool.setPower(scaleInput(gamepad2.left_stick_y)*2/5);

        }
        else
        {
            rightRangle.setPower(-scaleInput(gamepad2.right_stick_y));
            leftRangle.setPower(-scaleInput(gamepad2.left_stick_y));
        }

        // Brush Control
        if (brushTime > 0)
        {
            brush.setPower(-0.3);
            brushTime--;
        }
        else
            brush.setPower(0);

        if (gamepad1.right_trigger > 0)
            brush.setPower(gamepad1.right_trigger);
        else if (gamepad1.right_bumper)
            brush.setPower(-0.3);
        else
            brush.setPower(0);

        // Drive Control
        double[] drivePower = getDrivePower(-scaleInput(gamepad1.left_stick_x), -scaleInput(gamepad1.left_stick_y));

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

        leftDrive.setPower(drivePower[0]);
        rightDrive.setPower(drivePower[1]);

        telemetry.addData("Manual Rangle Control: ", manualMode);

        /*
         *  Servos
         */
        if (fieldOrientation == RIGHT)
        {
            if (gamepad2.x)
            {
                rightBoxServoPos = R_BOX_DOWN;
                telemetry.addData("Right Box Servo: ", R_BOX_DOWN);
            }
            else if (gamepad2.y)
            {
                rightBoxServoPos = R_BOX_UP;
                telemetry.addData("Right Box Servo: ", R_BOX_UP);
            }
        }
        else if (fieldOrientation == LEFT)
        {
            if (gamepad2.x)
            {
                leftBoxServoPos = L_BOX_DOWN;
                telemetry.addData("Left Box Servo: ", L_BOX_DOWN);
            }
            else if (gamepad2.y)
            {
                leftBoxServoPos = L_BOX_UP;
                telemetry.addData("Left Box Servo: ", L_BOX_UP);
            }
        }

        // Claws
        if (gamepad1.left_bumper) // up
        {
            leftClawServoPos = L_CLAW_UP;
            rightClawServoPos = R_CLAW_UP;
        }
        else if (gamepad1.left_trigger > 0.1) // down
        {
            leftClawServoPos = L_CLAW_DOWN;
            rightClawServoPos = R_CLAW_DOWN;
        }

        // Brush Arm
        if (gamepad1.dpad_up) // up
        {
            leftBrushArmServoPos = L_BRUSH_BAR_UP;
            rightBrushArmServoPos = R_BRUSH_BAR_UP;
            brushTime = 9;
        }
        else if (gamepad1.right_stick_button)
        {
            leftBrushArmServoPos = L_BRUSH_BAR_MIDDLE;
            rightBrushArmServoPos = R_BRUSH_BAR_MIDDLE;
        }
        else if (gamepad1.dpad_down) // down
        {
            leftBrushArmServoPos = L_BRUSH_BAR_DOWN;
            rightBrushArmServoPos = R_BRUSH_BAR_DOWN;
        }

        // Update Encoded Motor Positions
        pimpWheel.setTargetPosition(pimpWheelTarget);

        // Update Servo Positions (servos must be sent a position, otherwise they will move to default)
        leftBoxServo.setPosition(leftBoxServoPos);
        rightBoxServo.setPosition(rightBoxServoPos);

        leftClawServo.setPosition(leftClawServoPos);
        rightClawServo.setPosition(rightClawServoPos);

        leftBrushArmServo.setPosition(leftBrushArmServoPos);
        rightBrushArmServo.setPosition(rightBrushArmServoPos);
    }

    @Override
    public void stop()
    {
        // nothing to stop
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
