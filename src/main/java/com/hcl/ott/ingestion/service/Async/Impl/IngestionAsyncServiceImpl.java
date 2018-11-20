package com.hcl.ott.ingestion.service.Async.Impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.hcl.ott.ingestion.data.MetaDataDTO;
import com.hcl.ott.ingestion.data.UserCredentials;
import com.hcl.ott.ingestion.exception.IngestionException;
import com.hcl.ott.ingestion.service.IngestionService;
import com.hcl.ott.ingestion.service.Async.IngestionAsyncService;

@Service
@Qualifier("IngestionAsyncService")
public class IngestionAsyncServiceImpl implements IngestionAsyncService
{

    @Autowired
    @Qualifier("IngestionService")
    private IngestionService ingestionService;


    @Override
    public CompletableFuture<MetaDataDTO> uploadFile(MultipartFile mediaFile, String fileChecksum) throws IngestionException, ExecutionException, IOException
    {
        MetaDataDTO metaDataDTO = this.ingestionService.uploadFile(mediaFile, fileChecksum);
        return CompletableFuture.completedFuture(metaDataDTO);
    }


    @Async
    @Override
    public CompletableFuture<List<MetaDataDTO>> uploadMultipalFiles(MultipartFile mediaFile) throws IngestionException, IOException, AmazonServiceException, AmazonClientException, InterruptedException
    {
        List<MetaDataDTO> metaDataDTO = this.ingestionService.uploadMultipalFiles(mediaFile);
        return CompletableFuture.completedFuture(metaDataDTO);
    }


    @Override
    public CompletableFuture<MetaDataDTO> saveFileMetaData(MetaDataDTO metaData)
    {
        MetaDataDTO metaDataDTO = this.ingestionService.saveFileMetaData(metaData);
        return CompletableFuture.completedFuture(metaDataDTO);
    }


    @Override
    public CompletableFuture<List<MetaDataDTO>> getAllFiles(Pageable pageable)
    {
        List<MetaDataDTO> metaDataDTOs = this.ingestionService.getAllFiles(pageable);
        return CompletableFuture.completedFuture(metaDataDTOs);
    }


    @Override
    public CompletableFuture<MetaDataDTO> uploadFtpFile(UserCredentials userCredentials) throws IngestionException, IOException
    {
        MetaDataDTO metaDataDTO = this.ingestionService.uploadFtpFile(userCredentials);
        return CompletableFuture.completedFuture(metaDataDTO);
    }


    @Override
    public CompletableFuture<MetaDataDTO> updateMetatdateStatus(MetaDataDTO metaDataDTO) throws IngestionException
    {
        MetaDataDTO metaDataDT = this.ingestionService.updateMetatdateStatus(metaDataDTO);

        return CompletableFuture.completedFuture(metaDataDT);
    }


    @Override
    public CompletableFuture<MetaDataDTO> getMetaDataById(String id) throws IngestionException
    {
        MetaDataDTO metaDataDT = this.ingestionService.getMetaDataById(id);
        return CompletableFuture.completedFuture(metaDataDT);
    }

}
