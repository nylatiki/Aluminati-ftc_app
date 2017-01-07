package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Created by benlimpa on 12/2/16.
 */

@Autonomous(name="Blue Autonomous", group="CompBot")
public class CompBotAutoBlue extends CompBotAuto
{
    @Override
    public void runOpMode() throws InterruptedException
    {
        super.initVariation(FieldColor.BLUE);
        super.runOpMode();
    }
}
