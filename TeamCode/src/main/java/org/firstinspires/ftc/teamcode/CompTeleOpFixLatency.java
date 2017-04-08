package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Created by benlimpa on 10/26/16.
 */
@TeleOp(name="Competition TeleOp Minimal", group="Iterative Opmode")
public class CompTeleOpFixLatency extends OpMode
{

    private CompBotHardware hardware;
    private ElapsedTime runtime;

    public void init()
    {
        runtime = new ElapsedTime();
        telemetry.addData("Status", "Initializing...");
        hardware = new CompBotHardware();
        hardware.init(hardwareMap);

        hardware.lFork.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hardware.rFork.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //hardware.intake.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        hardware.flwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hardware.frwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hardware.blwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hardware.brwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        hardware.switchDirection(CompBotHardware.BotDirection.INTAKE_FRONT);
        hardware.shooter.setDirection(DcMotorSimple.Direction.REVERSE);

        hardware.buttonPusher.setPosition(0);
        hardware.lForkHolder.setPosition(1);
        hardware.rForkHolder.setPosition(1);

        telemetry.addData("Status", "Initialized");
    }
    public void start()
    {
        runtime.reset();
        hardware.intake.setTargetPosition(1440);
        hardware.brushy.setPosition(1);
    }

    public void loop()
    {
        telemetry.addData("Status", "Running: " + runtime.toString());
        double x = scaleInput(deadzone(gamepad1.left_stick_x));
        double y = scaleInput(deadzone(-gamepad1.left_stick_y));
        double r = scaleInput(deadzone(gamepad1.right_stick_x));
        hardware.flwheel.setPower(x + y + r);
        hardware.frwheel.setPower(-x + y - r);
        hardware.blwheel.setPower(-x + y + r);
        hardware.brwheel.setPower(x + y - r);

        if (gamepad1.right_bumper)
            hardware.shooter.setPower(1);
        else if (gamepad1.left_bumper)
            hardware.shooter.setPower(-1);
        else
            hardware.shooter.setPower(0);

        if(gamepad1.right_trigger > 0.1)
            hardware.intake.setPower(gamepad1.right_trigger);
        else if (gamepad1.left_trigger > 0.1)
            hardware.intake.setPower(-gamepad1.left_trigger);
        else
            hardware.intake.setPower(0);

        if (gamepad1.dpad_down)
        {
            hardware.buttonPusher.setPosition(0.6);
        }
        else if (gamepad1.dpad_up)
        {
            hardware.buttonPusher.setPosition(0);
        }
    }

    public void stop()
    {

    }

    private int floorMod(int val, int mod)
    {
        // FloorMod for floating point numbers
        val %= mod;

        // only need to add once because the value cannot be < -mod (because of the val is only remainder)
        if (val < 0)
            val += mod;

        return val;
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

    double deadzone(double input)
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
        scaledInput = (input / Math.abs(input)) * (Math.pow(Math.abs(input) + 0.2, 4) / 2 + 0.05);
        if (Math.abs(scaledInput) > 1)
            scaledInput = scaledInput / Math.abs(scaledInput);

        return scaledInput;
    }

}
