package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Created by benlimpa on 1/25/17.
 */

@Autonomous(name="New Blue", group="CompBot")
public class NewBlueAuto extends CompBotAutoNew
{
    @Override
    public void runOpMode() throws InterruptedException
    {
        super.initVariation(FieldColor.BLUE);
        super.runOpMode();
    }
}
