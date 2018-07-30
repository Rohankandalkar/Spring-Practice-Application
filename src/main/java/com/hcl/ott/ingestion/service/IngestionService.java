package com.hcl.ott.ingestion.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hcl.ott.ingestion.data.MetaDataDTO;
import com.hcl.ott.ingestion.exception.IngestionException;

/**
 * Interface for Ingestion service
 * 
 * @author kandalakar.r
 *
 */
public interface IngestionService
{

    String uploadFile(MultipartFile mediaFile) throws IngestionException;

    MetaDataDTO saveFileMetaData(MetaDataDTO metaData);

    List<MetaDataDTO> getAllFiles() throws IngestionException;

    String uploadFtpFile(String host, String user, String password, String port, String remoteFile) throws IngestionException;

}
