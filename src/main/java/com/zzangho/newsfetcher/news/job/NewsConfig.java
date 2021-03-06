package com.zzangho.newsfetcher.news.job;

import com.zzangho.newsfetcher.common.Constants;
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
import org.springframework.beans.factory.annotation.Value;
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
    private final TaskExecutor taskExecutor;

    private int chunkSize = 5000;

    @Value("${bulk.dir}")
    private String dir;

    @Value("${bulk.file-name}")
    private String fileName;

    public NewsConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory, DataSource dataSource, TaskExecutor taskExecutor) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.dataSource = dataSource;
        this.taskExecutor = taskExecutor;
    }

    /**
     * News JOB
     *
     * @return
     * @throws Exception
     */
    @Bean
    public Job newsJob() throws Exception {
        return jobBuilderFactory.get(Constants.NEWS_JOB + "Job")
                .incrementer(new RunIdIncrementer())
                .start(newsStep())
                .listener(new NewsJobExecutionListener())
                .build();
    }

    /**
     * News Job Step
     * -. Reader : MariaDB?????? News ????????? ?????????
     * -. Writer : News ????????? bulk.json????????? ??????
     * @return
     * @throws Exception
     */
    @Bean
    public Step newsStep() throws Exception {
        return stepBuilderFactory.get(Constants.NEWS_JOB + "Step")
                .<News, News>chunk(chunkSize)
                .reader(itemReader())
                .writer(itemWriter(dir, fileName))
                .taskExecutor(taskExecutor)
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

    /**
     * Thread safe
     * @return
     * @throws Exception
     */
    private SynchronizedItemStreamWriter<News> itemWriter(String dir, String fileName) throws Exception {
        SynchronizedItemStreamWriter<News> itemStreamWriter = new SynchronizedItemStreamWriter<>();
        itemStreamWriter.setDelegate(new BulkFileWriter(dir, fileName));

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
