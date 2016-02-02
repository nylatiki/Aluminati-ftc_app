package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by BenL on 12/28/15.
 */
public class Robot2Hardware extends OpMode {

    public enum RobotDirection {INTAKE, WINCH}

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
    private RobotDirection driveDirection;

    // Constants
    protected final boolean RIGHT = true;
    protected final boolean LEFT = false;

    public Robot2Hardware(boolean driveEncoders)
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
                motorControlrs[i].setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_WRITE);
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
            motorPower.put("frontLeftDrive", 1.0);
            motorPos.put("frontLeftDrive", 0);

            motorPower.put("backLeftDrive", 1.0);
            motorPos.put("backLeftDrive", 0);

            motorPower.put("frontRightDrive", 1.0);
            motorPos.put("frontRightDrive", 0);

            motorPower.put("backRightDrive", 1.0);
            motorPos.put("backRightDrive", 0);
        }
        else
        {
            motorPower.put("frontLeftDrive", 0.0);

            motorPower.put("backLeftDrive", 0.0);

            motorPower.put("frontRightDrive", 0.0);

            motorPower.put("backRightDrive", 0.0);
        }
        
        motorPower.put("brush", 0.0);
        motorPower.put("boxSlide", 1.0);
        motorPos.put("boxSlide", 0);

        try
        {
            if (driveEncoders)
            {
                encodedMotors.put("frontLeftDrive", hardwareMap.dcMotor.get("driveMotor4"));

                if (encodedMotors.get("frontLeftDrive") != null)
                {
                    encodedMotors.get("frontLeftDrive").setMode(DcMotorController.RunMode.RUN_TO_POSITION);
                    encodedMotors.get("frontLeftDrive").setPower(motorPower.get("frontLeftDrive"));
                }
            }
            else
            {
                nonEncodedMotors.put("frontLeftDrive", hardwareMap.dcMotor.get("driveMotor4"));
            }

        }
        catch (Exception e)
        {
            unmappedComponents.add("frontLeftDrive");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            if (driveEncoders)
            {
                encodedMotors.put("backLeftDrive", hardwareMap.dcMotor.get("driveMotor1"));

                if (encodedMotors.get("backLeftDrive") != null)
                {
                    encodedMotors.get("backLeftDrive").setMode(DcMotorController.RunMode.RUN_TO_POSITION);
                    encodedMotors.get("backLeftDrive").setPower(motorPower.get("backLeftDrive"));
                }
            }
            else
            {
                nonEncodedMotors.put("backLeftDrive", hardwareMap.dcMotor.get("driveMotor1"));
            }

        }
        catch (Exception e)
        {
            unmappedComponents.add("backLeftDrive");
            //DbgLog.logStacktrace(e);
        }
        
        try
        {
            if (driveEncoders) {
                encodedMotors.put("frontRightDrive", hardwareMap.dcMotor.get("driveMotor3"));

                DcMotor frontRightDrive = encodedMotors.get("frontRightDrive");
                if (frontRightDrive != null)
                {
                    frontRightDrive.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
                    frontRightDrive.setPower(motorPower.get("frontRightDrive"));
                    frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
                    encodedMotors.put("frontRightDrive", frontRightDrive);
                }
            }
            else
            {
                nonEncodedMotors.put("frontRightDrive", hardwareMap.dcMotor.get("driveMotor3"));

                DcMotor frontRightDrive = encodedMotors.get("frontRightDrive");
                if (frontRightDrive != null)
                {
                    frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
                    encodedMotors.put("frontRightDrive", frontRightDrive);
                }
            }
        }
        catch (Exception e)
        {
            unmappedComponents.add("frontRightDrive");
            //DbgLog.logStacktrace(e);
        }

        try
        {
            if (driveEncoders) {
                encodedMotors.put("backRightDrive", hardwareMap.dcMotor.get("driveMotor2"));

                DcMotor backRightDrive = encodedMotors.get("backRightDrive");
                if (backRightDrive != null)
                {
                    backRightDrive.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
                    backRightDrive.setPower(motorPower.get("backRightDrive"));
                    backRightDrive.setDirection(DcMotor.Direction.REVERSE);
                    encodedMotors.put("backRightDrive", backRightDrive);
                }
            }
            else
            {
                nonEncodedMotors.put("backRightDrive", hardwareMap.dcMotor.get("driveMotor2"));

                DcMotor backRightDrive = encodedMotors.get("backRightDrive");
                if (backRightDrive != null)
                {
                    backRightDrive.setDirection(DcMotor.Direction.REVERSE);
                    encodedMotors.put("backRightDrive", backRightDrive);
                }
            }
        }
        catch (Exception e)
        {
            unmappedComponents.add("backRightDrive");
            //DbgLog.logStacktrace(e);
        }
        
        try
        {
            nonEncodedMotors.put("brush", hardwareMap.dcMotor.get("brush"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("brush");
        }

        try
        {
            nonEncodedMotors.put("winch", hardwareMap.dcMotor.get("winch"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("winch");
        }

        try
        {
            encodedMotors.put("boxSlide", hardwareMap.dcMotor.get("boxSlide"));
            DcMotor boxSlide = encodedMotors.get("boxSlide");
            if (boxSlide != null)
            {
                boxSlide.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
                boxSlide.setPower(motorPower.get("boxSlide"));
            }
        }
        catch (Exception e)
        {
            unmappedComponents.add("boxSlide");
        }
        
        // Servos

        servoPos.put("peopleThrower", 0.0);

        try
        {
            servos.put("peopleThrower", hardwareMap.servo.get("peopleThrower"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("peopleThrower");
        }

    }

    // Getters
    public HashSet<String> getUnmappedComponents() {return unmappedComponents;}
    public HashSet<String> getOtherErrors() {return otherErrors;}
    public HashMap<String, Double> getServoPos() {return servoPos;}
    public RobotDirection getDriveDirection() {return driveDirection;}
    public int getMotorTarget(String motorName) {return motorPos.get(motorName);}

    public boolean isDriveBusy()
    {
        if (encodedMotors.get("frontLeftDrive").isBusy() || encodedMotors.get("frontRightDrive").isBusy()
                || encodedMotors.get("backLeftDrive").isBusy() || encodedMotors.get("backRightDrive").isBusy())
            return true;
        else
            return false;

    }
    
    // Setters
    
    protected void setDriveDirection(RobotDirection direction)
    {
        if (direction == RobotDirection.INTAKE)
        {
            if (driveEncoders)
            {
                encodedMotors.put("frontLeftDrive", hardwareMap.dcMotor.get("driveMotor4"));
                encodedMotors.put("frontRightDrive", hardwareMap.dcMotor.get("driveMotor3"));
                encodedMotors.put("backLeftDrive", hardwareMap.dcMotor.get("driveMotor1"));
                encodedMotors.put("backRightDrive", hardwareMap.dcMotor.get("driveMotor2"));
            }
            else
            {
                nonEncodedMotors.put("frontLeftDrive", hardwareMap.dcMotor.get("driveMotor4"));
                nonEncodedMotors.put("frontRightDrive", hardwareMap.dcMotor.get("driveMotor3"));
                nonEncodedMotors.put("backLeftDrive", hardwareMap.dcMotor.get("driveMotor1"));
                nonEncodedMotors.put("backRightDrive", hardwareMap.dcMotor.get("driveMotor2"));
            }
            
            driveDirection = RobotDirection.INTAKE;
        }
        else if (direction == RobotDirection.WINCH)
        {
            if (driveEncoders)
            {
                encodedMotors.put("frontLeftDrive", hardwareMap.dcMotor.get("driveMotor2"));
                encodedMotors.put("frontRightDrive", hardwareMap.dcMotor.get("driveMotor1"));
                encodedMotors.put("backLeftDrive", hardwareMap.dcMotor.get("driveMotor3"));
                encodedMotors.put("backRightDrive", hardwareMap.dcMotor.get("driveMotor4"));
            }
            else
            {
                nonEncodedMotors.put("frontLeftDrive", hardwareMap.dcMotor.get("driveMotor2"));
                nonEncodedMotors.put("frontRightDrive", hardwareMap.dcMotor.get("driveMotor1"));
                nonEncodedMotors.put("backLeftDrive", hardwareMap.dcMotor.get("driveMotor3"));
                nonEncodedMotors.put("backRightDrive", hardwareMap.dcMotor.get("driveMotor4"));
            }

            driveDirection = RobotDirection.WINCH;
        }
        
        if (driveEncoders)
        {
            encodedMotors.get("frontLeftDrive").setDirection(DcMotor.Direction.FORWARD);
            encodedMotors.get("frontRightDrive").setDirection(DcMotor.Direction.REVERSE);
            encodedMotors.get("backLeftDrive").setDirection(DcMotor.Direction.FORWARD);
            encodedMotors.get("backRightDrive").setDirection(DcMotor.Direction.REVERSE);
        }
        else
        {
            nonEncodedMotors.get("frontLeftDrive").setDirection(DcMotor.Direction.FORWARD);
            nonEncodedMotors.get("frontRightDrive").setDirection(DcMotor.Direction.REVERSE);
            nonEncodedMotors.get("backLeftDrive").setDirection(DcMotor.Direction.FORWARD);
            nonEncodedMotors.get("backRightDrive").setDirection(DcMotor.Direction.REVERSE);
        }
            
    }

    protected void resetEncoders(String motorName)
    {
        if (motorName.equals("driveMotors"))
        {
            encodedMotors.get("frontLeftDrive").setMode(DcMotorController.RunMode.RESET_ENCODERS);
            encodedMotors.get("frontRightDrive").setMode(DcMotorController.RunMode.RESET_ENCODERS);
            encodedMotors.get("backLeftDrive").setMode(DcMotorController.RunMode.RESET_ENCODERS);
            encodedMotors.get("backRightDrive").setMode(DcMotorController.RunMode.RESET_ENCODERS);
        }
        else
        {
            encodedMotors.get(motorName).setMode(DcMotorController.RunMode.RESET_ENCODERS);
        }
    }
    
    protected void setNonEncodedMotorPower(String motorName, double power)
    {
        if (motorPower.get(motorName) != null)
            motorPower.put(motorName, power);
        else
            otherErrors.add("COULD NOT SET MOTOR POWER: " + motorName);
    }

    protected void setMotorTarget(String motorName, double degrees)
    {
        if (motorPos.get(motorName) != null)
        {
            int pos = (int) (degrees / 360 * 1440);

            motorPos.put(motorName, pos);
        }
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
        setDriveDirection(RobotDirection.INTAKE);
        //resetEncoders("boxSlide");
    }

    @Override
    public void loop()
    {
        // Update Motor Positions
        if (driveEncoders)
        {
            encodedMotors.get("frontLeftDrive").setTargetPosition(motorPos.get("frontLeftDrive"));
            encodedMotors.get("frontRightDrive").setTargetPosition(motorPos.get("frontRightDrive"));
            encodedMotors.get("backLeftDrive").setTargetPosition(motorPos.get("backLeftDrive"));
            encodedMotors.get("backRightDrive").setTargetPosition(motorPos.get("backRightDrive"));
        }
        else
        {
            nonEncodedMotors.get("frontLeftDrive").setPower(motorPower.get("frontLeftDrive"));
            nonEncodedMotors.get("frontRightDrive").setPower(motorPower.get("frontRightDrive"));
            nonEncodedMotors.get("backLeftDrive").setPower(motorPower.get("backLeftDrive"));
            nonEncodedMotors.get("backRightDrive").setPower(motorPower.get("backRightDrive"));
        }

        encodedMotors.get("boxSlide").setTargetPosition(motorPos.get("boxSlide"));
        nonEncodedMotors.get("brush").setPower(motorPower.get("brush"));

        if (nonEncodedMotors.get("winch") != null)
            nonEncodedMotors.get("winch").setPower(motorPower.get("winch"));

        // Update Servo Positions (servos must be sent a position, otherwise they will move to default)
        servos.get("peopleThrower").setPosition(servoPos.get("peopleThrower"));
    }
}
