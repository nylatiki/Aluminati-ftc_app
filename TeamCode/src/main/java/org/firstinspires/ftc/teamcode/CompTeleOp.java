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

    public void init()
    {
        runtime = new ElapsedTime();
        telemetry.addData("Status", "Initializing...");
        hardware = new CompBotHardware();
        hardware.init(hardwareMap);

        hardware.lFork.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        hardware.rFork.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        hardware.intake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        hardware.lFork.setPower(0.75);
        hardware.rFork.setPower(0.75);

        hardware.switchDirection(CompBotHardware.BotDirection.INTAKE_FRONT);
        hardware.shooter.setDirection(DcMotorSimple.Direction.REVERSE);

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
        hardware.flwheel.setPower(x + y + r);
        hardware.frwheel.setPower(-x + y - r);
        hardware.blwheel.setPower(-x + y + r);
        hardware.brwheel.setPower(x + y - r);

        telemetry.addData("motor power: ", hardware.flwheel.getPower());
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
        else if (Math.abs((hardware.intake.getCurrentPosition())) % 720 > 10)
            hardware.intake.setPower(-((hardware.intake.getCurrentPosition() % 720) - 360) / 360d);
        else
            hardware.intake.setPower(0);

        telemetry.addData("Intake", hardware.intake.getCurrentPosition());
        telemetry.addData("lFork", hardware.lFork.getCurrentPosition());
        telemetry.addData("rFork", hardware.rFork.getCurrentPosition());

        if (gamepad1.dpad_down)
        {
            hardware.lFork.setTargetPosition(1800);
            hardware.rFork.setTargetPosition(-1800);
        }
        else if (gamepad1.dpad_up)
        {
            hardware.lFork.setTargetPosition(0);
            hardware.rFork.setTargetPosition(0);
        }


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

    }

    public void stop()
    {

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
