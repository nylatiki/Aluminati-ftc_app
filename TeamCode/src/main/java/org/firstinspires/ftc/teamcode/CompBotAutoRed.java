package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Created by benlimpa on 12/2/16.
 */

@Autonomous(name="Red Autonomous", group="CompBot")
public class CompBotAutoRed extends CompBotAuto
{
    @Override
    public void runOpMode() throws InterruptedException
    {
        super.initVariation(FieldColor.RED);
        super.runOpMode();
    }
}
