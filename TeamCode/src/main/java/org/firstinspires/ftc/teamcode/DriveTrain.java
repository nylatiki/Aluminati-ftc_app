package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Created by benlimpa on 2/19/17.
 */

public class DriveTrain
{
    // PID constants
    final double P_CONST = 1;
    final double I_CONST = 0.01;//1d/100;
    final double D_CONST = 0.005;//1d/100;

    final double TURN_DEADZONE = 3;
    final double FINISH_THRESHOLD = 90;
    final double MOVE_CONSTANT = 360;

    // Encoder Constants
    static final double     COUNTS_PER_MOTOR_REV    = 1440 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 5 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * Math.PI);
    static final double     DRIVE_SPEED             = 0.6;
    static final double     TURN_SPEED              = 0.3;

    // Wheels
    public DcMotor frwheel;
    public DcMotor flwheel;
    public DcMotor brwheel;
    public DcMotor blwheel;

    private double powerMultiplier;
    
    public DriveTrain(DcMotor flwheel, DcMotor frwheel, DcMotor blwheel, DcMotor brwheel)
    {
        this.flwheel = flwheel;
        this.frwheel = frwheel;
        this.blwheel = blwheel;
        this.brwheel = brwheel;

        powerMultiplier = 1;
    }

    public void setTargets(double flWheelTarget, double frWheelTarget, double blWheelTarget, double brWheelTarget)
    {
        flwheel.setTargetPosition(flwheel.getCurrentPosition() + convertToCount(flWheelTarget));
        frwheel.setTargetPosition(frwheel.getCurrentPosition() + convertToCount(frWheelTarget));
        blwheel.setTargetPosition(blwheel.getCurrentPosition() + convertToCount(blWheelTarget));
        brwheel.setTargetPosition(brwheel.getCurrentPosition() + convertToCount(brWheelTarget));
    }

    public boolean isBusy()
    {
        return ( Math.abs(flwheel.getTargetPosition() - flwheel.getCurrentPosition()) > FINISH_THRESHOLD) &&
                (Math.abs(frwheel.getTargetPosition() - frwheel.getCurrentPosition()) > FINISH_THRESHOLD) &&
                (Math.abs(blwheel.getTargetPosition() - blwheel.getCurrentPosition()) > FINISH_THRESHOLD) &&
                (Math.abs(brwheel.getTargetPosition() - brwheel.getCurrentPosition()) > FINISH_THRESHOLD);
    }

    public void turn(double r)
    {
        flwheel.setPower(capVal(r));
        frwheel.setPower(capVal(-r));
        blwheel.setPower(capVal(r));
        brwheel.setPower(capVal(-r));
    }

    public void stopAll()
    {
        flwheel.setPower(0);
        frwheel.setPower(0);
        blwheel.setPower(0);
        brwheel.setPower(0);
    }

    public void setPowerMultiplier(double multiplier)
    {
        powerMultiplier = multiplier;
    }

    public void updatePower()
    {
        flwheel.setPower(getMotorPower(flwheel) * powerMultiplier);
        frwheel.setPower(getMotorPower(frwheel) * powerMultiplier);
        blwheel.setPower(getMotorPower(blwheel) * powerMultiplier);
        brwheel.setPower(getMotorPower(brwheel) * powerMultiplier);
    }

    private double getMotorPower(DcMotor motor)
    {
        return capVal((motor.getTargetPosition() - motor.getCurrentPosition()) / MOVE_CONSTANT);
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
     * Converts the distance in inches to encoder ticks according to the wheel diameter and
     * @param inches the distance in inches
     * @return the distance in encoder ticks
     */
    private int convertToCount(double inches)
    {
        return (int) (inches * COUNTS_PER_INCH);
    }
}
