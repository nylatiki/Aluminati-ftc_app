package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Created by benlimpa on 10/26/16.
 */
@TeleOp(name="Competition TeleOp", group="Iterative Opmode")
public class CompTeleOp extends OpMode
{

    private CompBotHardware hardware;
    private ElapsedTime runtime;
    private double gateSetTime;

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

        hardware.buttonPusher.getController().pwmEnable();

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

        telemetry.addData("motor power: ", hardware.flwheel.getPower());

        telemetry.addData("Ultrasonic", hardware.ultrasonic.getUltrasonicLevel());
        telemetry.update();

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

        if (gamepad2.dpad_up)
        {
            hardware.lFork.setPower(-1);
            hardware.rFork.setPower(1);
        }
        else if (gamepad2.dpad_down)
        {
            hardware.lFork.setPower(1);
            hardware.rFork.setPower(-1);
        }
        else
        {
            hardware.lFork.setPower(deadzone(gamepad2.left_stick_y));
            hardware.rFork.setPower(deadzone(-gamepad2.right_stick_y));
        }

        if (gamepad2.left_bumper)
        {
            hardware.lForkHolder.setPosition(1);
            hardware.rForkHolder.setPosition(1);
        }
        else if (gamepad2.right_bumper)
        {
            hardware.lForkHolder.setPosition(0);
            hardware.rForkHolder.setPosition(0);
        }

        if (gamepad1.a)
        {
            hardware.ballDoor.setPosition(0.5);
            gateSetTime = runtime.milliseconds();
        }
        else if (runtime.milliseconds() - gateSetTime > 500)
        {
            hardware.ballDoor.setPosition(0.6);
        }

/*
        if (gamepad1.x)
            hardware.intake.setPower(1);
        else if (gamepad1.y)
            hardware.intake.setPower(-1);
        else
        {
            double modPos = floorMod(hardware.intake.getCurrentPosition(), 720); // 180 * 4 - 720 (1440 per rot)
            if (modPos > 20 && modPos < 720 - 20) // if true the position is must be adjusted
            {
                modPos -= 360; // center 0 to 90 (90 * 4 = 360)
                modPos /= (2500); // Normalize the degrees to convert to power
                hardware.intake.setPower(modPos);
            }
            else
                hardware.intake.setPower(0);
        }
        */

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
