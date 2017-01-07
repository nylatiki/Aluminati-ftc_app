package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Created by benlimpa on 10/26/16.
 */
@TeleOp(name="Competition TeleOp Slow Mode", group="Iterative Opmode")
public class CompTeleOpSlow extends OpMode
{

    private CompBotHardware hardware;
    private boolean slow;
    private ElapsedTime runtime;

    public void init()
    {
        slow = false;
        runtime = new ElapsedTime();
        telemetry.addData("Status", "Initializing...");
        hardware = new CompBotHardware();
        hardware.init(hardwareMap);

        hardware.lFork.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hardware.rFork.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hardware.intake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //hardware.lFork.setPower(0.75);
        //hardware.rFork.setPower(0.75);

        hardware.switchDirection(CompBotHardware.BotDirection.INTAKE_FRONT);
        hardware.shooter.setDirection(DcMotorSimple.Direction.REVERSE);

        hardware.buttonPusher.setPosition(0);

        telemetry.addData("Status", "Initialized");
    }
    public void start()
    {
        runtime.reset();
    }

    public void loop()
    {
        telemetry.addData("Status", "Running: " + runtime.toString());

        double x = scaleInput(deadzone(gamepad1.left_stick_x));
        double y = scaleInput(deadzone(-gamepad1.left_stick_y));
        double r = scaleInput(deadzone(gamepad1.right_stick_x));

        if (slow)
        {
            x *= 0.5;
            y *= 0.5;
            r *= 0.5;
        }

        hardware.flwheel.setPower(x + y + r);
        hardware.frwheel.setPower(-x + y - r);
        hardware.blwheel.setPower(-x + y + r);
        hardware.brwheel.setPower(x + y - r);

        telemetry.addData("motor power: ", hardware.flwheel.getPower());

        if (gamepad1.a)
            slow = true;
        else if (gamepad1.b)
            slow = false;

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
       /*int currentPos = hardware.intake.getCurrentPosition();
        if (gamepad1.x)
            hardware.intake.setPower(1);
        else if (gamepad1.y)
            hardware.intake.setPower(-1);
        else if (Math.abs(currentPos % 720) > 10)
        {
            int modPos = floorMod(currentPos, 720);
            if (modPos > 360)
                hardware.intake.setPower(-modPos / 450d);
            else
                hardware.intake.setPower((720 - modPos) / 450d);
        }
        else
            hardware.intake.setPower(0);*/

        /*
        telemetry.addData("Intake", hardware.intake.getCurrentPosition());
        telemetry.addData("lFork", hardware.lFork.getCurrentPosition());
        telemetry.addData("rFork", hardware.rFork.getCurrentPosition());
        telemetry.addData("AyLmao", gamepad1.right_trigger);
        telemetry.addData("AyyyLamo420", hardware.intake.getPower());
        */


        if (gamepad1.dpad_down)
        {
            hardware.buttonPusher.setPosition(0.6);
        }
        else if (gamepad1.dpad_up)
        {
            hardware.buttonPusher.setPosition(0);
        }
        /*
        if (gamepad1.dpad_down)
        {
            hardware.lFork.setPower(1);
            hardware.rFork.setPower(-1);
        }
        else if (gamepad1.dpad_up)
        {
            hardware.lFork.setPower(-1);
            hardware.rFork.setPower(1);
        }
        else
        {
            hardware.lFork.setPower(0);
            hardware.rFork.setPower(0);
        }
        */

/*
        if (gamepad1.dpad_left)
        {
            hardware.lFork.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            hardware.rFork.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
        else
        {
            hardware.lFork.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            hardware.rFork.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }
*/
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
