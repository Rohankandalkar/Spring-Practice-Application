package com.hcl.ott.ingestion.service.Async;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.hcl.ott.ingestion.data.MetaDataDTO;
import com.hcl.ott.ingestion.data.UserCredentials;
import com.hcl.ott.ingestion.exception.IngestionException;

public interface IngestionAsyncService 
{
    CompletableFuture<MetaDataDTO> uploadFile(MultipartFile mediaFile, String fileChecksum) throws IngestionException, ExecutionException, IOException;


    CompletableFuture<MetaDataDTO> saveFileMetaData(MetaDataDTO metaData);


    CompletableFuture<List<MetaDataDTO>> getAllFiles(Pageable pageable);


    CompletableFuture<MetaDataDTO> uploadFtpFile(UserCredentials userCredentials) throws IngestionException, IOException;


    CompletableFuture<MetaDataDTO> updateMetatdateStatus(MetaDataDTO metaDataDTO) throws IngestionException;


    CompletableFuture<MetaDataDTO> getMetaDataById(String id) throws IngestionException;


    CompletableFuture<List<MetaDataDTO>> uploadMultipalFiles(MultipartFile mediaFile) throws IngestionException, IOException, AmazonServiceException, AmazonClientException, InterruptedException;
}
