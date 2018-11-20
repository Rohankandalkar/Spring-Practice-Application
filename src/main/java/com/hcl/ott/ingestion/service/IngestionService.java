package com.hcl.ott.ingestion.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.hcl.ott.ingestion.data.MetaDataDTO;
import com.hcl.ott.ingestion.data.UserCredentials;
import com.hcl.ott.ingestion.exception.IngestionException;

/**
 * Interface for Ingestion service
 * 
 * @author kandalakar.r
 *
 */
public interface IngestionService
{

    MetaDataDTO uploadFile(MultipartFile mediaFile, String fileChecksum) throws IngestionException, IOException;


    MetaDataDTO saveFileMetaData(MetaDataDTO metaData);


    List<MetaDataDTO> getAllFiles(Pageable pageable);


    MetaDataDTO uploadFtpFile(UserCredentials userCredentials) throws IngestionException, IOException;


    MetaDataDTO updateMetatdateStatus(MetaDataDTO metaDataDTO) throws IngestionException;


    MetaDataDTO getMetaDataById(String id) throws IngestionException;


    List<MetaDataDTO> uploadMultipalFiles(MultipartFile excelFile) throws IngestionException, IOException, AmazonServiceException, AmazonClientException, InterruptedException;

}
