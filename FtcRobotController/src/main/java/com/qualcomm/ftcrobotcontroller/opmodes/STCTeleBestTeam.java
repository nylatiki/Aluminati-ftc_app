package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

/**
 * Created by benlimpa on 9/6/16.
 */
public class STCTeleBestTeam extends OpMode
{

    private enum STCMode {NORMAL_DRIVE, DRAWING}

    DcMotor motorBL;
    DcMotor motorBR;
    DcMotor motorFL;
    Servo markerServo;
    DcMotor motorFR;
    DcMotor winchMotor;
    STCMode mode;
    DcMotorController motorController1;
    DcMotorController motorController2;
    DcMotorController motorController3;
    ServoController drawingServoController;


    public void init()
    {
        mode = STCMode.NORMAL_DRIVE;

        motorBL = hardwareMap.dcMotor.get("motorBL");
        motorBR = hardwareMap.dcMotor.get("motorBR");
        motorFL = hardwareMap.dcMotor.get("motorFL");
        motorFR = hardwareMap.dcMotor.get("motorFR");

        motorController1 = hardwareMap.dcMotorController.get("motorController1");
        motorController2 = hardwareMap.dcMotorController.get("motorController2");
        motorController3 = hardwareMap.dcMotorController.get("motorController3");

        motorController1.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_WRITE);
        motorController2.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_WRITE);
        motorController3.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_WRITE);

        //motorBL.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        //motorBR.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        //motorFL.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        //motorFR.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        motorBR.setDirection(DcMotor.Direction.REVERSE);
        motorFR.setDirection(DcMotor.Direction.REVERSE);

        markerServo = hardwareMap.servo.get("markerServo");
        winchMotor = hardwareMap.dcMotor.get("winchMotor");

        drawingServoController = hardwareMap.servoController.get("drawingServoController");
    }
    public void start()
    {
        
    }
    public void loop()
    {
        if (gamepad1.dpad_up)
            mode = STCMode.NORMAL_DRIVE;
        else if (gamepad1.dpad_down)
            mode = STCMode.DRAWING;
        switch (mode)
        {
            case NORMAL_DRIVE:
                double rightPower = -gamepad1.left_stick_y-gamepad1.left_stick_x;
                double leftPower = gamepad1.left_stick_x-gamepad1.left_stick_y;
                motorBL.setPower(capVal(leftPower));
                motorFL.setPower(capVal(leftPower));
                motorBR.setPower(capVal(rightPower));
                motorFR.setPower(capVal(rightPower));
                winchMotor.setPower(gamepad1.right_stick_y);
                break;
            case DRAWING:
                double scaledVal = 0.3 * -gamepad1.left_stick_x;
                motorBL.setPower(scaledVal);
                motorFL.setPower(scaledVal);
                motorBR.setPower(scaledVal);
                motorFR.setPower(scaledVal);
                winchMotor.setPower(-gamepad1.left_stick_y);
                break;
        }
        markerServo.setPosition(.1);
        if(gamepad1.x){
            markerServo.setPosition(0);
            drawingServoController.pwmDisable();
        }
        telemetry.addData("Mode: ", mode);
        telemetry.addData("Motor Value: ", .3 * -gamepad1.left_stick_x);
        telemetry.addData("RealMotorBL: ", motorBL.getPower());                  //??? SOMEHOW MANAGES TO SLOW DOWN THE MOTOR
        telemetry.addData("RealMotorFL: ", motorFL.getPower());
        telemetry.addData("RealMotorBR: ", motorBR.getPower());
        telemetry.addData("RealMotorFR: ", motorFR.getPower());
    }
    public void stop()
    {
        
    }
    
    private double capVal(double val)
    {
        if (val > 1)
        {
            return 1;
        }
        if (val < -1)
        {
            return -1;
        }
        return val;
    }
}