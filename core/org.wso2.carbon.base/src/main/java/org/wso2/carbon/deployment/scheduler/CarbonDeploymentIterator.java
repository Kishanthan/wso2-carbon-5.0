package org.wso2.carbon.deployment.scheduler;


import java.util.Calendar;
import java.util.Date;

public class CarbonDeploymentIterator {

    private Calendar calendar = Calendar.getInstance();

    public Date next() {
        calendar.add(Calendar.SECOND, 10);

        return calendar.getTime();
    }
}
