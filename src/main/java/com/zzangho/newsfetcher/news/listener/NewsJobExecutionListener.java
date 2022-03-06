package com.zzangho.newsfetcher.news.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

import java.util.Collection;

@Slf4j
public class NewsJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("############################################################################");
        log.info("############################# 뉴스 Fetcher 프로그램 시작 ##############################");
        log.info("############################################################################");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long time = jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime();
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        int sum = stepExecutions.stream().mapToInt(stepExecution -> {
            int writeCount = stepExecution.getWriteCount();
            return writeCount;
        }).sum();

        log.info("############################################################################");
        log.info("총 데이터 처리 {}건, 처리시간 {}millis", sum, time);
        log.info("############################################################################");
    }
}
