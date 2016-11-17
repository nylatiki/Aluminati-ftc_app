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
import org.firstinspires.ftc.robotcore.external.navigation.Quaternion;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

/**
 * Created by benlimpa on 10/26/16.
 */
@TeleOp(name="Closed Loop TeleOp", group="Iterative Opmode")
public class ClosedLoopTele extends OpMode
{

    final double P_CONST = 1;
    final double I_CONST = 0.01;//1d/100;
    final double D_CONST = 0.005;//1d/100;

    final int SET_DELAY = 50;

    final double TURN_DEADZONE = 3;

    BNO055IMU imu;

    Orientation angles;
    Acceleration acceleration;

    double targetHeading;
    double integral;
    double old_diff;

    boolean wasManualMoving = false;

    double x;
    double y;
    double r;
    private CompBotHardware hardware;
    private ElapsedTime runtime;

    private boolean old_a = false;

    public void init()
    {
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "AdafruitIMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";

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
        targetHeading = 90;
    }

    public void loop()
    {
        telemetry.addData("Status", "Running: " + runtime.toString());
        angles = imu.getAngularOrientation();
        acceleration = imu.getLinearAcceleration();


        //translateMove(angles.firstAngle);
        x = deadband(gamepad1.left_stick_x);
        y = deadband(gamepad1.left_stick_y);
        r = deadband(gamepad1.right_stick_x);

        if (x == 0 && y == 0 && r == 0)
        {
            if (wasManualMoving)
            {
                targetHeading = floorMod(angles.firstAngle, 360);
                targetHeading = floorMod(angles.firstAngle, 360);
            }

            autoTurn(angles.firstAngle);
            wasManualMoving = false;
        }
        else
            wasManualMoving = true;

        x = scaleInput(x);
        y = scaleInput(y);
        //r = scaleInput(r);

        move();
        buttonControl();

        telemetry.addData("Bob's_Angle: ", angles.firstAngle);
    }

    public void stop()
    {

    }

    // Unfinished
    private void translateMove(double currentHeading)
    {
        double x = deadband(gamepad1.left_stick_x);
        double y = deadband(gamepad1.left_stick_y);
        double angle = Math.atan(y / x) * 180 / Math.PI;
        double magnitude = Math.sqrt(y * y + x * x);

        currentHeading = floorMod(currentHeading, 360);

        angle += currentHeading;

        angle = floorMod(angle, 360);

        this.x = Math.cos(angle * Math.PI / 180) * magnitude;
        this.y = Math.sin(angle * Math.PI / 180) * magnitude;
    }

    private double floorMod(double val, double mod)
    {
        // FloorMod for floating point numbers
        val %= mod;

        // only need to add once because the value cannot be < -mod (because of the val is only remainder)
        if (val < 0)
            val += mod;

        return val;
    }

    private void autoTurn(double currentHeading)
    {
        currentHeading = floorMod(currentHeading, 360);

        double diff = targetHeading-currentHeading;

        // Ensure that the difference is the short way (315-45)
        if (diff > 180)
            diff = diff - 360;

        telemetry.addData("Current Heading", currentHeading);
        telemetry.addData("Diff", diff);

        double prop = diff * P_CONST;
        integral += diff * I_CONST; // When it reaches the desired value, it may still have a non-zero value
        double deriv = (diff - old_diff) * D_CONST;

        r = - (prop + integral + deriv) / 180;

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

    private void move()
    {
        hardware.flwheel.setPower(capVal(x + y + r));
        hardware.frwheel.setPower(capVal(-x + y - r));
        hardware.blwheel.setPower(capVal(-x + y + r));
        hardware.brwheel.setPower(capVal(x + y - r));
    }
    
    private double capVal(double val)
    {
        if (val > 1)
            return 1;
        if (val < -1)
            return -1;
        return val;
    }

    void buttonControl()
    {
        if (gamepad1.a)
            hardware.shooter.setPower(0.75);
        else if (gamepad1.b)
            hardware.shooter.setPower(-0.75);
        else
            hardware.shooter.setPower(0);

        if (gamepad1.x)
            hardware.intake.setPower(0.75);
        else if (gamepad1.y)
            hardware.intake.setPower(-0.75);
        else
            hardware.intake.setPower(0);
    }

    double deadband(double input)
    {
        if (input < 0.05 && input > -0.05)
            return 0;
        else
            return input;
    }

    // Map the linear joystick input to a quartic function for greater accuracy at lower speeds
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
