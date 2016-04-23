package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by BenL on 12/28/15.
 */
public class Robot2Hardware extends OpMode {

    public enum RobotDirection {INTAKE, WINCH}
    public enum ColorComponent {RED, GREEN, BLUE}

    // Controllers
    private DcMotorController[] motorControlrs;

    private ServoController   servoControlr;

    private DeviceInterfaceModule interfaceModule;

    // Motors
    private HashMap<String, DcMotor> nonEncodedMotors;
    private HashMap<String, DcMotor> encodedMotors;
    private HashMap<String, Double> motorPower;
    private HashMap<String, Integer> motorPos;

    // Servos
    private HashMap<String, Servo> servos;
    private HashMap<String, Double> servoPos;

    // Sensors
    private ColorSensor colorSensor;

    // Miscellaneous
    private HashSet<String> unmappedComponents;
    private HashSet<String> otherErrors;
    private boolean driveEncoders;
    private RobotDirection driveDirection;

    // Constants
    protected final boolean RIGHT = true;
    protected final boolean LEFT = false;
    private final int ENCODER_THRESHOLD = 10;

    public Robot2Hardware(boolean driveEncoders)
    {
        this.driveEncoders = driveEncoders;
    }

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

        // Interface Module
        try
        {
            interfaceModule = hardwareMap.deviceInterfaceModule.get("InterfaceModule");
        }
        catch (Exception e)
        {
            unmappedComponents.add("InterfaceModule");
            //DbgLog.logStacktrace(e);
        }
        
        // Motors
        if (driveEncoders)
        {
            motorPower.put("frontLeftDrive", 0.5);
            motorPos.put("frontLeftDrive", 0);

            motorPower.put("backLeftDrive", 0.5);
            motorPos.put("backLeftDrive", 0);

            motorPower.put("frontRightDrive", 0.5);
            motorPos.put("frontRightDrive", 0);

            motorPower.put("backRightDrive", 0.5);
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
        motorPower.put("winch", 0.0);

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

        //
        if (!driveEncoders)
        {
            nonEncodedMotors.get("frontLeftDrive").setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            nonEncodedMotors.get("frontRightDrive").setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            nonEncodedMotors.get("backLeftDrive").setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
            nonEncodedMotors.get("backRightDrive").setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        }
        
        // Servos

        servoPos.put("peoplePutter", 0.0);
        servoPos.put("winchAngle", 0.0);
        servoPos.put("claws", 0.0);
        servoPos.put("leftTrigger", 0.0);
        servoPos.put("rightTrigger", 0.0);

        try
        {
            servos.put("peoplePutter", hardwareMap.servo.get("peoplePutter"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("peoplePutter");
        }

        try
        {
            servos.put("winchAngle", hardwareMap.servo.get("winchAngle"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("winchAngle");
        }

        try
        {
            servos.put("claws", hardwareMap.servo.get("claws"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("claws");
        }

        try
        {
            servos.put("leftTrigger", hardwareMap.servo.get("leftTrigger"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("leftTrigger");
        }

        try
        {
            servos.put("rightTrigger", hardwareMap.servo.get("rightTrigger"));
        }
        catch (Exception e)
        {
            unmappedComponents.add("rightTrigger");
        }

        /*
         *  Sensors
         */

        interfaceModule.setDigitalChannelMode(5, DigitalChannelController.Mode.OUTPUT);

        // Turn off LED
        interfaceModule.setDigitalChannelState(5, false);

        try
        {
            colorSensor = hardwareMap.colorSensor.get("colorSensor");
        }
        catch (Exception e)
        {
            unmappedComponents.add("colorSensor");
        }
    }

    // Getters
    public HashSet<String> getUnmappedComponents() {return unmappedComponents;}
    public HashSet<String> getOtherErrors() {return otherErrors;}

    public int getColor(ColorComponent colorComponent)
    {
        switch (colorComponent)
        {
            case RED:
                return colorSensor.red();
            case GREEN:
                return colorSensor.green();
            case BLUE:
                return colorSensor.blue();
            default:
                return 0;
        }
    }
    public HashMap<String, Double>  getServoPos()                   {return servoPos;}
    public double                   getServoCurrentPos(String servoName)
    {
        return servos.get(servoName).getPosition();
    }
    public double                   getServoPos(String servoName)   {return servoPos.get(servoName);}
    public RobotDirection           getDriveDirection()             {return driveDirection;}
    public int                      getMotorTarget(String motorName){return motorPos.get(motorName);}
    public int getMotorPosition(String motorName)
    {
        int pos;
        try
        {
            pos = nonEncodedMotors.get(motorName).getCurrentPosition();
        }
        catch (Exception e)
        {
            try
            {
                pos = encodedMotors.get(motorName).getCurrentPosition();
            }
            catch (Exception e1)
            {
                pos = 0;
                otherErrors.add("COULD NOT GET POSITION OF '" + motorName + "'");
            }
        }
        return pos;
    }

    public boolean isDriveDone()
    {
        int motorsDone = 0;
        for (int i = 0; i < 4; i++)
        {
            DcMotor motor = encodedMotors.get("frontLeftDrive"); // because the compiler thinks it won't be initialized
            switch (i)
            {
                case 0:
                    motor = encodedMotors.get("frontLeftDrive");
                    break;
                case 1:
                    motor = encodedMotors.get("frontRightDrive");
                    break;
                case 2:
                    motor = encodedMotors.get("backLeftDrive");
                    break;
                case 3:
                    motor = encodedMotors.get("backRightDrive");
                    break;
            }
            if (motor.getCurrentPosition() < motor.getTargetPosition()+ENCODER_THRESHOLD
                    && motor.getCurrentPosition() > motor.getTargetPosition()-ENCODER_THRESHOLD)
                motorsDone++;

        }
        if (motorsDone >= 4)
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

    protected void resetEncoders()
    {
        Set encodedSet = encodedMotors.entrySet();
        Iterator encodedIterator = encodedSet.iterator();

        while (encodedIterator.hasNext())
        {
            Map.Entry motorEntry = (Map.Entry) encodedIterator.next();

            try
            {
                ((DcMotor) motorEntry.getValue()).setMode(DcMotorController.RunMode.RESET_ENCODERS);
            } catch (Exception e)
            {
                otherErrors.add("CANNOT RESET" + motorEntry.getKey());
            }
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
            if (motorName.contains("front"))
                degrees *= 10d/8d;
            int pos = (int) (degrees / 360 * 1440);

            motorPos.put(motorName, pos);
        }
        else
            otherErrors.add("COULD NOT SET MOTOR POS: " + motorName);
    }
    
    protected void setServoVal(String servoName, double pos)
    {
        if (pos < 0)
            pos = 0;
        else if (pos > 1)
            pos = 1;

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
    public void init()
    {

    }


    @Override
    public void start()
    {
        setDriveDirection(RobotDirection.INTAKE);
        resetEncoders();
    }

    @Override
    public void loop()
    {
        // Update Motor Positions
        Set nonEncodedSet = nonEncodedMotors.entrySet();
        Iterator nonEncodedIterator = nonEncodedSet.iterator();

        while (nonEncodedIterator.hasNext())
        {
            Map.Entry motorEntry = (Map.Entry) nonEncodedIterator.next();

            try
            {
                ((DcMotor) motorEntry.getValue()).setPower(motorPower.get(motorEntry.getKey()));
            } catch (Exception e)
            {
                otherErrors.add("CANNOT SET '" + motorEntry.getKey() + "' Value");
            }
        }

        Set encodedSet = encodedMotors.entrySet();
        Iterator encodedIterator = encodedSet.iterator();

        while (encodedIterator.hasNext())
        {
            Map.Entry motorEntry = (Map.Entry) encodedIterator.next();

            try
            {
                // Check if Motor is reset
                if (((DcMotor) motorEntry.getValue()).getMode() == DcMotorController.RunMode.RESET_ENCODERS
                        && ((DcMotor) motorEntry.getValue()).getCurrentPosition() == 0)
                    ((DcMotor) motorEntry.getValue()).setMode(DcMotorController.RunMode.RUN_TO_POSITION);

                // Set Position
                ((DcMotor) motorEntry.getValue()).setTargetPosition(motorPos.get(motorEntry.getKey()));
            } catch (Exception e)
            {
                otherErrors.add("CANNOT SET '" + motorEntry.getKey() + "' Value");
            }
        }

        // Check Servo Limits
        double winchAng = servoPos.get("winchAngle");
        if (winchAng > 0.75)
            winchAng = 0.75;
        else if (winchAng < 0.15)
            winchAng = 0.15;
        servoPos.put("winchAngle", winchAng);

        // Update Servo Positions (servos must be sent a position, otherwise they will move to default)

        servos.get("peoplePutter").setPosition(servoPos.get("peoplePutter"));
        servos.get("winchAngle").setPosition(servoPos.get("winchAngle"));
        servos.get("claws").setPosition(servoPos.get("claws"));
        servos.get("leftTrigger").setPosition(servoPos.get("leftTrigger"));
        servos.get("rightTrigger").setPosition(servoPos.get("rightTrigger"));
    }

    @Override
    public void stop()
    {
        resetEncoders();
    }
}
