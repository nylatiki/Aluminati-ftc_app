package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Created by Wshoe on 9/4/16.
 */
public class STCTeleSketch extends OpMode
{
    DcMotor motorfr,motorbr,motorfl,motorbl,motorrs,motorls;
    public void init()
    {
        motorfr=hardwareMap.dcMotor.get("motorfr");
        motorbr=hardwareMap.dcMotor.get("motorbr");
        motorfr.setDirection(DcMotor.Direction.REVERSE);
        motorbr.setDirection(DcMotor.Direction.REVERSE);
        motorfl=hardwareMap.dcMotor.get("motorfl");
        motorbl=hardwareMap.dcMotor.get("motorbl");
        motorrs=hardwareMap.dcMotor.get("motorrs");
        motorls=hardwareMap.dcMotor.get("motorls");
    }
    public void start()
    {

    }
    public void loop()
    {
        double deadband = 0.1;
        /*
        motor[frontRight] = Y1 - X2 - X1;
        motor[backRight] =  Y1 - X2 + X1;
        motor[frontLeft] = Y1 + X2 + X1;
        motor[backLeft] =  Y1 + X2 - X1;
        -gamepad1.left_stick_y
         */
        float ay_lmao = gamepad1.right_stick_x;
        if((Math.abs(gamepad1.left_stick_x)<=deadband && Math.abs(-gamepad1.left_stick_y)<=deadband))
        {
            motorfr.setPower(0);
            motorbr.setPower(0);
            motorfl.setPower(0);
            motorbl.setPower(0);
        }
        else {
            if (Math.abs(gamepad1.right_stick_x) <= deadband)
            {
                ay_lmao = 0;
            }

            float fr = -gamepad1.left_stick_y - ay_lmao - gamepad1.left_stick_x;
            float br = -gamepad1.left_stick_y - ay_lmao + gamepad1.left_stick_x;
            float fl = -gamepad1.left_stick_y + ay_lmao + gamepad1.left_stick_x;
            float bl = -gamepad1.left_stick_y + ay_lmao - gamepad1.left_stick_x;
            if (Math.abs(fr) <= 1) //front right wheel
            {
                motorfr.setPower(fr);
            }
            if ((fr) > 1) {
                motorfr.setPower(1);
            }
            if ((fr) < -1) {
                motorfr.setPower(-1);
            }

            if (Math.abs(br) <= 1) //back right wheel
            {
                motorbr.setPower(br);
            }
            if ((br) > 1) {
                motorbr.setPower(1);
            }
            if ((br) < -1) {
                motorbr.setPower(-1);
            }

            if (Math.abs(fl) <= 1) //front left wheel
            {
                motorfl.setPower(fl);
            }
            if ((fl) > 1) {
                motorfl.setPower(1);
            }
            if ((fl) < -1) {
                motorfl.setPower(-1);
            }

            if (Math.abs(bl) <= 1) //back left wheel
            {
                motorbl.setPower(bl);
            }
            if ((bl) > 1)
            {
                motorbl.setPower(1);
            }
            if ((bl) < -1)
            {
                motorbl.setPower(-1);
            }

        }
        if (gamepad1.a)
        {
            motorrs.setPower(1);
            motorls.setPower(1);
        }
        else
        {
            motorrs.setPower(0);
            motorls.setPower(0);
        }
        if (gamepad1.y)
        {
            motorrs.setPower(-1);
            motorls.setPower(-1);
        }
        else
        {
            motorrs.setPower(0);
            motorls.setPower(0);
        }
        if(gamepad1.x)
        {

        }
    }
}
