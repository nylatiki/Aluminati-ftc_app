package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.hitechnic.HiTechnicNxtUltrasonicSensor;
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
    public enum BotDirection {INTAKE_FRONT, SENSOR_FRONT}
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

    public DcMotor rFork;
    public DcMotor lFork;

    public Servo buttonPusher;
    public Servo lForkHolder;
    public Servo rForkHolder;
    public Servo brushy;
    public Servo ballDoor;

    public DeviceInterfaceModule dim;

    public DigitalChannel centerIR;

    public ColorSensor colorSensor;

    public UltrasonicSensor ultrasonic;

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

        rFork = hardwareMap.dcMotor.get("rFork");
        lFork = hardwareMap.dcMotor.get("lFork");

        buttonPusher = hardwareMap.servo.get("buttonPusher");
        lForkHolder = hardwareMap.servo.get("lForkHolder");
        rForkHolder = hardwareMap.servo.get("rForkHolder");
        brushy = hardwareMap.servo.get("brushy");
        ballDoor = hardwareMap.servo.get("ballDoor");

        dim = hardwareMap.deviceInterfaceModule.get("dim");

        switchDirection(BotDirection.INTAKE_FRONT);

        colorSensor = hardwareMap.colorSensor.get("color");
        centerIR = hardwareMap.digitalChannel.get("centerIR");
        dim.setDigitalChannelMode(0, DigitalChannelController.Mode.INPUT);
        ultrasonic = hardwareMap.ultrasonicSensor.get("ultrasonic");
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
