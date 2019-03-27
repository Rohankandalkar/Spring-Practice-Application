package com.hcl.ott.ingestion.util;

/**
 * Ingestion constants class 
 * 
 * @author kandalakar.r
 *
 */
public class IngestionConstants
{
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
    public static final String AWS_BASE_URL = "https://s3.ap-south-1.amazonaws.com/";
    public static final String SUCCESS_MESSAGE = "SUCCESSFULLY DONE OPERATION";
    public static final String FILE_LOCATION = "FILE_LOCATION";
    public static final String FILE_KEY = "FILE_KEY";
    public static final String FILE_STATUS_INGESTION_PROCESS = "DRAFTED";
    public static final String FILE_STATUS_INGESTION = "INGESTED";
    public static final String FILE_STATUS_PROCESS = "PROCESSED";
    public static final String FILE_STATUS_PUBLISH = "PUBLISHED";
    public static final String FILE_STATUS_COMPLETE = "COMPLETE";
    public static final String FILE_INGESTION_URL = "FILE_INGESTION_URL";
    public static final String FILE_CONTENT_TYPE_VIDEO = "video";
    public static final String FILE_CONTENT_TYPE_AUDIO = "audio";
    public static final String FILE_DIRECTORY = "C:\\Users\\kandalakar.r\\Documents\\OTT POC\\OTT_DIRECTORY";
    public static final String FTP_INPUTSTREAM = "FTPFileStream";
    public static final String FTP_CONTENTLENGTH = "FTPFilecontentLength";
    public static final String AWS_FILE_CHECKSUM = "AWS_FILE_MURGED_ETAG_CHECKSUM";
    public static final String ALGORITHM_CHECKSUM = "CALCULATED_FILE_CHECKSUM_USING_ALGORITHM";
    public static final String PART_ETAGS = "LIST_OF_ETAGS_OF_PARTS";
    public static final String VIDEO_DIRECTORY = "video";
    public static final String AUDIO_DIRECTORY = "audio";
    public static final long MULTIPART_PART_SIZE = 5 * 1024 * 1024;
    public static final long FILE_UPLOAD_LIMIT = 1000 * 1024 * 1024;
    public static final String VIDEO_FILE_TYPE = "video";

}
