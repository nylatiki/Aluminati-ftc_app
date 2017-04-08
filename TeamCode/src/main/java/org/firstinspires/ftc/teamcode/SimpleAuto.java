package org.firstinspires.ftc.teamcode;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

//@Autonomous(name="Simple Auto", group="CompBot")
abstract public class SimpleAuto extends OpMode
{

    /*
    Constants
     */

    // Gyro Constants
    final double P_CONST = 1;
    final double I_CONST = 0.01;//1d/100;
    final double D_CONST = 0.005;//1d/100;
    final double TURN_DEADZONE = 3;

    // Encoder Constants
    static final double     COUNTS_PER_MOTOR_REV    = 1440 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 5 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * Math.PI);
    static final double     DRIVE_SPEED             = 0.6;

    // Ultrasonic Sensor
    final int ULTRA_OFFSET = -5;
    final int ULTRA_SPEED = 40; // Higher = slower
    final int ULTRA_TOLERANCE = 1;

    // IR Sensor
    final double IR_DRIVE_SPEED = 0.3;

    // Button Pushing
    final double PUSH_DISTANCE = 10;

    // Autonomous Constants
    public enum FieldColor {BLUE, RED}

    // States
    protected enum MasterState
    {
        ENCODER_MOVE,
        GYRO_TURN,
        NEW_INSTRUCTION,
        WAIT_TIME,
        ULTRA_MOVE,
        IR_MOVE,
        IR_MOVE_ALT
    }

    protected enum PushButtonState
    {
        DECIDE_COLOR,
        MOVE_FORWARD,
        MOVE_BACK,
        MOVE_SIDE,
        MOVE_ALIGN,
        MOVE_TO_SHOOT
    }

    /*
        Class instance variables
     */

    // Gyro Variables
    double targetHeading;
    double gryoInteg; // to keep track of the cumulative error
    double old_diff;

    // Ultrasonic Variables
    int lUltraTarget;
    int rUltraTarget;

    // Movement vectors
    double r;

    // Hardware
    protected CompBotHardware hardware;
    protected DriveTrain driveTrain;
    private ElapsedTime runtime;

    // Trans-state variables
    protected int instructionState;
    protected PushButtonState buttonInstructionState;
    protected MasterState masterState;
    protected boolean needToMoveBack;
    private double waitTime;
    private double waitInterval;

    private int ultraStopCount;

    BNO055IMU imu;

    protected MatchSpecificVars matchSpecificVars;

    public void init()
    {
        teleLog("Status", "Begin Init");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "AdafruitIMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";

        teleLog("Status", "Initializing IMU");
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        teleLog("Status", "Initializing Hardware");
        runtime = new ElapsedTime();
        hardware = new CompBotHardware();
        hardware.init(hardwareMap);

        matchSpecificVars = new MatchSpecificVars("compBotAutoVars.txt");

        driveTrain = new DriveTrain(hardware.flwheel, hardware.frwheel, hardware.blwheel, hardware.brwheel);


        hardware.switchDirection(CompBotHardware.BotDirection.INTAKE_FRONT);
        hardware.shooter.setDirection(DcMotorSimple.Direction.REVERSE);

        hardware.flwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.frwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.blwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hardware.brwheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        hardware.flwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hardware.frwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hardware.blwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hardware.brwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        teleLog("Status", "Initialization Finished");

        gryoInteg = 0;
    }

    public void start()
    {
        runtime.reset();
        imu.startAccelerationIntegration(new Position(DistanceUnit.CM, 0, 0, 0, 0), new Velocity(), 5);
        targetHeading = 0;

        instructionState = 0;
        masterState = MasterState.NEW_INSTRUCTION;

        r = 0;

        hardware.buttonPusher.setPosition(CompBotHardware.BUTTON_PUSHER_UP);
        hardware.lForkHolder.setPosition(CompBotHardware.L_FORK_HOLD);
        hardware.rForkHolder.setPosition(CompBotHardware.R_FORK_HOLD);
        hardware.ballDoor.setPosition(CompBotHardware.BALL_DOOR_CLOSE);
        hardware.ballDoor2.setPosition(CompBotHardware.BALL_DOOR2_CLOSE);
        hardware.itsOver.setPosition(CompBotHardware.ITS_NOT_OVER);
    }

    public void loop()
    {
        telemetry.addData("Status", "Running for " + runtime.toString());

        /*
        Check if the target has been reached
         */

        teleLog("Master State", masterState.toString());
        teleLog("Instruction State", Integer.toString(instructionState));
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

        switch (masterState)
        {
            case IR_MOVE:
                irMove();
                break;
            case IR_MOVE_ALT:
                irMoveAlt();
                break;
            case ULTRA_MOVE:
                double lUltraLevel = hardware.lUltra.getUltrasonicLevel();
                double rUltraLevel = hardware.rUltra.getUltrasonicLevel();

                if (ultraStopCount > 20)
                {
                    teleLog("Ultra Move", "Stopped at " + Double.toString(lUltraLevel) + ", " + Double.toString(rUltraLevel));
                    driveTrain.stopAll();
                    masterState = MasterState.NEW_INSTRUCTION;
                    ultraStopCount = 0;
                }

                if (Math.abs(lUltraLevel - rUltraLevel) > 35
                        || lUltraLevel > 250 || rUltraLevel > 250)
                {
                    // Throw out bad input
                    teleLog("Bad Ultrasonic Data", Double.toString(lUltraLevel) + ", " + Double.toString(rUltraLevel));
                    ultraStopCount++;
                }
                else
                {
                    if (Math.abs(lUltraTarget - (lUltraLevel + ULTRA_OFFSET)) > ULTRA_TOLERANCE ||
                            Math.abs(rUltraTarget - (rUltraLevel+ ULTRA_OFFSET)) > ULTRA_TOLERANCE)
                    {
                        double lPower = -minVal(capVal((lUltraTarget - (lUltraLevel + ULTRA_OFFSET)) / ULTRA_SPEED));
                        double rPower = -minVal(capVal((rUltraTarget - (rUltraLevel + ULTRA_OFFSET)) / ULTRA_SPEED));

                        telemetry.addData("LPower", lPower);
                        telemetry.addData("RPower", rPower);
                        telemetry.addData("lUltra", lUltraLevel);
                        telemetry.addData("rUltra", rUltraLevel);

                        hardware.flwheel.setPower(lPower);
                        hardware.blwheel.setPower(lPower);
                        hardware.frwheel.setPower(rPower);
                        hardware.brwheel.setPower(rPower);
                    }
                    else
                    {
                        ultraStopCount++;
                    }
                }
                break;
            case WAIT_TIME:
                if (runtime.milliseconds()-waitTime < waitInterval)
                {
                    // Wait
                }
                else
                {
                    masterState = MasterState.NEW_INSTRUCTION;
                }
                break;
            case ENCODER_MOVE:
                if (driveTrain.isBusy())
                {
                    driveTrain.updatePower();
                }
                else
                {
                    driveTrain.stopAll();
                    masterState = MasterState.NEW_INSTRUCTION;
                }
                break;
            case GYRO_TURN:
                double currentHeading = floorMod(imu.getAngularOrientation().firstAngle, 360);
                double diff = targetHeading-currentHeading;
                if (diff < TURN_DEADZONE && diff > -TURN_DEADZONE)
                {
                    r = 0; // deadzone affect the motor speed directly so that the integral would not leave
                    gryoInteg = 0;
                    driveTrain.stopAll();
                    masterState = MasterState.NEW_INSTRUCTION;
                }
                else
                {
                    // Ensure that the difference is the short way (315-45)
                    if (diff > 180)
                        diff = diff - 360;
                    else if (diff < -180)
                    {
                        diff += 360;
                        //diff = -diff;
                    }

                    double prop = diff * P_CONST;
                    gryoInteg += diff * I_CONST; // When it reaches the desired value, it may still have a non-zero value
                    double deriv = (diff - old_diff) * D_CONST;

                    r = -(prop + gryoInteg + deriv) / 180;

                    old_diff = diff;

                    driveTrain.turn(r);

                    telemetry.addData("p", prop);
                    telemetry.addData("i", gryoInteg);
                    telemetry.addData("d", deriv);
                    telemetry.addData("r", r);
                    telemetry.addData("Target Heading", targetHeading);
                    telemetry.addData("Diff", diff);
                    telemetry.addData("Current Heading", currentHeading);
                }
                break;
            case NEW_INSTRUCTION:
                switch (instructionState)
                {
                    case 0:
                        driveTrain.setTargets(20, 20, 20, 20);

                        masterState = MasterState.ENCODER_MOVE;
                        instructionState++;
                        break;
                    case 1:
                        changeHeading(-45);

                        masterState = MasterState.GYRO_TURN;
                        instructionState++;
                        break;
                    case 2:
                        driveTrain.setTargets(60, 60, 60, 60);

                        masterState = MasterState.ENCODER_MOVE;
                        instructionState++;
                        break;
                    case 3:
                        changeHeading(-45);

                        masterState = MasterState.GYRO_TURN;
                        instructionState++;
                        break;
                    case 4:
                        lUltraTarget = 17;
                        rUltraTarget = 17;

                        masterState = MasterState.ULTRA_MOVE;
                        instructionState++;
                        break;
                    case 5:
                        instructionState++;
                        masterState = MasterState.IR_MOVE;
                        break;
                    case 6:
                        //driveTrain.setTargets(2, -2, -2, 2);
                        //masterState = MasterState.ENCODER_MOVE;
                        instructionState++;
                        break;
                    case 7:
                        buttonInstructionState = PushButtonState.DECIDE_COLOR;
                        instructionState++;
                        waitInterval = 1000;
                        waitTime = runtime.milliseconds();
                        masterState = MasterState.WAIT_TIME;
                        break;
                    case 8:
                        pushButton();
                        break;
                    case 9:
                        lUltraTarget = 20;
                        rUltraTarget = 20;

                        masterState = MasterState.ULTRA_MOVE;
                        instructionState++;
                        break;
                    case 10:
                        irPrime();
                        break;
                    case 11:
                        lUltraTarget = 20;
                        rUltraTarget = 20;

                        masterState = MasterState.ULTRA_MOVE;
                        instructionState++;
                        break;
                    case 12:
                        masterState = MasterState.IR_MOVE_ALT;
                        instructionState++;
                        break;
                    case 13:
                        lUltraTarget = 17;
                        rUltraTarget = 17;

                        masterState = MasterState.ULTRA_MOVE;
                        instructionState++;
                        break;
                    case 14:
                        //driveTrain.setTargets(3.5, -3.5, -3.5, 3.5);
                        //masterState = MasterState.ENCODER_MOVE;
                        instructionState++;
                        break;
                    case 15:
                        buttonInstructionState = PushButtonState.DECIDE_COLOR;
                        instructionState++;
                        waitInterval = 1000;
                        waitTime = runtime.milliseconds();
                        masterState = MasterState.WAIT_TIME;
                        break;
                    case 16:
                        pushButton();
                        break;
                    case 17:
                        turnToShoot();
                        break;
                    case 18:
                        driveTrain.setTargets(36, 36, 36, 36);
                        masterState = MasterState.ENCODER_MOVE;
                        instructionState++;
                        break;
                    case 19:
                        hardware.shooter.setPower(1);
                        waitTime = runtime.milliseconds();
                        waitInterval = 2000;

                        masterState = MasterState.WAIT_TIME;
                        instructionState++;
                        break;
                    case 20:
                        hardware.shooter.setPower(0);
                        hardware.ballDoor.setPosition(CompBotHardware.BALL_DOOR_OPEN);
                        waitTime = runtime.milliseconds();
                        waitInterval = 800;

                        masterState = MasterState.WAIT_TIME;
                        instructionState++;
                        break;
                    case 21:
                        hardware.shooter.setPower(1);
                        hardware.ballDoor.setPosition(CompBotHardware.BALL_DOOR_CLOSE);
                        waitTime = runtime.milliseconds();
                        waitInterval = 2000;

                        masterState = MasterState.WAIT_TIME;
                        instructionState++;
                        break;
                    case 22:
                        hardware.itsOver.setPosition(CompBotHardware.ITS_OVER);
                        hardware.shooter.setPower(0);
                        //driveTrain.setTargets(20, 20, 20, 20);

                        //masterState = MasterState.ENCODER_MOVE;
                        instructionState++;
                        break;
                    case 23:
                        requestOpModeStop();
                        break;
                }
                break;
        }

        telemetry.update();
    }

    abstract protected void pushButton();

    abstract protected void irPrime();

    abstract protected void irMove();

    abstract protected void irMoveAlt();

    abstract protected void changeHeading(double heading);

    abstract protected void turnToShoot();

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
     * Truncates the value if it is out of bounds [-1,1]
     * @param val value to be truncated
     * @return truncated value
     */
    private double capVal(double val)
    {
        if (val > 1)
            return 1;
        if (val < -1)
            return -1;
        return val;
    }

    private double minVal(double val)
    {
        if (0 < val && val < 0.2)
            return 0.2;
        else if (-0.2 < val && val < 0)
            return -0.2;
        else
            return val;
    }

    /**
     * Modulo function, but ensures that the value is between 0 and val
     * @param val the dividend
     * @param mod the divisor
     * @return result of modulo function
     */
    protected double floorMod(double val, double mod)
    {
        // FloorMod for floating point numbers
        val %= mod;

        // only need to add once because the value cannot be < -mod (because of the val is only remainder)
        if (val < 0)
            val += mod;

        return val;
    }

    private void teleLog(String caption, String value)
    {
        telemetry.addData(caption, value);
        telemetry.update();
        DbgLog.msg("TeleLog: " + caption + ": " + value);
    }

    /**
     * Updates the rotation modifier to hold current heading
     * @param currentHeading the current heading of the robot
     */
    private void updateGyroAdj(double currentHeading)
    {
        currentHeading = floorMod(currentHeading, 360);

        double diff = targetHeading-currentHeading;

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
        gryoInteg += diff * I_CONST; // When it reaches the desired value, it may still have a non-zero value
        double deriv = (diff - old_diff) * D_CONST;

        r = - (prop + gryoInteg + deriv) / 180;

        if (diff < TURN_DEADZONE && diff > -TURN_DEADZONE)
        {
            r = 0; // deadzone affect the motor speed directly so that the integral would not leave
            gryoInteg = 0;
        }

        old_diff = diff;

        telemetry.addData("p", prop);
        telemetry.addData("i", gryoInteg);
        telemetry.addData("d", deriv);
        telemetry.addData("r", r);
        telemetry.addData("Position", imu.getPosition());
    }

    /**
     * Truncate values below a certain threshold
     * @param input original value
     * @return truncated value
     */
    double deadband(double input)
    {
        if (input < 0.05 && input > -0.05)
            return 0;
        else
            return input;
    }

    /**
     *  Map the linear joystick input to a quartic function for greater accuracy at lower speeds
     * @param input input
     * @return output
     */
    double scaleInput(double input)
    {
        if (input == 0)
            return 0;

        double scaledInput;
        //              sign * ((input+0.2)^4)/2+0.05
        scaledInput = (input / Math.abs(input)) * (Math.pow(Math.abs(input) + 0.2, 4) / 2 + 0.1);
        if (Math.abs(scaledInput) > 1)
            scaledInput = scaledInput / Math.abs(scaledInput);

        return scaledInput;
    }
}
