package com.hcl.ott.ingestion.service.storage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.hcl.ott.ingestion.data.FileDetailsData;
import com.hcl.ott.ingestion.exception.IngestionException;

/**
 * Interface to configure different storage with Ingestion service 
 * 
 * @author kandalakar.r
 *
 */
public interface IngestionStorageService
{
    public FileDetailsData uploadFile(FileDetailsData fileDetailsData)
        throws AmazonClientException, IOException, InterruptedException, IngestionException, ExecutionException;

    public List<FileDetailsData> uploadFileList(List<FileDetailsData> storageDetailsList) throws AmazonServiceException, AmazonClientException, InterruptedException;
}
