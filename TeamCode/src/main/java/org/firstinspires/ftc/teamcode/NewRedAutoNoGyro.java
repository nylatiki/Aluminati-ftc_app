package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Created by benlimpa on 1/25/17.
 */

@Autonomous(name="New Red No Gyro", group="CompBot")
public class NewRedAutoNoGyro extends CompBotAutoNewNoGyro
{
    @Override
    public void runOpMode() throws InterruptedException
    {
        super.initVariation(FieldColor.RED);
        super.runOpMode();
    }
}
