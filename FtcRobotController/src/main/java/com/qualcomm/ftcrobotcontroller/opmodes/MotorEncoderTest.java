package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

/**
 * Created by BenL on 10/16/15.
 */
public class MotorEncoderTest extends OpMode
{
    private final int ENCODER_THRESHOLD = 10;

    private DcMotorController.DeviceMode    devMode;
    private DcMotorController               motorControl;
    private DcMotor                         testMotor;
    private int                             targetPos;
    private boolean                         setPosDone;
    private int state;

    @Override
    public void init()
    {
        targetPos = 1440;
        setPosDone = false;
        state = 1;
        motorControl    = hardwareMap.dcMotorController.get("motorControl");
        testMotor       = hardwareMap.dcMotor.get("testMotor");

        testMotor.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
        //motorState = MotorState.STOPPED;
        //targetMotorPos = 200;

        //loops = 0;
        //haveNotRun = true;
    }

    @Override
    public void start()
    {
        motorControl.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_WRITE);
        testMotor.setPower(0.5);
        testMotor.setMode(DcMotorController.RunMode.RESET_ENCODERS);
    }

    @Override
    public void loop()
    {
        switch (state)
        {
            case 2:
                if (testMotor.getMode() == DcMotorController.RunMode.RESET_ENCODERS
                        && testMotor.getCurrentPosition() == 0)
                {
                    testMotor.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
                    state++;
                }
                break;
            case 1:
                testMotor.setTargetPosition(targetPos);
                state++;
                break;
            case 3:
                if (testMotor.getCurrentPosition() < testMotor.getTargetPosition()+ENCODER_THRESHOLD
                        && testMotor.getCurrentPosition() > testMotor.getTargetPosition()-ENCODER_THRESHOLD)
                    state++;
                break;
        }
        telemetry.addData("Motor Mode: ", testMotor.getMode());
        telemetry.addData("Motor State: ", state);

        /*
         *  OLD ALGORITHM
         */

        /*
        if (devMode == DcMotorController.DeviceMode.WRITE_ONLY) // Allowed to write
        {
            switch (motorState)
            {
                case DECREASING:
                    telemetry.addData("Motor State: ", "DECREASING");
                    testMotor.setPower(-0.1);
                    break;
                case INCREASING:
                    telemetry.addData("Motor State: ", "INCREASING");
                    testMotor.setPower(0.1);
                    break;
                case STOPPED:
                    telemetry.addData("Motor State: ", "STOPPED");
                    testMotor.setPower(0);
                    break;
            }
        }

        if (devMode == DcMotorController.DeviceMode.READ_ONLY) // Allowed to read
        {
            int currentPos = testMotor.getCurrentPosition();
            telemetry.addData("Encoder Position: ", currentPos);

            if (currentPos > (targetMotorPos + 3))
            {
                motorState = MotorState.DECREASING;
            }
            else if (currentPos < (targetMotorPos - 3))
            {
                motorState = MotorState.INCREASING;
            }
            else if (currentPos > (targetMotorPos - 3) && currentPos < (targetMotorPos + 3))
            {
                motorState = MotorState.STOPPED;
            }
            else
            {
                // throw exception
            }

            motorControl.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
        }

        if (loops % 10 == 0 && devMode == DcMotorController.DeviceMode.WRITE_ONLY)
        {
            motorControl.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_ONLY);
        }

        switch (devMode)
        {
            case WRITE_ONLY:
                telemetry.addData("Device Mode", "WRITE_ONLY");
                break;
            case READ_ONLY:
                telemetry.addData("Device Mode", "READ_ONLY");
                break;
            case SWITCHING_TO_READ_MODE:
                telemetry.addData("Device Mode", "SWITCHING_TO_READ_MODE");
                break;
            case SWITCHING_TO_WRITE_MODE:
                telemetry.addData("Device Mode", "SWITCHING_TO_WRITE_MODE");
                break;
        }
        devMode = motorControl.getMotorControllerDeviceMode();
        loops++;
        */
    }
}