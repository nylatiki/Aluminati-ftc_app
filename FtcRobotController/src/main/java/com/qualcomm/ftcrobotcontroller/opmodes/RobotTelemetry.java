package com.qualcomm.ftcrobotcontroller.opmodes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * Created by BenL on 12/30/15.
 */
public class RobotTelemetry extends RobotHardware
{
    private final int RESERVED_INDEX = 6;
    private HashSet<String> unmappedComponents;
    private LinkedHashMap<String, String> telemetryData;

    public RobotTelemetry(boolean driveEncoders)
    {
        super(driveEncoders);
    }

    public void initTelemetry()
    {
        initComponents();
        telemetryData = new LinkedHashMap<String, String>();
        unmappedComponents = getUnmappedComponents();
    }

    public void updateTelemetry()
    {
        // Display other errors
        // Make sure the errors take precedence in the display
        for (String error : getOtherErrors())
        {
            telemetry.addData("000: ERROR:", error);
        }

        // Create Warning if required components cannot be mapped
        if (!unmappedComponents.isEmpty())
        {
            String finalWarning = "";
            for (String component : unmappedComponents)
            {
                finalWarning += component;
                finalWarning += ", ";
            }
            finalWarning = finalWarning.substring(0, finalWarning.length() - 2);
            telemetry.addData("00: ", "COULD NOT FIND: " + finalWarning);
        }

        // Servo Telemetry
        /*
        0: leftBox
        1: rightBox
        2: leftClaw
        3: rightClaw
        4: leftBrush
        5: rightBrush
        */

        Set servoPosSet = getServoPos().entrySet();
        Iterator servoPosIterator = servoPosSet.iterator();

        // Servo Telemetry
        int servoPosIndex = 1;
        while (servoPosIterator.hasNext())
        {
            Map.Entry posEntry = (Map.Entry) servoPosIterator.next();

            telemetry.addData("0" + servoPosIndex + ": ", posEntry.getKey() + ": " + posEntry.getValue());
            servoPosIndex++;
        }

        // Variable Telemetry
        Set telemetrySet = telemetryData.entrySet();
        Iterator telemetryIterator = telemetrySet.iterator();

        int teleIndex = RESERVED_INDEX + 1;
        while (telemetryIterator.hasNext())
        {
            Map.Entry teleEntry = (Map.Entry) telemetryIterator.next();

            String teleIndexString = String.valueOf(teleIndex);
            if (teleIndex < 10) {
                teleIndexString = "0" + teleIndexString;
            }
            telemetry.addData(teleIndexString + ": ", teleEntry.getKey() + (String) teleEntry.getValue());
            teleIndex++;
        }
    }

    public void addTelemetry(String index, String data)
    {
        telemetryData.put(index, data);
    }
}
