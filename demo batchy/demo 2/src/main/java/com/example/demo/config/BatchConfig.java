package com.example.demo.config;

import com.example.demo.entity.PersonEntity;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public PlatformTransactionManager transactionManager (EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
    // CSV Reader personentity
    @Bean
    @StepScope // Add this for Spring Batch to properly handle the reader
    public FlatFileItemReader<PersonEntity> reader() {
        FlatFileItemReader<PersonEntity> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("people.csv")); // CSV file in src/main/resources
        reader.setLineMapper(new DefaultLineMapper<PersonEntity>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setDelimiter(",");  // Explicitly set comma delimiter
                setNames("email", "name", "age"); // CSV column names
            }});
            setFieldSetMapper(
                    /*new BeanWrapperFieldSetMapper<PersonEntity>() {{
                setTargetType(PersonEntity.class);
            }}*/
                    fieldSet -> {
                        PersonEntity person = new PersonEntity();
                        person.setEmail(fieldSet.readString("email"));
                        person.setName(fieldSet.readString("name"));
                        person.setAge(fieldSet.readInt("age"));
                        System.out.println("Mapped: " + person); // Debug log
                        return person;
                    });
        }});
        return reader;
    }


    // Processor (Transform Person → PersonEntity)
    /*@Bean
    public ItemProcessor<Person, PersonEntity> processor() {
        return person -> {
            PersonEntity entity = new PersonEntity();
            entity.setEmail(person.getEmail());
            entity.setName(person.getName());
            entity.setAge(person.getAge());
            return entity;
        };
    }*/

    // Database Writer (JPA)
    @Bean
    public JpaItemWriter<PersonEntity> writer(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<PersonEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    // Step and Job
    /*@Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager txManager,
                      ItemReader<Person> reader, ItemProcessor<Person, PersonEntity> processor,
                      ItemWriter<PersonEntity> writer) {
        return new StepBuilder("csvToDbStep", jobRepository)
                .<Person, PersonEntity>chunk(10, txManager) // Process 10 items at a time
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }*/

    // Step (no processor needed)
    @Bean
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager txManager,
                      ItemReader<PersonEntity> reader,
                      ItemWriter<PersonEntity> writer) {
        return new StepBuilder("csvToDbStep", jobRepository)
                .<PersonEntity, PersonEntity>chunk(10, txManager)
                .reader(reader)
                .writer(list -> {
                    System.out.println("Writing: " + list); // Add logging
                    writer.write(list);
                })
                .build();
    }

    //job
    @Bean
    public Job csvToDbJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("csvToDbJob", jobRepository)
                .start(step1)
                .build();
    }


}
