package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

/**
 * Created by benlimpa on 10/23/16.
 */

public class CompBotHardware
{
    // Constants
    public enum BotDirection {INTAKE_FRONT, SENSOR_FRONT}

    static final double BUTTON_PUSHER_DOWN  = 0.42;
    static final double BUTTON_PUSHER_UP    = 0.1;

    static final double L_FORK_RELEASE  = 0.2;
    static final double L_FORK_HOLD     = 0.8;

    static final double R_FORK_RELEASE  = 0.6;
    static final double R_FORK_HOLD     = 0.0;

    static final double BALL_DOOR_CLOSE = 0.6;
    static final double BALL_DOOR_OPEN  = 0.5;

    static final double BALL_DOOR2_CLOSE = 0.2;
    static final double BALL_DOOR2_OPEN  = 0.4;

    static final double ITS_NOT_OVER = 0.15;
    static final double ITS_OVER = 0.43;

    // Hardware
    private DcMotor wheel1;
    private DcMotor wheel2;
    private DcMotor wheel3;
    private DcMotor wheel4;

    public DcMotor[] wheels;

    public DcMotorController forkController;
    public DcMotor frwheel;
    public DcMotor flwheel;
    public DcMotor brwheel;
    public DcMotor blwheel;

    public DcMotor intake;
    public DcMotor shooter;

    public Servo buttonPusher;
    public Servo lForkHolder;
    public Servo rForkHolder;
    public Servo ballDoor;
    public Servo ballDoor2;
    public Servo itsOver;

    public DeviceInterfaceModule dim;

    public DigitalChannel centerIR;

    public ColorSensor colorSensor;

    public UltrasonicSensor lUltra;
    public UltrasonicSensor rUltra;

    private BotDirection direction;

    public CompBotHardware()
    {

    }

    public void init(HardwareMap hardwareMap)
    {
        forkController = hardwareMap.dcMotorController.get("forkController");

        wheel1 = hardwareMap.dcMotor.get("w1");
        wheel2 = hardwareMap.dcMotor.get("w2");
        wheel3 = hardwareMap.dcMotor.get("w3");
        wheel4 = hardwareMap.dcMotor.get("w4");

        wheels = new DcMotor[] {wheel1, wheel2, wheel3, wheel4};

        intake = hardwareMap.dcMotor.get("intake");
        shooter = hardwareMap.dcMotor.get("shooter");

        buttonPusher = hardwareMap.servo.get("buttonPusher");
        lForkHolder = hardwareMap.servo.get("lForkHolder");
        rForkHolder = hardwareMap.servo.get("rForkHolder");
        ballDoor = hardwareMap.servo.get("ballDoor");
        ballDoor2 = hardwareMap.servo.get("ballDoor2");
        itsOver = hardwareMap.servo.get("itsOver");

        dim = hardwareMap.deviceInterfaceModule.get("dim");

        switchDirection(BotDirection.INTAKE_FRONT);

        colorSensor = hardwareMap.colorSensor.get("color");
        centerIR = hardwareMap.digitalChannel.get("centerIR");
        dim.setDigitalChannelMode(0, DigitalChannelController.Mode.INPUT);
        lUltra = hardwareMap.ultrasonicSensor.get("lUltra");
        rUltra = hardwareMap.ultrasonicSensor.get("rUltra");
    }

    public void switchDirection(BotDirection direction)
    {
        this.direction = direction;
        switch (direction)
        {
            case INTAKE_FRONT:
                flwheel = wheel4;
                frwheel = wheel3;
                blwheel = wheel2;
                brwheel = wheel1;
                break;
            case SENSOR_FRONT:
                flwheel = wheel1;
                frwheel = wheel2;
                blwheel = wheel3;
                brwheel = wheel4;
                break;
        }
        flwheel.setDirection(DcMotorSimple.Direction.REVERSE);
        blwheel.setDirection(DcMotorSimple.Direction.REVERSE);
        frwheel.setDirection(DcMotorSimple.Direction.FORWARD);
        brwheel.setDirection(DcMotorSimple.Direction.FORWARD);
    }
}
