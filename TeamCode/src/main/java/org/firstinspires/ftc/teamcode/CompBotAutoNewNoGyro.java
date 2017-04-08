package org.firstinspires.ftc.teamcode;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

/**
 * Created by benlimpa on 11/25/16.
 */

@Autonomous(name="New Comp Auto No Gyro", group="CompBot")
//@Disabled
public class CompBotAutoNewNoGyro extends LinearOpMode {

    /*
    Constants
     */

    public enum FieldColor {BLUE, RED}

    // Counts per inch calculation taken from the PushBot example class
    static final double     COUNTS_PER_MOTOR_REV    = 1440 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 5 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * Math.PI);
    static final double     DRIVE_SPEED             = 0.4;
    static final double     TURN_SPEED              = 0.3;
    static final double     PUSH_DISTANCE           = 10;

    /*
    Instance Variables
     */

    private CompBotHardware hardware;
    private MatchSpecificVars matchVars;
    private BNO055IMU imu;
    private FieldColor color;
    private int colorConst;
    private int ultraCheck;

    /*
    Methods
     */

    /**
     * Sets the team color
     * @param color the team color
     */
    protected void initVariation (FieldColor color)
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

    /**
     * Converts the distance in inches to encoder ticks according to the wheel diameter, drive train ratio, and ticks per revolution
     * @param inches the distance in inches
     * @return the distance in encoder ticks
     */
    private int convertToCount(double inches)
    {
        return (int) (inches * COUNTS_PER_INCH);
    }

    /**
     * Waits for the robot to finish moving to the target position !!!Does NOT work if the magnitude of the target position is different for each wheel!!!
     * @throws InterruptedException if the robot is stopped
     */
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
     * Checks if the color sensor senses the OPPOSITE of the team color since the color sensor is across from the button pusher
     * @return true if the color matches the field (team) color
     */
    private boolean checkColor()
    {
        switch (color)
        {
            case RED:
                return hardware.colorSensor.blue() > hardware.colorSensor.red();
            case BLUE:
                return hardware.colorSensor.red() > hardware.colorSensor.blue();
        }
        return false;
    }

    /**
     * Sets the target position for each wheel relative to the current position
     * @param flwheel target position of the front-left wheel
     * @param frwheel target position of the front-right wheel
     * @param blwheel target position of the back-left wheel
     * @param brwheel target position of the back-right wheel
     */
    private void setNewTarget(double flwheel, double frwheel, double blwheel, double brwheel)
    {
        hardware.flwheel.setTargetPosition(
                hardware.flwheel.getCurrentPosition() + convertToCount(flwheel));
        hardware.frwheel.setTargetPosition(
                hardware.frwheel.getCurrentPosition() + convertToCount(frwheel));
        hardware.blwheel.setTargetPosition(
                hardware.blwheel.getCurrentPosition() + convertToCount(blwheel));
        hardware.brwheel.setTargetPosition(
                hardware.brwheel.getCurrentPosition() + convertToCount(brwheel));
    }

    /**
     * Use the gyroscope to turn a specfic amount
     * @param turnAmount angle in degrees [-180,180] to turn (positive=counter-clockwise; negative=clockwise)
     * @param tolerance degree of acceptable error
     */
    private void gyroTurn(double turnAmount, double tolerance)
    {
        double currentHeading = floorMod(imu.getAngularOrientation().firstAngle, 360);
        double targetHeading = floorMod(currentHeading + turnAmount, 360);

        if (turnAmount > 0)
        {
            hardware.flwheel.setPower(-TURN_SPEED);
            hardware.frwheel.setPower(TURN_SPEED);
            hardware.blwheel.setPower(-TURN_SPEED);
            hardware.brwheel.setPower(TURN_SPEED);
        }
        else
        {
            hardware.flwheel.setPower(TURN_SPEED);
            hardware.frwheel.setPower(-TURN_SPEED);
            hardware.blwheel.setPower(TURN_SPEED);
            hardware.brwheel.setPower(-TURN_SPEED);
        }

        double diff;
        do
        {
            diff = targetHeading - floorMod(imu.getAngularOrientation().firstAngle, 360);
            teleLog("Diff:", diff);
            idle();
        }
        while ((diff > tolerance || diff < -tolerance) && opModeIsActive());

        hardware.flwheel.setPower(0);
        hardware.frwheel.setPower(0);
        hardware.blwheel.setPower(0);
        hardware.brwheel.setPower(0);
    }

    /**
     * Mathematical modulus function (nonnegative)
     * @param dividend -
     * @param divisor -
     * @return result
     */
    private double floorMod(double dividend, double divisor)
    {
        // FloorMod for floating point numbers
        dividend %= divisor;

        // only need to add once because the dividend cannot be < -mod (because of the val is only remainder)
        if (dividend < 0)
            dividend += divisor;

        return dividend;
    }

    /**
     * Write caption and value to the debug log and the telemetry
     * @param caption -
     * @param value -
     */
    private void teleLog(String caption, String value)
    {
        telemetry.addData(caption, value);
        telemetry.update();
        DbgLog.msg("TeleLog: " + caption + ": " + value);
    }

    /**
     * Write caption and value to the debug log and the telemetry
     * @param caption -
     * @param value -
     */
    private void teleLog(String caption, double value)
    {
        teleLog(caption, Double.toString(value));
    }

    /**
     * Stop/Reset Encoders, then change the mode of all wheel motors
     * @param mode RunMode to set the motors
     */
    private void changeWheelMode(DcMotor.RunMode mode)
    {
        /*
        teleLog("Status", "Resetting Encoders");

        telemetry.update();
        hardware.flwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.frwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.blwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.brwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        idle();

        teleLog("Status", "Encoders Reset");

        */

        hardware.flwheel.setMode(mode);
        hardware.frwheel.setMode(mode);
        hardware.blwheel.setMode(mode);
        hardware.brwheel.setMode(mode);

        teleLog("Status", "New Mode: " + mode.toString());
    }

    @Override
    public void runOpMode() throws InterruptedException {

        teleLog("Status", "Begin Initialization");

        hardware = new CompBotHardware();
        hardware.init(hardwareMap);
        teleLog("Status", "Hardware Initialized");

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "AdafruitIMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        //imu = hardwareMap.get(BNO055IMU.class, "imu");
        //imu.initialize(parameters);
        //teleLog("Status", "IMU Initialized");

        matchVars = new MatchSpecificVars("compBotAutoVars.txt");
        teleLog("Status", "Match Vars Loaded");

        changeWheelMode(DcMotor.RunMode.RUN_TO_POSITION);
        teleLog("Status", "Wheel Mode Set");

        hardware.flwheel.setPower(DRIVE_SPEED);
        hardware.frwheel.setPower(DRIVE_SPEED);
        hardware.blwheel.setPower(DRIVE_SPEED);
        hardware.brwheel.setPower(DRIVE_SPEED);
        teleLog("Status", "Wheel Power Set");

        // Acceleration Integration must be started before the waitForStart() method
        //imu.startAccelerationIntegration(new Position(), new Velocity(), 5);
        teleLog("Status", "IMU Integration started");

        teleLog("Status", "Ready To Start");
        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        hardware.brushy.setPosition(1);

        // Move Forward
        teleLog("Status", "Moving Forward");
        setNewTarget(20, 20, 20, 20);
        waitUntilDone();

        sleep(500);

        teleLog("Status", "Turning Slight");
        if (color == FieldColor.BLUE)
        {
            setNewTarget(-3, 3, -3, 3);
            waitUntilDone();
        }

        sleep(500);

        teleLog("Status", "Shooting Balls");
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

        if (color == FieldColor.BLUE)
        {
            teleLog("Status", "Turning Slight back");
            setNewTarget(3, -3, 3, -3);
            waitUntilDone();
        }

        sleep(500);

        // Move Forward
        teleLog("Status", "Moving Forward more");
        setNewTarget(12, 12, 12, 12);
        waitUntilDone();

        sleep(500);

        // Turn to face beacon
        teleLog("Status", "Turning to face beacon");
        switch (color)
        {
            case BLUE:
                setNewTarget(23, -23, 23, -23);
                break;
            case RED:
                setNewTarget(-23, 23, -23, 23);
                break;
        }
        waitUntilDone();

        sleep(500);

        // Move Closer to beacon
        teleLog("Status", "Moving closer to beacon");

        setNewTarget(30, 30, 30, 30);
        waitUntilDone();

        // Move Closer to beacon

        hardware.flwheel.setPower(0);
        hardware.frwheel.setPower(0);
        hardware.blwheel.setPower(0);
        hardware.brwheel.setPower(0);

        changeWheelMode(DcMotor.RunMode.RUN_USING_ENCODER);

        hardware.flwheel.setPower(0.3);
        hardware.frwheel.setPower(0.3);
        hardware.blwheel.setPower(0.3);
        hardware.brwheel.setPower(0.3);

        ultraCheck = 0;
        while (ultraCheck < 5 && opModeIsActive())
        {
            if (hardware.ultrasonic.getUltrasonicLevel() < 25)
                ultraCheck++;
            sleep(5);
            idle();
        }

        hardware.flwheel.setPower(0);
        hardware.frwheel.setPower(0);
        hardware.blwheel.setPower(0);
        hardware.brwheel.setPower(0);

        // Slide to the line
        teleLog("Status", "Sliding to line");
        switch (color)
        {
            case BLUE:
                hardware.flwheel.setPower(-0.2);
                hardware.frwheel.setPower(0.2);
                hardware.blwheel.setPower(0.2);
                hardware.brwheel.setPower(-0.2);
                break;
            case RED:
                hardware.flwheel.setPower(0.2);
                hardware.frwheel.setPower(-0.2);
                hardware.blwheel.setPower(-0.2);
                hardware.brwheel.setPower(0.2);
                break;
        }

        while (hardware.centerIR.getState() && opModeIsActive())
        {
            idle();
        }

        hardware.flwheel.setPower(0);
        hardware.frwheel.setPower(0);
        hardware.blwheel.setPower(0);
        hardware.brwheel.setPower(0);

        sleep(500);

        teleLog("Status", "Correcting drift");
        changeWheelMode(DcMotor.RunMode.RUN_TO_POSITION);
        hardware.flwheel.setPower(DRIVE_SPEED);
        hardware.frwheel.setPower(DRIVE_SPEED);
        hardware.blwheel.setPower(DRIVE_SPEED);
        hardware.brwheel.setPower(DRIVE_SPEED);

        switch (color)
        {
            case BLUE:
                setNewTarget(-1, 1, -1, 1);
                break;
            case RED:
                setNewTarget(1, -1, 1, -1);
                break;
        }
        waitUntilDone();

        // Move pusher down
        hardware.buttonPusher.setPosition(0.6);
        //hardware.intake.setPower(1);
        sleep(500);

        // Drive close enough to sense the color
        teleLog("Status", "Drive to see color");
        hardware.flwheel.setPower(0.2);
        hardware.frwheel.setPower(0.2);
        hardware.blwheel.setPower(0.2);
        hardware.brwheel.setPower(0.2);

        ultraCheck = 0;
        while (ultraCheck < 10 && opModeIsActive())
        {
            if (hardware.ultrasonic.getUltrasonicLevel() < 21)
                ultraCheck++;
            sleep(5);
            idle();
        }

        hardware.flwheel.setPower(0);
        hardware.frwheel.setPower(0);
        hardware.blwheel.setPower(0);
        hardware.brwheel.setPower(0);

        changeWheelMode(DcMotor.RunMode.RUN_TO_POSITION);
        hardware.flwheel.setPower(DRIVE_SPEED);
        hardware.frwheel.setPower(DRIVE_SPEED);
        hardware.blwheel.setPower(DRIVE_SPEED);
        hardware.brwheel.setPower(DRIVE_SPEED);

        teleLog("Status", "Pressing Button");
        if (checkColor())
        {
            // Push button
            setNewTarget(PUSH_DISTANCE, PUSH_DISTANCE, PUSH_DISTANCE, PUSH_DISTANCE);
            waitUntilDone();

            setNewTarget(-PUSH_DISTANCE, -PUSH_DISTANCE, -PUSH_DISTANCE, -PUSH_DISTANCE);
            waitUntilDone();

            // Reset pusher
            hardware.buttonPusher.setPosition(0);

            // Move to finish position
        }
        else
        {
            switch (color)
            {
                case BLUE:
                    setNewTarget(-4, 4, 4, -4);
                    waitUntilDone();

                    setNewTarget(PUSH_DISTANCE, PUSH_DISTANCE, PUSH_DISTANCE, PUSH_DISTANCE);
                    waitUntilDone();

                    setNewTarget(-PUSH_DISTANCE, -PUSH_DISTANCE, -PUSH_DISTANCE, -PUSH_DISTANCE);
                    waitUntilDone();
                    break;
                case RED:
                    setNewTarget(4, -4, -4, 4);
                    waitUntilDone();

                    setNewTarget(PUSH_DISTANCE, PUSH_DISTANCE, PUSH_DISTANCE, PUSH_DISTANCE);
                    waitUntilDone();

                    setNewTarget(-PUSH_DISTANCE, -PUSH_DISTANCE, -PUSH_DISTANCE, -PUSH_DISTANCE);
                    waitUntilDone();
                    break;
            }
        }

        //hardware.intake.setPower(0);

        //hardware.brushy.setPosition(0);

        // Hit the capball
        teleLog("Status", "Hitting capball");
        setNewTarget(-20, -20, -20, -20);
        waitUntilDone();

        sleep(500);

        setNewTarget(43, -43, 43, -43);
        waitUntilDone();

        sleep(500);

        setNewTarget(25, 25, 25, 25);
        waitUntilDone();

        // hit second beacon

        // turn around and hit cap ball


        hardware.flwheel.setPower(0);
        hardware.frwheel.setPower(0);
        hardware.blwheel.setPower(0);
        hardware.brwheel.setPower(0);
        
        telemetry.addData("Path", "Complete");
        telemetry.update();
    }
}

