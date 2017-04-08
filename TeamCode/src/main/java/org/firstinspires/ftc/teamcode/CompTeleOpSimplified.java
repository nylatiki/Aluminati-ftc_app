package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Created by benlimpa on 10/26/16.
 */
@TeleOp(name="Competition TeleOp Simplified", group="Competition")
public class CompTeleOpSimplified extends OpMode
{
    private int GATE_TIME = 500;
    private int GATE2_TIME = 500;

    private CompBotHardware hardware;
    private ElapsedTime runtime;
    private double gateTimer;
    private double gate2Timer;

    private boolean old_a;
    private boolean gate2;
    private boolean old_gate2;

    public void init()
    {
        runtime = new ElapsedTime();
        telemetry.addData("Status", "Initializing...");
        hardware = new CompBotHardware();
        hardware.init(hardwareMap);

        hardware.flwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hardware.frwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hardware.blwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        hardware.brwheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        hardware.switchDirection(CompBotHardware.BotDirection.INTAKE_FRONT);
        hardware.shooter.setDirection(DcMotorSimple.Direction.REVERSE);

        hardware.buttonPusher.getController().pwmEnable();

        telemetry.addData("Status", "Initialized");
        hardware.buttonPusher.setPosition(CompBotHardware.BUTTON_PUSHER_UP);
        hardware.lForkHolder.setPosition(CompBotHardware.L_FORK_HOLD);
        hardware.rForkHolder.setPosition(CompBotHardware.R_FORK_HOLD);
        hardware.ballDoor.setPosition(CompBotHardware.BALL_DOOR_CLOSE);
        hardware.ballDoor2.setPosition(CompBotHardware.BALL_DOOR2_CLOSE);
        hardware.itsOver.setPosition(CompBotHardware.ITS_NOT_OVER);
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
        hardware.flwheel.setPower(capVal(capVal(x + y) + r));
        hardware.frwheel.setPower(capVal(capVal(-x + y) - r));
        hardware.blwheel.setPower(capVal(capVal(-x + y) + r));
        hardware.brwheel.setPower(capVal(capVal(x + y) - r));

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
            hardware.buttonPusher.setPosition(CompBotHardware.BUTTON_PUSHER_DOWN);
        }
        else if (gamepad1.dpad_up)
        {
            hardware.buttonPusher.setPosition(CompBotHardware.BUTTON_PUSHER_UP);
        }

        if (gamepad1.x)
        {
            hardware.lForkHolder.setPosition(CompBotHardware.L_FORK_RELEASE);
            hardware.rForkHolder.setPosition(CompBotHardware.R_FORK_RELEASE);
        }
        else if (gamepad1.b)
        {
            hardware.lForkHolder.setPosition(CompBotHardware.L_FORK_HOLD);
            hardware.rForkHolder.setPosition(CompBotHardware.R_FORK_HOLD);
        }
        /*
        Gamepad 2
         */

        if (gamepad2.x)
            hardware.ballDoor.setPosition(CompBotHardware.BALL_DOOR_OPEN);
        else if (gamepad2.b)
            hardware.ballDoor2.setPosition(CompBotHardware.BALL_DOOR2_OPEN);
        else
        {
            if (gamepad1.a)
            {
                hardware.ballDoor.setPosition(CompBotHardware.BALL_DOOR_OPEN);
                gate2 = false;

                //gateTimer = runtime.milliseconds();
            } else// if (runtime.milliseconds() - gateTimer > GATE_TIME)
            {
                hardware.ballDoor.setPosition(CompBotHardware.BALL_DOOR_CLOSE);
                gate2 = true;
            }

            if (!old_gate2 && gate2)
            {
                hardware.ballDoor2.setPosition(CompBotHardware.BALL_DOOR2_OPEN);
                gate2Timer = runtime.milliseconds();
            } else if (runtime.milliseconds() - gate2Timer > GATE2_TIME)
            {
                hardware.ballDoor2.setPosition(CompBotHardware.BALL_DOOR2_CLOSE);
            }
        }

        if (gamepad2.a)
            hardware.itsOver.setPosition(CompBotHardware.ITS_OVER);
        else
            hardware.itsOver.setPosition(CompBotHardware.ITS_NOT_OVER);

        old_a = gamepad1.a;
        old_gate2 = gate2;

        telemetry.addData("Jyostick", gamepad1.right_stick_x);
        telemetry.update();
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
        if (input < 0.15 && input > -0.15)
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

}
