package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

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

    public DcMotorController forkController;
    public DcMotor frwheel;
    public DcMotor flwheel;
    public DcMotor brwheel;
    public DcMotor blwheel;

    public DcMotor intake;
    public DcMotor shooter;

    public DcMotor rFork;
    public DcMotor lFork;

    public DeviceInterfaceModule dim;

    public DigitalChannel frontIR;
    public DigitalChannel backIR;

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

        intake = hardwareMap.dcMotor.get("intake");
        shooter = hardwareMap.dcMotor.get("shooter");

        rFork = hardwareMap.dcMotor.get("rFork");
        lFork = hardwareMap.dcMotor.get("lFork");

        dim = hardwareMap.deviceInterfaceModule.get("dim");

        //frontIR = dim.get
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
