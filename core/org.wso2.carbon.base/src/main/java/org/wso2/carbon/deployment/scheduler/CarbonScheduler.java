package org.wso2.carbon.deployment.scheduler;


import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CarbonScheduler {
    private final Timer timer = new Timer(true);

    private void reschedule(CarbonSchedulerTask carbonSchedulerTask,
                            CarbonDeploymentIterator iterator) {
        Date time = iterator.next();

        if (time == null) {
            carbonSchedulerTask.cancel();
        } else {
            synchronized (carbonSchedulerTask.lock) {
                if (carbonSchedulerTask.state != CarbonSchedulerTask.CANCELLED) {
                    carbonSchedulerTask.timerTask = new SchedulerTimerTask(carbonSchedulerTask,
                                                                           iterator);
                    timer.schedule(carbonSchedulerTask.timerTask, time);
                }
            }
        }
    }

    /**
     * Schedules the specified task for execution according to the specified schedule.
     * If times specified by the <code>ScheduleIterator</code> are in the past they are
     * scheduled for immediate execution.
     *
     * @param carbonSchedulerTask task to be scheduled
     * @param iterator            iterator that describes the schedule
     * @throws IllegalStateException if task was already scheduled or cancelled,
     *                               carbonScheduler was cancelled, or carbonScheduler thread terminated.
     */
    public void schedule(CarbonSchedulerTask carbonSchedulerTask,
                         CarbonDeploymentIterator iterator) {
        Date time = iterator.next();

        if (time == null) {
            carbonSchedulerTask.cancel();
        } else {
            synchronized (carbonSchedulerTask.lock) {
                carbonSchedulerTask.state = CarbonSchedulerTask.SCHEDULED;
                carbonSchedulerTask.timerTask = new SchedulerTimerTask(carbonSchedulerTask,
                                                                       iterator);
                timer.schedule(carbonSchedulerTask.timerTask, time);
            }
        }
    }

    public void cleanup(CarbonSchedulerTask carbonSchedulerTask) {
        synchronized (carbonSchedulerTask.lock) {
            carbonSchedulerTask.state = CarbonSchedulerTask.CANCELLED;
            timer.cancel();
        }
    }

    public class SchedulerTimerTask extends TimerTask {
        private CarbonDeploymentIterator iterator;
        private CarbonSchedulerTask carbonSchedulerTask;

        public SchedulerTimerTask(CarbonSchedulerTask carbonSchedulerTask,
                                  CarbonDeploymentIterator iterator) {
            this.carbonSchedulerTask = carbonSchedulerTask;
            this.iterator = iterator;
        }

        public void run() {
            carbonSchedulerTask.run();
            reschedule(carbonSchedulerTask, iterator);
        }
    }
}
