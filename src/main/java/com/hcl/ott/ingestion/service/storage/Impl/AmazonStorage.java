package com.hcl.ott.ingestion.service.storage.Impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.ObjectMetadataProvider;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.Md5Utils;
import com.hcl.ott.ingestion.data.FileDetailsData;
import com.hcl.ott.ingestion.exception.IngestionException;
import com.hcl.ott.ingestion.service.storage.IngestionStorageService;
import com.hcl.ott.ingestion.util.AmazonUploadListener;
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
    private TransferManager transferManager;

    @Value("${ecms.aws.bucketName}")
    private String bucketName;

    @Value("${ecms.aws.bucketName.video}")
    private String bucketVideoFolder;

    @Value("${ecms.aws.bucketName.audio}")
    private String bucketAudioFolder;

    private static final Logger logger = LoggerFactory.getLogger(AmazonStorage.class);


    /**
     * 
     *USING THIS METHOD WE UPLOAD FILE TO AWS S3 AND 
     *RETURN META-DATA OF UPLOAED FILE TO SERVICE. 
     *
     *@param FileDetailsData - WE GOT FILE DETAILS WHICH WE HAVE TO UPLOAD 
     *@return FileDetailsData - WE SET ADDITIONAL META-DATA TO FILES DETAILS AND RETURN TO SERVICE.
     *@throws AmazonClientException,IOException,InterruptedException,IngestionException,ExecutionException
     *
     */
    @Override
    public FileDetailsData uploadFile(FileDetailsData fileDetailsData)
        throws AmazonClientException, IOException, InterruptedException, IngestionException, ExecutionException
    {
        logger.debug(" AMAZONE SERVICE : UPLOADING " + fileDetailsData.getFileKey() + " FILE TO AMAZON S3 BUCKET START ");

        //SET BUCKET NAME WITH PROPER PREFIX
        String ContentBucketName = getBucketName(fileDetailsData.getFileContentType());

        //OBJECT WHICH CONTAINS DATA TO BE STORE ON S3 BUCKET
        PutObjectRequest putObjectRequest = getPutObjectRequest(fileDetailsData);

        logger.debug(" AMAZONE SERVICE : TRANSFER MANAGER UPLOADING FILE TO AMAZON S3  ");

        Upload myUpload = this.transferManager.upload(putObjectRequest);

        myUpload.waitForCompletion();

        if (myUpload.isDone())
            fileDetailsData.setFileStatus(IngestionConstants.FILE_STATUS_INGESTION_PROCESS);

        logger.debug(" AMAZONE SERVICE : " + fileDetailsData.getFileKey() + "  FILE UPLOADED REQUEST COMPLETED FOR ALL MULTIPART PARTS  ");

        String fileURL = this.transferManager.getAmazonS3Client().getUrl(bucketName, fileDetailsData.getFileKey()).toString();

        fileDetailsData.setIngestionFileLocation(ContentBucketName + "/" + fileDetailsData.getFileKey());
        fileDetailsData.setIngestionURL(fileURL);

        logger.debug("AMAZONE SERVICE : " + fileDetailsData.getFileKey() + "  FILE UPLOADED TO AMAZON S3 BUCKET SUCCESSFULLY  ");

        return fileDetailsData;

    }


    /**
     * Returns BucketName with sub-folder and sub-folder depends on filecontentType.
     * BucketName with sub-folder select dynamically to store file in particular folder on amazon s3 bucket
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
     *  
     * RETURNS PUTOBJECTREQUEST OBJECT REQUIRED TO SEND PUTOBJECT REQUEST TO AWS S3
     * @param FileDetailsData
     * @return PutObjectRequest
     * @throws IOException 
     */
    private PutObjectRequest getPutObjectRequest(FileDetailsData fileDetailsData) throws IOException
    {
        PutObjectRequest putObjectRequest = new PutObjectRequest(null, null, null, null);

        putObjectRequest.setKey(fileDetailsData.getFileKey());
        putObjectRequest.setFile(fileDetailsData.getFile());
        putObjectRequest.setBucketName(getBucketName(fileDetailsData.getFileContentType()));

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(fileDetailsData.getFileContentType());
        metadata.setContentLength(fileDetailsData.getFileSize());

        String contentMD5 = Md5Utils.md5AsBase64(fileDetailsData.getFile());
        metadata.setContentMD5(contentMD5);
        metadata.addUserMetadata("x-amz-meta-" + fileDetailsData.getFileKey(), contentMD5);

        putObjectRequest.setMetadata(metadata);
        //putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);

        AmazonUploadListener progressListener = new AmazonUploadListener();
        putObjectRequest.withGeneralProgressListener(progressListener);

        return putObjectRequest;
    }


    /** 
     * USING THIS METHOD WE UPLOAD MULTIPAL FILES IN ONE REQUEST 
     * AND RETURN LIST OF META-DATA'S OF UPLOADED FILES ACCODING TO THERE UPLOADING STATUS
     * 
     * @param List<FileDetailsData> -  WE GOT LIST OF OBJECTS WHICH CONTAINS SOME BASIC META-DATA OF UPLODING FILES .
     * @return List<FileDetailsData> - WE SET META-DATA OF UPLODED FILES TO ACCEPTED LIST OF META-DATA OF FILE AND
     *                                    RETURN LIST OF NEW META-DATA.
     */
    @Override
    public List<FileDetailsData> uploadFileList(List<FileDetailsData> storageDetailsList) throws AmazonServiceException, AmazonClientException, InterruptedException
    {

        logger.debug(" AMAZONE SERVICE : UPLOADING " + storageDetailsList.toString() + " FILE'S TO AMAZON S3 BUCKET START ");

        List<File> files = storageDetailsList.stream().map(ftpFile -> ftpFile.getFile()).collect(Collectors.toList());

        File directory = new File(files.get(0).getParent());

        ObjectMetadataProvider metadataProvider = getObjectMetadataProvider();

        String virtualDirectoryKeyPrefix = getvirtualDirectoryKeyPrefix(storageDetailsList);

        //AWS UPLOADFILELIST STANDARD METHOD CALL TO UPLOAD LIST OF FILES IN ONE REQUEST
        MultipleFileUpload upload = this.transferManager.uploadFileList(bucketName, virtualDirectoryKeyPrefix, directory, files, metadataProvider);

        upload.addProgressListener(new AmazonUploadListener());

        upload.waitForCompletion();

        //FIND OUT ONLY SUCCESSFULLY UPLOADED FILES NAME (isDone() RETURN TRUE OR FALSE DEPENDS ON SUCCESS OR FAILURE)      
        Set<String> uploadedFilesNames =
            upload
                .getSubTransfers().stream().filter(fileStatus -> fileStatus.isDone())
                .map(fileDescription -> fileDescription.getDescription().substring(fileDescription.getDescription().lastIndexOf("/") + 1)).collect(Collectors.toSet());

        //FIND OUT META-DATA FROM LIST OF LOCAL META-DATA LIST ACCORDING TO SUCCESSFULLY UPLOADED FILES NAME
        List<FileDetailsData> uploadedFileDetails =
            storageDetailsList.stream().filter(fileMetaData -> uploadedFilesNames.contains(fileMetaData.getFileKey())).collect(Collectors.toList());

        uploadedFileDetails.stream().forEach(fileMetaData -> {

            fileMetaData.setFileStatus(IngestionConstants.FILE_STATUS_INGESTION_PROCESS);
            fileMetaData.setIngestionFileLocation(bucketVideoFolder + "/" + fileMetaData.getFileKey());
            fileMetaData.setIngestionURL(this.transferManager.getAmazonS3Client().getUrl(bucketName, fileMetaData.getFileKey()).toString());

        });

        logger.debug(" AMAZONE SERVICE : SUCCESSFULLY UPLODED THIS  " + uploadedFileDetails.toString() + " FILE'S TO AMAZON S3 BUCKET START ");

        return storageDetailsList;

    }


    /**
     * THIS IS RETURNS DIRCTORY NAME ACCORDING TO FILE TYPES - VIDEO,AUDIO
     * IF FILE TYPE IS DIFFRENT THAN VIDEO ,AUDIO THEN IT WILL RETURN NULL
     * THEN FILE WILL STORE AT ROOT DIRECTORY OF AWS S3 BUCKET. 
     * 
     * @return virtualDirectoryKeyPrefix - NAME OF DIRECTORY
     */
    private String getvirtualDirectoryKeyPrefix(List<FileDetailsData> storageDetailsList)
    {
        String virtualDirectoryKeyPrefix = null;

        if (storageDetailsList.get(0).getFileContentType().contains(IngestionConstants.FILE_CONTENT_TYPE_VIDEO))
            virtualDirectoryKeyPrefix = IngestionConstants.VIDEO_DIRECTORY;
        if (storageDetailsList.get(0).getFileContentType().contains(IngestionConstants.FILE_CONTENT_TYPE_AUDIO))
            virtualDirectoryKeyPrefix = IngestionConstants.AUDIO_DIRECTORY;

        return virtualDirectoryKeyPrefix;
    }


    /**
     * THIS IS THE CALLBACK METHOD.THIS METHOD GET CALL DURING UPLOADING MULTIPAL FILES IN ONE REQUEST.
     * DURING UPLODING LIST OF FILES,USING THIS METHOD WE SET META-DATA OF THAT FILES.
     * 
     * 
     * @return ObjectMetadataProvider
     */
    private ObjectMetadataProvider getObjectMetadataProvider()
    {
        ObjectMetadataProvider objectMetadataProvider = new ObjectMetadataProvider()
        {

            @Override
            public void provideObjectMetadata(File file, ObjectMetadata metadata)
            {
                metadata.setContentLength(file.length());

                String localContentMd5 = null;
                try
                {
                    metadata.setContentMD5(Md5Utils.md5AsBase64(file));

                    localContentMd5 = DigestUtils.md5Hex(new FileInputStream(file));

                }
                catch (IOException e)
                {
                    logger.error(e.getMessage());

                }
                metadata.addUserMetadata("x-amz-meta-" + file.getName(), localContentMd5);

            }
        };

        return objectMetadataProvider;

    }

}
