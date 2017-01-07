package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

/**
 * Created by benlimpa on 11/25/16.
 */

@Autonomous(name="Auto", group="CompBot")
@Disabled
public class CompBotAuto extends LinearOpMode {

    static final double     COUNTS_PER_MOTOR_REV    = 1440 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 5 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * Math.PI);
    static final double     DRIVE_SPEED             = 0.6;
    static final double     TURN_SPEED              = 0.5;

    public enum FieldColor {BLUE, RED}

    BNO055IMU imu;

    private CompBotHardware hardware;
    private MatchSpecificVars matchVals;

    private double integral;

    private FieldColor color;
    private int colorConst; // Blue = 1, Red = -1

    private ElapsedTime runtime = new ElapsedTime();



    protected void initVariation(FieldColor color)
    {
        this.color = color;

        switch (color)
        {
            case BLUE:
                colorConst = 1;
                break;
            case RED:
                colorConst = -1;
                break;
        }
    }

    @Override
    public void runOpMode() throws InterruptedException {

        telemetry.addData("Status", "Begin Initialization");
        telemetry.update();
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "AdafruitIMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        integral = 0;

        matchVals = new MatchSpecificVars("compBotAutoVars.txt");

        hardware = new CompBotHardware();
        hardware.init(hardwareMap);

        telemetry.addData("Status", "Resetting Encoders");
        telemetry.update();

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

        hardware.flwheel.setPower(0.4);
        hardware.frwheel.setPower(0.4);
        hardware.blwheel.setPower(0.4);
        hardware.brwheel.setPower(0.4);

        telemetry.update();

        /*
         *  Start
         */
        waitForStart();

        imu.startAccelerationIntegration(new Position(DistanceUnit.CM, 0, 0, 0, 0), new Velocity(), 5);

        hardware.buttonPusher.setPosition(0);

        // Wait for partner
        telemetry.addData("Waiting for Partner (sec)", matchVals.getStartWait() / 1000d);
        telemetry.update();
        sleep(matchVals.getStartWait());

        // Move to start
        setNewTarget(matchVals.getStartOffset(), -matchVals.getStartOffset(),
                -matchVals.getStartOffset(), matchVals.getStartOffset(), true);
        waitUntilDone();

        // Forward to shoot
        setNewTarget(24, 24, 24, 24, false);
        waitUntilDone();

        // Shoot
        hardware.shooter.setPower(-1);
        sleep(2000);
        hardware.shooter.setPower(0);
        hardware.intake.setPower(1);
        sleep(1500);
        hardware.intake.setPower(0);
        hardware.shooter.setPower(-1);
        sleep(2000);
        hardware.shooter.setPower(0);

        // Turn to face beacon
        setNewTarget(23, -23, 23, -23, true);
        telemetry.addData("Progress", "Turn to beacon");
        telemetry.update();
        waitUntilDone();

        // Move closer to beacon
        setNewTarget(36, 36, 36, 36, false);
        telemetry.addData("Progress", "");
        telemetry.update();
        waitUntilDone();

        // Move left to align with beacon
        hardware.flwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.frwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.blwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.brwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        idle();

        hardware.flwheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hardware.frwheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hardware.blwheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hardware.brwheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        while (hardware.centerIR.getState())
        {
            hardware.flwheel.setPower(-0.3);
            hardware.frwheel.setPower(0.3);
            hardware.blwheel.setPower(0.3);
            hardware.brwheel.setPower(-0.3);
            idle();
        }

        hardware.flwheel.setPower(0);
        hardware.frwheel.setPower(0);
        hardware.blwheel.setPower(0);
        hardware.brwheel.setPower(0);

        hardware.flwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.frwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.blwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.brwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        idle();

        hardware.flwheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        hardware.frwheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        hardware.blwheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        hardware.brwheel.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        idle();

        hardware.flwheel.setPower(0.4);
        hardware.frwheel.setPower(0.4);
        hardware.blwheel.setPower(0.4);
        hardware.brwheel.setPower(0.4);
        waitUntilDone();

        hardware.flwheel.setPower(0);
        hardware.frwheel.setPower(0);
        hardware.blwheel.setPower(0);
        hardware.brwheel.setPower(0);
        
        //drive(0.5, 12, 90, 10);

        telemetry.addData("Path", "Complete");
        telemetry.update();
    }

    /*

    private void autoTurn(double turnAmount)
    {
        double currentHeading = floorMod(imu.getAngularOrientation().firstAngle, 360);
        double targetHeading = currentHeading + turnAmount;

        while (true)
        {
            double diff = targetHeading - currentHeading;

            // Ensure that the difference is the short way (315-45)
            if (diff > 180)
                diff = diff - 360;
            else if (diff < -180)
            {
                diff += 360;
                diff = -diff;
            }

            telemetry.addData("Current Heading", currentHeading);
            telemetry.addData("Diff", diff);

            double prop = diff * P_CONST;
            integral += diff * I_CONST; // When it reaches the desired value, it may still have a non-zero value
            double deriv = (diff - old_diff) * D_CONST;

            r = -(prop + integral + deriv) / 180;

            // deadzone affect the motor speed directly so that the integral would not leave
            if (diff < TURN_DEADZONE && diff > -TURN_DEADZONE)
            {
                r = 0;
                integral = 0;
            }

            old_diff = diff;

            telemetry.addData("p", prop);
            telemetry.addData("i", integral);
            telemetry.addData("d", deriv);
            telemetry.addData("r", r);
            telemetry.addData("Position", imu.getPosition());
        }
    }
    */

    private double floorMod(double val, double mod)
    {
        // FloorMod for floating point numbers
        val %= mod;

        // only need to add once because the value cannot be < -mod (because of the val is only remainder)
        if (val < 0)
            val += mod;

        return val;
    }

    private void chooseColor() throws InterruptedException
    {

        if (checkColor())
        {

            // Move pusher down
            hardware.buttonPusher.setPosition(0.6);

            // Push button
            setNewTarget(3, 3, 3, 3, false);
            waitUntilDone();

            setNewTarget(-3, -3, -3, -3, false);
            waitUntilDone();

            // Reset pusher
            hardware.buttonPusher.setPosition(0);

            // Move to finish position
            setNewTarget(-8, 8, -8, 8, true);
            waitUntilDone();
        }
        else
        {
            setNewTarget(-8, 8, -8, 8, true);
        }
    }

    /**
     * Sets the new target position for the robot
     * @param flwheel distance in inches
     * @param frwheel distance in inches
     * @param blwheel distance in inches
     * @param brwheel distance in inches
     * @param color invert matchVals depending on team color
     */

    private void setNewTarget(double flwheel, double frwheel, double blwheel, double brwheel, boolean color)
    {
        if (color)
        {
            flwheel *= colorConst;
            frwheel *= colorConst;
            blwheel *= colorConst;
            brwheel *= colorConst;
        }

        hardware.flwheel.setTargetPosition(
                hardware.flwheel.getCurrentPosition() + convertToCount(flwheel));
        hardware.frwheel.setTargetPosition(
                hardware.frwheel.getCurrentPosition() + convertToCount(frwheel));
        hardware.blwheel.setTargetPosition(
                hardware.blwheel.getCurrentPosition() + convertToCount(blwheel));
        hardware.brwheel.setTargetPosition(
                hardware.brwheel.getCurrentPosition() + convertToCount(brwheel));
    }

    private void waitUntilDone() throws InterruptedException
    {

        // Must be && since not all motors will reach the target
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
            //idle();
        }
    }

    /**
     * Checks if the color sensor senses the same color as the field color.
     * @return true if the color matches the field (team) color
     */
    private boolean checkColor()
    {
        switch (color)
        {
            case BLUE:
                return hardware.colorSensor.blue() > hardware.colorSensor.red();
            case RED:
                return hardware.colorSensor.red() > hardware.colorSensor.blue();
        }
        return false;
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

    /**
     *
     * @param speed the speed multiplier [-1, 1]
     * @param inchDistance the number of inches to move
     * @param direction the direction of matchVars in degrees, 90 degrees is forward
     * @param timeout the time in seconds before aborting the drive command
     * @throws InterruptedException
     */
    private void drive(double speed, double inchDistance, double direction, double timeout) throws InterruptedException
    {
        if (opModeIsActive())
        {
            int[] startPoses = new int[] {
                    hardware.flwheel.getCurrentPosition(),
                    hardware.frwheel.getCurrentPosition(),
                    hardware.blwheel.getCurrentPosition(),
                    hardware.brwheel.getCurrentPosition()};

            /*
            for (DcMotor wheel : hardware.wheels)
            {
                wheel.setTargetPosition(wheel.getCurrentPosition()
                        + (int) (inchDistance * COUNTS_PER_INCH));

                wheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            */

            int targetCount = (int) (inchDistance * COUNTS_PER_INCH);

            // reset the timeout time and start motion.
            runtime.reset();

            // speed
            double x = speed * Math.cos(direction);
            double y = speed * Math.sin(direction);

            hardware.flwheel.setPower(x + y);
            hardware.frwheel.setPower(-x + y);
            hardware.blwheel.setPower(-x + y);
            hardware.brwheel.setPower(x + y);

            // keep looping while we are still active, and there is time left, and both motors are running.
            while (opModeIsActive() &&
                    (runtime.seconds() < timeout) ||
                    (hardware.flwheel.isBusy() || hardware.frwheel.isBusy() ||
                     hardware.blwheel.isBusy() || hardware.brwheel.isBusy())) {

                if (Math.abs(hardware.flwheel.getCurrentPosition() - startPoses[0]) >= targetCount)
                    hardware.flwheel.setPower(0);
                if (Math.abs(hardware.frwheel.getCurrentPosition() - startPoses[1]) >= targetCount)
                    hardware.flwheel.setPower(0);
                if (Math.abs(hardware.blwheel.getCurrentPosition() - startPoses[2]) >= targetCount)
                    hardware.flwheel.setPower(0);
                if (Math.abs(hardware.brwheel.getCurrentPosition() - startPoses[3]) >= targetCount)
                    hardware.flwheel.setPower(0);

                // Display it for the driver.
                telemetry.addData("Path1",  "Running to %7d:%7d:%7d:%7d",
                        hardware.flwheel.getTargetPosition(),
                        hardware.frwheel.getTargetPosition(),
                        hardware.blwheel.getTargetPosition(),
                        hardware.brwheel.getTargetPosition());
                telemetry.addData("Path2",  "Running at %7d:%7d:%7d:%7d",
                        hardware.flwheel.getCurrentPosition(),
                        hardware.frwheel.getCurrentPosition(),
                        hardware.blwheel.getCurrentPosition(),
                        hardware.brwheel.getCurrentPosition());
                telemetry.update();

                // Allow time for other processes to run.
                idle();
            }

            // Stop all motion;
            for (DcMotor wheel : hardware.wheels)
                wheel.setPower(0);
        }
    }
}

