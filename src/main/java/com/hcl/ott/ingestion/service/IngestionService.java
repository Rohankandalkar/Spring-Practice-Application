package com.hcl.ott.ingestion.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hcl.ott.ingestion.data.MetaDataDTO;
import com.hcl.ott.ingestion.exception.IngestionException;
import com.hcl.ott.ingestion.model.UserCredentials;

/**
 * Interface for Ingestion service
 * 
 * @author kandalakar.r
 *
 */
public interface IngestionService
{

    MetaDataDTO uploadFile(MultipartFile mediaFile) throws IngestionException;


    MetaDataDTO saveFileMetaData(MetaDataDTO metaData);


    List<MetaDataDTO> getAllFiles();


    MetaDataDTO uploadFtpFile(UserCredentials userCredentials) throws IngestionException;


    MetaDataDTO updateMetatdateStatus(MetaDataDTO metaDataDTO)throws IngestionException;


    MetaDataDTO getMetaData(String id) throws IngestionException;

}
