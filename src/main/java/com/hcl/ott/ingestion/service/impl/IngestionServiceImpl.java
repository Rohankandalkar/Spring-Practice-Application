package com.hcl.ott.ingestion.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.hcl.ott.ingestion.controller.mapper.MetaDataMapper;
import com.hcl.ott.ingestion.dao.IngestionDao;
import com.hcl.ott.ingestion.data.MetaDataDTO;
import com.hcl.ott.ingestion.exception.IngestionException;
import com.hcl.ott.ingestion.model.MetaDataModel;
import com.hcl.ott.ingestion.service.IngestionService;
import com.hcl.ott.ingestion.service.IngestionStorageService;

/**
 * Ingestion's service class  
 * 
 * @author kandalakar.r
 *
 */
@Service
public class IngestionServiceImpl implements IngestionService
{
    @Autowired
    private IngestionStorageService ingestionStorageService;

    @Autowired
    private IngestionDao ingestionDao;

    @Autowired
    private FtpStorage ftpStorage;

    private static final Logger logger = LoggerFactory.getLogger(IngestionServiceImpl.class);


    /**
     * Upload multipart file to configured storage  
     * 
     *@param Multipart file
     *@return Location of uploaded file
     *@throws IngestionException
     *          This is custom exception.It wrappes storage exceptions
     */
    @Override
    public String uploadFile(MultipartFile mediaFile) throws IngestionException
    {

        logger.debug(" UPLOADING FILE TO STORAGE START ");
        String fileLocation = null;

        try
        {
            logger.debug(" FILE TRANSFERED TO AMAZON SERVICE TO ADD FILE IN S3 BUCKET");
            fileLocation = ingestionStorageService.uploadFile(mediaFile.getOriginalFilename(), mediaFile.getInputStream(), mediaFile.getContentType(), mediaFile.getSize());
        }
        catch (AmazonClientException | IOException exception)
        {
            logger.error(" FAILED TO UPLOAD FILE TO CONFIGURED STORAGE ");
            logger.error(exception.getMessage());
            throw new IngestionException(exception.getMessage());
        }

        if (fileLocation == null)
        {
            logger.error(" LOCATION NOT FOUND ");
            throw new IngestionException(" LOCATION NOT FOUND ");
        }
        logger.debug(" FILE SUCCESSFULLY UPLOADED TO STORAGE ");

        return fileLocation;
    }


    /** 
     * Saves uploaded file's meta-data to database
     * 
     *@parm MetaDataDTO - Data transfer object
     *@return MetaDataDTO - returns saved db object data using DTO.
     */
    @Override
    public MetaDataDTO saveFileMetaData(MetaDataDTO metaDataDTO)
    {
        logger.debug(" ADDING META-DATA OF FILE TO DATABASE ");
        MetaDataModel metaDataDBO = MetaDataMapper.makeMetaDataDBO(metaDataDTO);

        MetaDataModel metaDataDB = ingestionDao.saveMetaData(metaDataDBO);

        MetaDataDTO metaDataDT = MetaDataMapper.makeMetaDataDTO(metaDataDB);
        logger.debug(" META - DATA OF " + metaDataDBO.getTitle() + " IS ADDED TO DATABASE SUCCESSFULLY ");

        return metaDataDT;

    }


    /**
     * Returns all meta-data of uploaded files 
     *  
     *@return List<MetaDataDTO>
     *@throws IngestionException - If metadata list is empty 
     */
    @Override
    public List<MetaDataDTO> getAllFiles() throws IngestionException
    {
        logger.debug(" FINDING ALL FILES META-DATA FROM DATABASE  ");
        List<MetaDataModel> metaDataList = this.ingestionDao.findAll();

        if (metaDataList.isEmpty() && metaDataList.size() == 0)
            throw new IngestionException(" META-DATA LIST IS EMPLTY ");

        logger.debug(" SUCCEFULLY FOUND ALL FILES META-DATA FROM DATABASE  ");

        return metaDataList
            .stream()
            .map(m -> new MetaDataDTO(m.getTitle(), m.getDescription(), m.getTags(), m.getLocation()))
            .collect(Collectors.toList());
    }


    /**
     * Upload file from FTP Server to configured storage
     *  
     * @param hostName - FTP Server hostname 
     * @param userName - FTP Server credentiols
     * @param password - FTP Server credentiols
     * @param port - FTP Server port number
     * @param remoteFile - File Location on server
     */
    @Override
    public String uploadFtpFile(String host, String user, String password, String port, String remoteFile) throws IngestionException
    {
        InputStream mediaFileStream = null;
        try
        {
            logger.debug(" DOWNLOADING REQUESTED FILE FROM FTP SERVER ");
            mediaFileStream = this.ftpStorage.downloadFile(host, user, password, port, remoteFile);
        }
        catch (Exception e)
        {
            logger.error(" DOWNLOADING REQUESTED FILE FROM FTP SERVER FAILED ");
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        if (mediaFileStream == null)
            throw new IngestionException("FILE NOT DELIVERED FROM FTP SERVER.IT RETURN NULL AS VALUE FROM FTP SERVER ");

        String fileName = remoteFile.substring(remoteFile.lastIndexOf("/") + 1, remoteFile.length());

        /* //FTP FILE CONTENT TYPE 
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentType = fileNameMap.getContentTypeFor("file-path");*/

        String mediaContentType = "video";
        String fileLocation = null;
        Long contentSize = null;
        try
        {
            contentSize = (long) mediaFileStream.available();
            logger.info("" + mediaFileStream.available());
            logger.info("" + contentSize);

        }
        catch (IOException ex)
        {
            logger.error(ex.getMessage());
        }

        try
        {
            logger.debug(" UPLOADING DOWNLOADED FTP FILE TO AMAZONE S3 BUCKET ");
            fileLocation = ingestionStorageService.uploadFile(fileName, mediaFileStream, mediaContentType, contentSize);
        }
        catch (Exception e)
        {
            logger.error(" UPLOADING FILE TO AMAZONE S3 BUCKET FAILED ");
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        logger.debug(" FTP FILE SUCCESSFULLY UPLOADED TO STORAGE ");
        return fileLocation;
    }

}
