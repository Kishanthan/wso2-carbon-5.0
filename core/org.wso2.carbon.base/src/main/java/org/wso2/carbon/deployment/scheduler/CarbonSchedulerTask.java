package org.wso2.carbon.deployment.scheduler;

import org.wso2.carbon.deployment.CarbonRepositoryListener;

import java.util.TimerTask;


public class CarbonSchedulerTask {
    static final int SCHEDULED = 1;
    static final int CANCELLED = 2;
    final Object lock = new Object();
    int state = 0;
    TimerTask timerTask;
    private CarbonRepositoryListener repositoryListener;

    /**
     * Creates a new carbonScheduler task.
     */
    public CarbonSchedulerTask(CarbonRepositoryListener listener) {
        this.repositoryListener = listener;
    }

    /**
     * Cancels this carbonScheduler task.
     * <p/>
     * This method may be called repeatedly; the second and subsequent calls have no effect.
     *
     * @return Returns true if this task was already scheduled to run.
     */
    public boolean cancel() {
        synchronized (lock) {
            if (timerTask != null) {
                timerTask.cancel();
            }
            boolean result = (state == SCHEDULED);
            state = CANCELLED;
            return result;
        }
    }

    private void checkRepository() {
        repositoryListener.start();
    }

    /**
     * The action to be performed by this carbonScheduler task.
     */
    public void run() {
        checkRepository();
    }
}
