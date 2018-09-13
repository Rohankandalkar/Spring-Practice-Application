package com.hcl.ott.ingestion.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

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

    MetaDataDTO uploadFile(MultipartFile mediaFile) throws IngestionException;


    MetaDataDTO saveFileMetaData(MetaDataDTO metaData);


    List<MetaDataDTO> getAllFiles(Pageable pageable);


    MetaDataDTO uploadFtpFile(UserCredentials userCredentials) throws IngestionException;


    MetaDataDTO updateMetatdateStatus(MetaDataDTO metaDataDTO) throws IngestionException;


    MetaDataDTO getMetaDataById(String id) throws IngestionException;


    List<MetaDataDTO> uploadMultipalFiles(MultipartFile excelFile) throws IngestionException;

}
