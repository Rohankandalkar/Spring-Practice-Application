package com.hcl.ott.ingestion.service;

import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.AmazonClientException;

/**
 * Interface to configure different storage with Ingestion service 
 * 
 * @author kandalakar.r
 *
 */
public interface IngestionStorageService
{
    public String uploadFile(String fileName, InputStream fileStream, String fileContentType, Long contentLength) throws AmazonClientException, IOException;
}
