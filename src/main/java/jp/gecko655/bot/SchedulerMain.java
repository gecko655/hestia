package jp.gecko655.bot;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForever;
import static org.quartz.TriggerBuilder.newTrigger;
import jp.gecko655.bot.hestia.HestiaBot;
import jp.gecko655.bot.hestia.HestiaReply;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerMain {
    private static Scheduler scheduler;
    public static void main(String[] args) throws SchedulerException {
        System.out.println("Scheduler Started!!!");
        scheduler = StdSchedulerFactory.getDefaultScheduler();

        scheduler.start();
        setSchedule(HestiaReply.class, repeatSecondlyForever(60*2));
        setSchedule(HestiaBot.class, repeatSecondlyForever(60*60*4));

    }
    private static void setSchedule(Class<? extends Job> classForExecute, ScheduleBuilder<? extends Trigger> schedule) throws SchedulerException {
        JobDetail jobDetail = newJob(classForExecute).build();

        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(schedule)
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        System.out.println(classForExecute.getName()+" has been scheduled");
        
    }

}