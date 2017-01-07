package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
/**
 * Created by benlimpa on 11/25/16.
 */

@Autonomous(name="Score Center & Hit CapBall", group="CompBot")
//@Disabled
public class CompBotAuto_ScoreOnly extends LinearOpMode {
    
    private CompBotHardware hardware;

    static final double     COUNTS_PER_MOTOR_REV    = 1440 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 5 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * Math.PI);
    static final double     DRIVE_SPEED             = 0.3;
    @Override
    public void runOpMode() throws InterruptedException {

        hardware = new CompBotHardware();
        hardware.init(hardwareMap);

        // Send telemetry message to signify hardware waiting;
        telemetry.addData("Status", "Resetting Encoders");    //
        telemetry.update();

        if (hardware.flwheel == null)
            telemetry.addData("hardware", "null!");
        else
            telemetry.addData("hardware", "good!");
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

        hardware.flwheel.setPower(DRIVE_SPEED);
        hardware.frwheel.setPower(DRIVE_SPEED);
        hardware.blwheel.setPower(DRIVE_SPEED);
        hardware.brwheel.setPower(DRIVE_SPEED);

        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        sleep(500);

        // Move Forward
        hardware.flwheel.setTargetPosition(convertToCount(27));
        hardware.frwheel.setTargetPosition(convertToCount(27));
        hardware.blwheel.setTargetPosition(convertToCount(27));
        hardware.brwheel.setTargetPosition(convertToCount(27));
        waitUntilDone();

        // Shoot 2 Balls
        hardware.shooter.setPower(-1);
        sleep(2000);
        hardware.shooter.setPower(0);
        hardware.intake.setPower(1);
        sleep(1000);
        hardware.intake.setPower(0);
        hardware.shooter.setPower(-1);
        sleep(2000);
        hardware.shooter.setPower(0);
        
        // Hit the Cap Ball and Stop on the center platform
        hardware.flwheel.setTargetPosition(hardware.flwheel.getCurrentPosition()
                + convertToCount(24));
        hardware.frwheel.setTargetPosition(hardware.flwheel.getCurrentPosition()
                + convertToCount(24));
        hardware.blwheel.setTargetPosition(hardware.flwheel.getCurrentPosition()
                + convertToCount(24));
        hardware.brwheel.setTargetPosition(hardware.flwheel.getCurrentPosition()
                + convertToCount(24));
        telemetry.addData("Progress", 1);
        waitUntilDone();

        hardware.flwheel.setPower(0);
        hardware.frwheel.setPower(0);
        hardware.blwheel.setPower(0);
        hardware.brwheel.setPower(0);
        
        telemetry.addData("Path", "Complete");
        telemetry.update();
    }

    private void waitUntilDone() throws InterruptedException
    {
        while ( opModeIsActive() &&
                (hardware.flwheel.isBusy() && hardware.frwheel.isBusy() &&
                        hardware.blwheel.isBusy() && hardware.brwheel.isBusy()))
        {
            telemetry.addData("Target", "%7d:%7d:%7d:%7d",
                    hardware.flwheel.getTargetPosition(),
                    hardware.frwheel.getTargetPosition(),
                    hardware.blwheel.getTargetPosition(),
                    hardware.brwheel.getTargetPosition());
            telemetry.addData("Current", "%7d:%7d:%7d:%7d",
                    hardware.flwheel.getCurrentPosition(),
                    hardware.frwheel.getCurrentPosition(),
                    hardware.blwheel.getCurrentPosition(),
                    hardware.brwheel.getCurrentPosition());
            telemetry.update();
            idle();
        }
    }

    /**
     * Converts the distance in inches to encoder ticks according to the wheel diameter and 
     * @param inches the distance in inches
     * @return the distance in encoder ticks
     */
    private int convertToCount(double inches)
    {
        return (int) (inches * COUNTS_PER_INCH);
    }
}

