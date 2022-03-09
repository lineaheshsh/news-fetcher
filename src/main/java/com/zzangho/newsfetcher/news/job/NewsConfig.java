package com.zzangho.newsfetcher.news.job;

import com.zzangho.newsfetcher.news.item.BulkFileWriter;
import com.zzangho.newsfetcher.news.listener.NewsJobExecutionListener;
import com.zzangho.newsfetcher.news.model.News;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@Slf4j
public class NewsConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final @Qualifier("springBatchDB")
    DataSource dataSource;
//    private final TaskExecutor taskExecutor;

    private int chunkSize = 5000;

//    public NewsConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory, DataSource dataSource, TaskExecutor taskExecutor) {
    public NewsConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.dataSource = dataSource;
//        this.taskExecutor = taskExecutor;
    }

    @Bean(name = "newsTaskPool")
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(); // (2)
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("multi-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }

    @Bean
    public Job newsJob() throws Exception {
        log.info("newsJob start");
        return jobBuilderFactory.get("newsJob")
                .incrementer(new RunIdIncrementer())
                .start(newsStep())
                .listener(new NewsJobExecutionListener())
                .build();
    }

    @Bean
    public Step newsStep() throws Exception {
        return stepBuilderFactory.get("newsStep")
                .<News, News>chunk(chunkSize)
                .reader(itemReader())
//                .processor(new NewsProcessor())
                .writer(itemWriter())
                .taskExecutor(executor())
                .throttleLimit(8)
                .build();
    }

    private JpaPagingItemReader<News> itemReader() throws Exception {
        JpaPagingItemReader<News> jpaPagingItemReader = new JpaPagingItemReaderBuilder<News>()
                .queryString("SELECT v FROM view_news v ORDER BY contents_id ASC")
                .pageSize(chunkSize)
                .entityManagerFactory(entityManagerFactory)
                .name("jpaPagingItemReader")
                .build();

        jpaPagingItemReader.afterPropertiesSet();
        return jpaPagingItemReader;
    }

    private SynchronizedItemStreamWriter<News> itemWriter() throws Exception {
        System.out.println("itemWriter : " + Thread.currentThread().getId());
        SynchronizedItemStreamWriter<News> itemStreamWriter = new SynchronizedItemStreamWriter<>();
        itemStreamWriter.setDelegate(new BulkFileWriter());

        itemStreamWriter.afterPropertiesSet();
        return itemStreamWriter;
    }

//    private ItemWriter<News> itemWriter() {
//        return items -> {
//            for (News news : items) {
//                log.info(String.valueOf(news.getContents_id()));
//            }
//        };
//    }
}
