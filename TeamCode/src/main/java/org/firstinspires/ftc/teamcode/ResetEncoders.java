package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
/**
 * Created by benlimpa on 11/25/16.
 */

@Autonomous(name="Reset Encoders", group="CompBot")
public class ResetEncoders extends LinearOpMode {

    /* Declare OpMode members. */
    private CompBotHardware hardware;

    @Override
    public void runOpMode() throws InterruptedException {

        hardware = new CompBotHardware();
        hardware.init(hardwareMap);

        // Send telemetry message to signify hardware waiting;
        telemetry.addData("Status", "Resetting Encoders");    //
        telemetry.update();
        hardware.flwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.frwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.blwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.brwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        idle();
        hardware.flwheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        hardware.frwheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        hardware.blwheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        hardware.brwheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        hardware.flwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.frwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.blwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.brwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        telemetry.addData("Status", "Complete");
        telemetry.update();
    }
}

