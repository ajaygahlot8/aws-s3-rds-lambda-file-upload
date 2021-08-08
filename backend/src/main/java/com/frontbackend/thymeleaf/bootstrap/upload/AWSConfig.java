package com.frontbackend.thymeleaf.bootstrap.upload;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class AWSConfig {


  @Bean
  @Primary
  public AmazonS3 awsS3Client(AWSCredentialsProvider credentialsProvider,
                              @Value("${cloud.aws.region.static}") String region) {
    return AmazonS3ClientBuilder
        .standard()
        .withCredentials(credentialsProvider)
        .withRegion(region)
        .build();
  }
}