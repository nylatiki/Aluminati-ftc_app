package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

/**
 * Outline:
 * Constantly maintain set orientation while moving from point to point
 * When turing, change the target position and allow the same PID algorithm to compensate
 */
@TeleOp(name="Closed Loop Autonomous", group="CompBot")
public class ClosedLoopAuto extends OpMode
{

    /*
    Constants
     */

    // PID constants
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
    static final double     TURN_SPEED              = 0.3;

    // Autonomous Constants
    public enum FieldColor {BLUE, RED}

    private FieldColor color;
    private int colorConst; // Blue = 1, Red = -1

    /*
        Class instance variables
     */
    double targetHeading;
    double integral; // to keep track of the cumulative error
    double old_diff;

    // Movement vectors
    double x;
    double y;
    double r;

    // Variables for
    private CompBotHardware hardware;
    private ElapsedTime runtime;

    BNO055IMU imu;
    Orientation angles;

    private boolean old_a = false;

    public void init()
    {
        telemetry.addData("Status", "Begin Initialization");
        telemetry.update();
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "AdafruitIMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new CustomAccelerationIntegrator();

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        runtime = new ElapsedTime();
        telemetry.addData("Status", "Initializing...");
        hardware = new CompBotHardware();
        hardware.init(hardwareMap);

        hardware.switchDirection(CompBotHardware.BotDirection.INTAKE_FRONT);
        hardware.shooter.setDirection(DcMotorSimple.Direction.REVERSE);

        telemetry.addData("Status", "Initialized");

        integral = 0;
    }
    public void start()
    {
        runtime.reset();
        imu.startAccelerationIntegration(new Position(DistanceUnit.CM, 0, 0, 0, 0), new Velocity(), 5);
        targetHeading = 0;

        x = 0;
        y = 0;
        r = 0;
    }

    public void loop()
    {
        telemetry.addData("Status", "Running for " + runtime.toString());
        angles = imu.getAngularOrientation();

        updateGyroAdj(angles.firstAngle);
        telemetry.addData("Target Heading", targetHeading);

        telemetry.addData("x", x);
        telemetry.addData("y", y);
        telemetry.addData("z", r);

        updateMove();
        telemetry.update();
    }

    /**
     * Updates the power for each wheel motor
     */
    private void updateMove()
    {
        hardware.flwheel.setPower(capVal(x + y + r));
        hardware.frwheel.setPower(capVal(-x + y - r));
        hardware.blwheel.setPower(capVal(-x + y + r));
        hardware.brwheel.setPower(capVal(x + y - r));
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

    /**
     * Modulu function, but ensures that the value is between 0 and val
     * @param val the dividend
     * @param mod the divisor
     * @return result of modulo function
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
        integral += diff * I_CONST; // When it reaches the desired value, it may still have a non-zero value
        double deriv = (diff - old_diff) * D_CONST;

        r = - (prop + integral + deriv) / 180;

        if (diff < TURN_DEADZONE && diff > -TURN_DEADZONE)
        {
            r = 0; // deadzone affect the motor speed directly so that the integral would not leave
            integral = 0;
        }

        old_diff = diff;

        telemetry.addData("p", prop);
        telemetry.addData("i", integral);
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
