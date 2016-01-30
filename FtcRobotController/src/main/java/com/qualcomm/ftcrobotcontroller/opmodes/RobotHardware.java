package com.qualcomm.ftcrobotcontroller.opmodes;

import java.util.HashMap;
import java.util.HashSet;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

/**
 * Created by BenL on 12/28/15.
 */
public class RobotHardware extends OpMode {

    // Controllers
    private DcMotorController[] motorControlrs;

    private ServoController   servoControlr;

    // Motors
    private HashMap<String, DcMotor> nonEncodedMotors;
    private HashMap<String, DcMotor> encodedMotors;
    private HashMap<String, Double> motorPower;
    private HashMap<String, Integer> motorPos;

    // Servos
    private HashMap<String, Servo> servos;
    private HashMap<String, Double> servoPos;

    // Miscellaneous
    private HashSet<String> unmappedComponents;
    private HashSet<String> otherErrors;
    private boolean driveEncoders;
    
    // Constants
    protected final boolean RIGHT = true;
    protected final boolean LEFT = false;

    protected final double L_BOX_UP = 0;
    protected final double L_BOX_DOWN = 0.6;
    protected final double R_BOX_UP = 0.6;
    protected final double R_BOX_DOWN = 0;

    protected final double L_CLAW_UP = 0.1;
    protected final double L_CLAW_DOWN = 0.7;
    protected final double R_CLAW_UP = 1;
    protected final double R_CLAW_DOWN = 0.35;

    protected final double L_BRUSH_BAR_UP = 0.25;
    protected final double L_BRUSH_BAR_MIDDLE = 0.625;
    protected final double L_BRUSH_BAR_DOWN = 1.0;
    protected final double R_BRUSH_BAR_UP = 0.50;
    protected final double R_BRUSH_BAR_MIDDLE = 0.325;
    protected final double R_BRUSH_BAR_DOWN = 0.15;

    public RobotHardware(boolean driveEncoders)
    {
        this.driveEncoders = driveEncoders;
    }

    @Override
    public void init(){} // does nothing

    public void initComponents()
    {
        unmappedComponents = new HashSet<String>();
        otherErrors = new HashSet<String>();

        nonEncodedMotors = new HashMap<String, DcMotor>();
        encodedMotors = new HashMap<String, DcMotor>();
        motorPower = new HashMap<String, Double>();
        motorPos = new HashMap<String, Integer>();

        servos = new HashMap<String, Servo>();
        servoPos = new HashMap<String, Double>();

        //
        // Map Robot Components
        //
        
        // Motor Controllers
        motorControlrs = new DcMotorController[4];
        for (int i = 0; i < 4; i++) 
        {
            try
            {
                motorControlrs[i] = hardwareMap.dcMotorController.get("MotorControl" + (i + 1));
            }
            catch (Exception e)
            {
                unmappedComponents.add("MotorControl" + (i + 1));
                //DbgLog.logStacktrace(e);
            }
            
            // Set Device Mode
            if (motorControlrs[i] != null)
                motorControlrs[i].setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
        }
        
        // Servo Controllers
        try
        {
            servoControlr = hardwareMap.servoController.get("ServoControl");
        }
        catch (Exception e)
        {
            unmappedComponents.add("ServoControl");
            //DbgLog.logStacktrace(e);
        }
        
        // Motors
        if (driveEncoders)
        {
            motorPower.put("leftDrive", 1.0);
            motorPos.put("leftDrive", 0);

            motorPower.put("rightDrive", 1.0);
            motorPos.put("rightDrive", 0);
        }
        else
        {
            motorPower.put("leftDrive", 0.0);
            motorPower.put("rightDrive", 0.0);
        }
        motorPower.put("pimpWheel", 1.0);
        motorPos.put("pimpWheel", 0);

        motorPower.put("leftSpool", 0.0);
        motorPower.put("rightSpool", 0.0);

        motorPower.put("leftRangle", 0.0);
        motorPower.put("rightRangle", 0.0);

        motorPower.put("brush", 0.0);

        try
        {
            if (driveEncoders)
            {
                encodedMotors.put("leftDrive", hardwareMap.dcMotor.get("leftDrive"));

                if (encodedMotors.get("leftDrive") != null)
                {
                    encodedMotors.get("leftDrive").setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
                    encodedMotors.get("leftDrive").setPower(1);
                }
            }
            else
            {
                nonEncodedMotors.put("leftDrive", hardwareMap.dcMotor.get("leftDrive"));
            }

        }
        catch (Exception e)
        {
            unmappedComponents.add("leftDrive");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            if (driveEncoders) {
                encodedMotors.put("rightDrive", hardwareMap.dcMotor.get("rightDrive"));

                DcMotor rightDrive = encodedMotors.get("rightDrive");
                if (rightDrive != null)
                {
                    rightDrive.setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
                    rightDrive.setPower(1);
                    rightDrive.setDirection(DcMotor.Direction.REVERSE);
                    encodedMotors.put("rightDrive", rightDrive);
                }
            }
            else
            {
                nonEncodedMotors.put("rightDrive", hardwareMap.dcMotor.get("rightDrive"));

                DcMotor rightDrive = encodedMotors.get("rightDrive");
                if (rightDrive != null)
                {
                    rightDrive.setDirection(DcMotor.Direction.REVERSE);
                    encodedMotors.put("rightDrive", rightDrive);
                }
            }
        }
        catch (Exception e)
        {
            unmappedComponents.add("rightDrive");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            encodedMotors.put("pimpWheel", hardwareMap.dcMotor.get("pimpWheel"));

            if (encodedMotors.get("pimpWheel") != null)
            {
                encodedMotors.get("pimpWheel").setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
                encodedMotors.get("pimpWheel").setPower(1);
            }
        }
        catch (Exception e)
        {
            unmappedComponents.add("pimpWheel");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            nonEncodedMotors.put("leftSpool", hardwareMap.dcMotor.get("leftSpool"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("leftSpool");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            nonEncodedMotors.put("rightSpool", hardwareMap.dcMotor.get("rightSpool"));

            if (nonEncodedMotors.get("rightSpool") != null)
                nonEncodedMotors.get("rightSpool").setDirection(DcMotor.Direction.REVERSE);
        }
        catch (Exception e)
        {
            unmappedComponents.add("rightSpool");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            nonEncodedMotors.put("leftRangle", hardwareMap.dcMotor.get("leftRangle"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("leftRangle");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            nonEncodedMotors.put("rightRangle", hardwareMap.dcMotor.get("rightRangle"));

            if (nonEncodedMotors.get("rightRangle") != null)
                nonEncodedMotors.get("rightRangle").setDirection(DcMotor.Direction.REVERSE);
        }
        catch (Exception e)
        {
            unmappedComponents.add("rightRangle");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            nonEncodedMotors.put("brush", hardwareMap.dcMotor.get("brush"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("brush");
            //DbgLog.logStacktrace(e);
        }
        
        // Servos

        servoPos.put("leftBox", L_BOX_UP);
        servoPos.put("rightBox", R_BOX_UP);
        servoPos.put("leftBrushArm", L_BRUSH_BAR_UP);
        servoPos.put("rightBrushArm", R_BRUSH_BAR_UP);
        servoPos.put("leftClaw", L_CLAW_UP);
        servoPos.put("rightClaw", R_CLAW_UP);

        try
        {
            servos.put("leftBox", hardwareMap.servo.get("leftBoxServo"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("leftBoxServo");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            servos.put("rightBox", hardwareMap.servo.get("rightBoxServo"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("rightBoxServo");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            servos.put("leftBrushArm", hardwareMap.servo.get("leftBrushArmServo"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("leftBrushArmServo");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            servos.put("rightBrushArm", hardwareMap.servo.get("rightBrushArmServo"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("rightBrushArmServo");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            servos.put("leftClaw", hardwareMap.servo.get("leftClawServo"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("leftClawServo");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            servos.put("rightClaw", hardwareMap.servo.get("rightClawServo"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("rightClawServo");
            //DbgLog.logStacktrace(e);
        }
    }

    // Getters
    public HashSet<String> getUnmappedComponents() {return unmappedComponents;}
    public HashSet<String> getOtherErrors() {return otherErrors;}
    public HashMap<String, Double> getServoPos() {return servoPos;}
    protected boolean isControllerModeChanged(int controllerIndex)
    {
        if (controllerIndex == -1)
        {
            DcMotorController.DeviceMode devMode1 = motorControlrs[0].getMotorControllerDeviceMode();
            DcMotorController.DeviceMode devMode2 = motorControlrs[2].getMotorControllerDeviceMode();

            if (devMode1 == DcMotorController.DeviceMode.READ_ONLY
                    || devMode1 == DcMotorController.DeviceMode.WRITE_ONLY)
            {
                if (devMode2 == DcMotorController.DeviceMode.READ_ONLY
                        || devMode2 == DcMotorController.DeviceMode.WRITE_ONLY)
                {
                    return true;
                }
                else
                    return false;
            }
            else
                return false;
        }
        else
        {
            DcMotorController.DeviceMode devMode = motorControlrs[controllerIndex].getMotorControllerDeviceMode();
            if (devMode == DcMotorController.DeviceMode.READ_ONLY
                    || devMode == DcMotorController.DeviceMode.WRITE_ONLY)
                return true;
            else
                return false;
        }
    }

    protected boolean isDriveRunning()
    {
        if (motorControlrs[0].getMotorControllerDeviceMode() == DcMotorController.DeviceMode.READ_ONLY
                && motorControlrs[2].getMotorControllerDeviceMode() == DcMotorController.DeviceMode.READ_ONLY)
        {
            if (encodedMotors.get("leftDrive").isBusy() || encodedMotors.get("rightDrive").isBusy())
                return true;
            else
                return false;
        }
        else
            return false;
    }
    
    // Setters
    
    protected void setNonEncodedMotorPower(String motorName, double power)
    {
        if (motorPower.get(motorName) != null)
            motorPower.put(motorName, power);
        else
            otherErrors.add("COULD NOT SET MOTOR POWER: " + motorName);
    }

    protected void setEncodedMotorPos(String motorName, int pos)
    {
        if (motorPos.get(motorName) != null) // pls work
            motorPos.put(motorName, pos);
        else
            otherErrors.add("COULD NOT SET MOTOR POS: " + motorName);
    }
    
    protected void setServoVal(String servoName, double pos)
    {
        if (servoPos.get(servoName) != null)
            servoPos.put(servoName, pos);
        else
            otherErrors.add("COULD NOT SET SERVO POS: " + servoName);
    }

    protected void setControllerMode(int controllerIndex, DcMotorController.DeviceMode deviceMode)
    {
        if (controllerIndex == -1)
        {
            motorControlrs[0].setMotorControllerDeviceMode(deviceMode);
            motorControlrs[2].setMotorControllerDeviceMode(deviceMode);
        }
        else
            motorControlrs[controllerIndex].setMotorControllerDeviceMode(deviceMode);
    }

    @Override
    public void start()
    {

    }

    @Override
    public void loop()
    {
        // Update Motor Positions
        if (driveEncoders)
        {
            encodedMotors.get("leftDrive").setTargetPosition(motorPos.get("leftDrive"));
            encodedMotors.get("rightDrive").setTargetPosition(motorPos.get("rightDrive"));
        }
        else
        {
            nonEncodedMotors.get("leftDrive").setPower(motorPower.get("leftDrive"));
            nonEncodedMotors.get("rightDrive").setPower(motorPower.get("rightDrive"));
        }
        encodedMotors.get("pimpWheel").setTargetPosition((int)Math.round(motorPos.get("pimpWheel")));

        nonEncodedMotors.get("leftRangle").setPower(motorPower.get("leftRangle"));
        nonEncodedMotors.get("rightRangle").setPower(motorPower.get("rightRangle"));

        nonEncodedMotors.get("leftSpool").setPower(motorPower.get("leftSpool"));
        nonEncodedMotors.get("rightSpool").setPower(motorPower.get("rightSpool"));

        nonEncodedMotors.get("brush").setPower(motorPower.get("brush"));

        // Update Servo Positions (servos must be sent a position, otherwise they will move to default)
        servos.get("leftBox").setPosition(servoPos.get("leftBox"));
        servos.get("rightBox").setPosition(servoPos.get("rightBox"));

        servos.get("leftClaw").setPosition(servoPos.get("leftClaw"));
        servos.get("rightClaw").setPosition(servoPos.get("rightClaw"));

        servos.get("leftBrushArm").setPosition(servoPos.get("leftBrushArm"));
        servos.get("rightBrushArm").setPosition(servoPos.get("rightBrushArm"));
    }
}
