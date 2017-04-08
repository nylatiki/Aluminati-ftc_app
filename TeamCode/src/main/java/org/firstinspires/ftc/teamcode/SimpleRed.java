package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Created by benlimpa on 2/24/17.
 */
@Autonomous(name="Simple Red Auto", group="CompBot")
public class SimpleRed extends SimpleAuto
{
    @Override
    protected void irMove()
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
    protected void irMoveAlt()
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
    protected void changeHeading(double heading)
    {
        targetHeading = floorMod(targetHeading - heading, 360);
    }

    @Override
    protected void pushButton()
    {
        switch (buttonInstructionState)
        {
            case DECIDE_COLOR:
                if (hardware.colorSensor.blue() < 1)
                {
                    hardware.lForkHolder.setPosition(CompBotHardware.L_FORK_RELEASE);
                    instructionState++; // Skip Button Press
                }
                else if (hardware.colorSensor.red() < hardware.colorSensor.blue())
                {
                    //driveTrain.setTargets(3.5, -3.5, -3.5, 3.5);
                    //masterState = MasterState.ENCODER_MOVE;
                    buttonInstructionState = PushButtonState.MOVE_FORWARD;
                    needToMoveBack = false;
                }
                else
                {
                    //driveTrain.setTargets(3.5, -3.5, -3.5, 3.5);
                    //masterState = MasterState.ENCODER_MOVE;
                    buttonInstructionState = PushButtonState.MOVE_SIDE;
                    needToMoveBack = true;
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

                if (needToMoveBack)
                    buttonInstructionState = PushButtonState.MOVE_TO_SHOOT;
                else
                    instructionState++; // Finish
                break;
            case MOVE_TO_SHOOT:
                driveTrain.setTargets(6, -6, -6, 6);
                masterState = MasterState.ENCODER_MOVE;
                instructionState++; // Finish
                break;
        }
    }

    @Override
    protected void irPrime()
    {
        driveTrain.setTargets(10, -10, -10, 10);
        masterState = MasterState.ENCODER_MOVE;
        instructionState++;
    }

    @Override
    protected void turnToShoot()
    {
        changeHeading(matchSpecificVars.getRedTurnAngle());

        masterState = MasterState.GYRO_TURN;
        instructionState++;
    }
}
