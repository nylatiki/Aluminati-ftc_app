package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Created by benlimpa on 2/25/17.
 */

@Autonomous(name="Simple Blue Auto", group="CompBot")
public class SimpleBlue extends SimpleAuto
{
    @Override
    protected void pushButton()
    {
        switch (buttonInstructionState)
        {
            case DECIDE_COLOR:
                if (hardware.colorSensor.blue() < 1)
                {
                    hardware.itsOver.setPosition(CompBotHardware.ITS_OVER);
                    instructionState++; // Skip Button Press
                } else if (hardware.colorSensor.red() > hardware.colorSensor.blue())
                {
                    //driveTrain.setTargets(3.5, -3.5, -3.5, 3.5);
                    //masterState = MasterState.ENCODER_MOVE;
                    buttonInstructionState = PushButtonState.MOVE_FORWARD;
                } else
                {
                    //driveTrain.setTargets(3.5, -3.5, -3.5, 3.5);
                    //masterState = MasterState.ENCODER_MOVE;
                    buttonInstructionState = PushButtonState.MOVE_SIDE;
                }
                break;
            case MOVE_SIDE:
                driveTrain.setTargets(-6, 6, 6, -6);
                masterState = MasterState.ENCODER_MOVE;
                buttonInstructionState = PushButtonState.MOVE_FORWARD;
                break;
            case MOVE_FORWARD: // Push Button
                driveTrain.setPowerMultiplier(0.3);
                hardware.buttonPusher.setPosition(CompBotHardware.BUTTON_PUSHER_DOWN);
                driveTrain.setTargets(PUSH_DISTANCE, PUSH_DISTANCE, PUSH_DISTANCE, PUSH_DISTANCE);
                masterState = MasterState.ENCODER_MOVE;
                buttonInstructionState = PushButtonState.MOVE_BACK;
                break;
            case MOVE_BACK: // Push Button
                driveTrain.setPowerMultiplier(1);
                driveTrain.setTargets(-PUSH_DISTANCE, -PUSH_DISTANCE, -PUSH_DISTANCE, -PUSH_DISTANCE);
                masterState = MasterState.ENCODER_MOVE;
                instructionState++; // Finish
                break;
        }
    }

    @Override
    protected void irPrime()
    {
        driveTrain.setTargets(-10, 10, 10, -10);
        masterState = MasterState.ENCODER_MOVE;
        instructionState++;
    }

    @Override
    protected void irMove()
    {
        if (hardware.centerIR.getState())
        {
            // Move Right
            hardware.flwheel.setPower(IR_DRIVE_SPEED);
            hardware.blwheel.setPower(-IR_DRIVE_SPEED);
            hardware.frwheel.setPower(-IR_DRIVE_SPEED);
            hardware.brwheel.setPower(IR_DRIVE_SPEED);
        }
        else
        {
            driveTrain.stopAll();
            masterState = MasterState.NEW_INSTRUCTION;
        }
    }

    @Override
    protected void irMoveAlt()
    {
        if (hardware.centerIR.getState())
        {
            // Move Right
            hardware.flwheel.setPower(-IR_DRIVE_SPEED);
            hardware.blwheel.setPower(IR_DRIVE_SPEED);
            hardware.frwheel.setPower(IR_DRIVE_SPEED);
            hardware.brwheel.setPower(-IR_DRIVE_SPEED);
        }
        else
        {
            driveTrain.stopAll();
            masterState = MasterState.NEW_INSTRUCTION;
        }
    }

    @Override
    protected void changeHeading(double heading)
    {
        targetHeading = floorMod(targetHeading + heading, 360);
    }

    @Override
    protected void turnToShoot()
    {
        changeHeading(matchSpecificVars.getBlueTurnAngle());

        masterState = MasterState.GYRO_TURN;
        instructionState++;
    }
}
