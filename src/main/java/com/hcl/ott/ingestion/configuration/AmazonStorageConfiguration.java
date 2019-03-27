package com.hcl.ott.ingestion.configuration;

import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

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


    /**
     * Gets an TransferManager.
     * create a client connection based on credentials
     * save your credentials at application.properties. 
     *
     * accessKey = YOUR_ACCESS_KEY_ID
     * secretKey = YOUR_SECRET_ACCESS_KEY
     * 
     * @return a TransferManager
     */
    @Bean
    public TransferManager getTransferManager()
    {
        AWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCreds);
        ClientConfiguration config = new ClientConfiguration();
        config.setSocketTimeout(0);

        AmazonS3 s3Client =
            AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(awsCredentialsProvider)
                .withClientConfiguration(config)
                .build();

        TransferManager transferManager =
            TransferManagerBuilder
                .standard()
                .withS3Client(s3Client)
                .withMinimumUploadPartSize(Long.valueOf(5 * 1024 * 1024))
                .withMultipartUploadThreshold(Long.valueOf(2 * 1024 * 1024))
                .withExecutorFactory(() -> Executors.newFixedThreadPool(20))
                .build();

        return transferManager;
    }

}
