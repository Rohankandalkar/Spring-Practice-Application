package com.hcl.ott.ingestion.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.hcl.ott.ingestion.service.IngestionStorageService;
import com.hcl.ott.ingestion.util.IngestionConstants;

/**
 * Amazon Storage Component 
 * Implements IngestionStorageService Interface
 * 
 * @author kandalakar.r
 */
@Component
public class AmazonStorage implements IngestionStorageService
{

    @Autowired
    private AmazonS3 s3Client;

    @Value("${ecms.aws.bucketName}")
    private String bucketName;

    @Value("${ecms.aws.bucketName.video}")
    private String bucketVideoFolder;

    @Value("${ecms.aws.bucketName.audio}")
    private String bucketAudioFolder;

    private static final Logger logger = LoggerFactory.getLogger(AmazonStorage.class);


    /**
     *upload file to amzone s3 bucket
     *
     *@param fileName
     *@param fileStram
     *@param fileContentType
     *@param fileSize
     *@return fileLocation - Uploaded file location
     *@exception AmazonClientException - If amazon s3 put request failed 
     * @throws IOException 
     *
     */
    @Override
    public Map<String, Object> uploadFile(String fileName, InputStream fileStream, String fileContentType, Long fileSize) throws AmazonClientException, IOException
    {
        logger.debug(" UPLOADING FILE TO AMAZON S3 BUCKET START ");
        String bucketName = getBucketName(fileContentType);
        String fileKey = generateFileKey(fileName);

        PutObjectRequest putObjectRequest = getPutObjectRequest(fileKey, fileStream, fileContentType, bucketName, fileSize);

        this.s3Client.putObject(putObjectRequest);
        String fileURL = this.s3Client.getUrl(bucketName, fileKey).toString();

        String fileLocation = putObjectRequest.getBucketName() + "/" + putObjectRequest.getKey();

        Map<String, Object> fileMetaData = new HashMap<String, Object>();
        fileMetaData.put(IngestionConstants.FILE_INGESTION_URL, fileURL);
        fileMetaData.put(IngestionConstants.FILE_LOCATION, fileLocation);
        fileMetaData.put(IngestionConstants.FILE_KEY, fileKey);
        return fileMetaData;

    }


    /**
     * Returns configured PutObjectRequest object required to send put request to amazon s3 bucket 
     * 
     * @param fileName
     * @param fileStream
     * @param fileContentType
     * @param bucketName
     * @param contentLength
     * @return PutObjectRequest
     * @throws IOException 
     */
    private PutObjectRequest getPutObjectRequest(String key, InputStream fileStream, String fileContentType, String bucketName, Long contentLength) throws IOException
    {
        PutObjectRequest putObjectRequest = new PutObjectRequest(null, null, null, null);

        putObjectRequest.setKey(key);
        putObjectRequest.setInputStream(fileStream);
        putObjectRequest.setBucketName(bucketName);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(fileContentType);
        metadata.setContentLength(contentLength);

        putObjectRequest.setMetadata(metadata);
        putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
        return putObjectRequest;
    }


    /**
     * Returns BucketName with sub-folder and sub-folder depends on filecontentType.
     * Bucketname with sub-folder select dynamicaly to store file in particuler folder on amazon s3 bucket
     * 
     * @param fileContentType
     * @return tempBucketName Destination folder bucket
     */
    private String getBucketName(String fileContentType)
    {
        String tempBucketName = this.bucketName;

        if (fileContentType.contains(IngestionConstants.FILE_CONTENT_TYPE_VIDEO))
        {
            tempBucketName = this.bucketVideoFolder;
        }
        else if (fileContentType.contains(IngestionConstants.FILE_CONTENT_TYPE_AUDIO))
        {
            tempBucketName = this.bucketAudioFolder;
        }

        return tempBucketName;
    }


    /**
     * Returns unique key.which is used as name on amazon s3 bucket.
     * 
     * @param fileName
     * @return key - unique key
     */
    private String generateFileKey(String fileName)
    {
        String uniqueID = UUID.randomUUID().toString();
        String key = uniqueID + "_" + fileName;
        return key;
    }

}
