package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

/**
 * Created by BenL on 1/9/16.
 */
public class LinearAutoRight extends LinearOpMode
{
    // Controllers
    DcMotorController motorControl1;
    DcMotorController               motorControl2;
    DcMotorController               motorControl3;
    DcMotorController               motorControl4;

    ServoController servoControl;

    // Motors
    DcMotor leftDrive;
    DcMotor     rightDrive;

    DcMotor     pimpWheel;

    DcMotor     leftSpool;
    DcMotor     rightSpool;

    DcMotor     leftRangle;
    DcMotor     rightRangle;

    DcMotor     brush;

    // Servos
    Servo leftBoxServo;
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
    public void runOpMode()
    {
        // Controllers
        motorControl1 = hardwareMap.dcMotorController.get("MotorControl1");
        motorControl2 = hardwareMap.dcMotorController.get("MotorControl2");
        motorControl3 = hardwareMap.dcMotorController.get("MotorControl3");
        motorControl4 = hardwareMap.dcMotorController.get("MotorControl4");

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

        // Move Servos to starting position
        // ...

        /*
        Beginning of Autonomous function
         */

        leftDrive.setPower(1);
        rightDrive.setPower(1);

        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        leftDrive.setPower(0);
        rightDrive.setPower(0);

        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        leftDrive.setPower(1);
        rightDrive.setPower(-1);

        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        leftDrive.setPower(0);
        rightDrive.setPower(0);

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        leftDrive.setPower(1);
        rightDrive.setPower(1);

        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        leftDrive.setPower(0);
        rightDrive.setPower(0);

        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        leftDrive.setPower(1);
        rightDrive.setPower(-1);
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
}
