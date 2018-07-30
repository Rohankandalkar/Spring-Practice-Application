package com.hcl.ott.ingestion.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * Configuration class for amazon client
 * 
 * @author kandalakar.r
 *
 */
@Configuration
public class AmazonStorageConfiguration
{

    @Value("${ecms.aws.accessKey}")
    private String accessKey;

    @Value("${ecms.aws.secretKey}")
    private String secretKey;

    @Value("${ecms.aws.region}")
    private String region;


    @Bean
    public AmazonS3 s3Client()
    {
        AWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCreds);
        AmazonS3 s3Client =
            AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(awsCredentialsProvider)
                .build();
        return s3Client;

    }

}
