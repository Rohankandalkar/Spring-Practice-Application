package com.hcl.ott.ingestion.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
import com.hcl.ott.ingestion.model.UserCredentials;
import com.hcl.ott.ingestion.service.IngestionService;
import com.hcl.ott.ingestion.service.IngestionStorageService;
import com.hcl.ott.ingestion.util.IngestionConstants;

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
     * Upload MultipartFile to configured storage  
     * 
     *@param MultipartFile mediaFile
     *@return MetaDataDTO - MetaData of uploaded file
     *@throws IngestionException
     *          This is custom exception.It is wrapper to storage exceptions
     */
    @Override
    public MetaDataDTO uploadFile(MultipartFile mediaFile) throws IngestionException
    {

        logger.debug(" UPLOADING FILE TO STORAGE START ");
        Map<String, Object> fileMetaData = null;
        try
        {
            logger.debug(" FILE TRANSFERED TO AMAZON SERVICE TO ADD FILE IN S3 BUCKET ");
            fileMetaData = ingestionStorageService.uploadFile(mediaFile.getOriginalFilename(), mediaFile.getInputStream(), mediaFile.getContentType(), mediaFile.getSize());
        }
        catch (AmazonClientException | IOException exception)
        {
            logger.error(" FAILED TO UPLOAD FILE TO CONFIGURED STORAGE ");
            logger.error(exception.getMessage());
            throw new IngestionException(exception.getMessage());
        }

        logger.debug(" FILE SUCCESSFULLY UPLOADED TO STORAGE ");

        MetaDataDTO metaDataDTO = getMetaDataDTO(fileMetaData, mediaFile.getSize(), mediaFile.getContentType());

        MetaDataModel metaDataModel = MetaDataMapper.makeMetaDataDBO(metaDataDTO);

        MetaDataModel metaDataDBO = this.ingestionDao.saveMetaData(metaDataModel);
        logger.debug(" META - DATA OF " + metaDataDBO.getTitle() + " IS ADDED TO DATABASE SUCCESSFULLY ");

        MetaDataDTO metaDataDT = MetaDataMapper.makeMetaDataDTO(metaDataDBO);

        return metaDataDT;
    }


    /** 
     * Saves uploaded file's meta-data to database
     * 
     *@param MetaDataDTO - Data transfer object
     *@return MetaDataDTO - returns saved DB object data using DTO.
     */
    @Override
    public MetaDataDTO saveFileMetaData(MetaDataDTO metaDataDTO)
    {
        logger.debug(" SAVE META-DATA OF FILE TO DATABASE ");
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
     *@throws IngestionException - If Metadata list is empty 
     */
    @Override
    public List<MetaDataDTO> getAllFiles()
    {
        logger.debug(" FINDING ALL FILES META-DATA FROM DATABASE  ");
        List<MetaDataModel> metaDataList = this.ingestionDao.findAll();

        logger.debug(" SUCCEFULLY FOUND ALL FILES META-DATA FROM DATABASE  ");
        List<MetaDataDTO> fileList = MetaDataMapper.makeMetaDataDTOList(metaDataList);

        Collections.reverse(fileList);
        return fileList;
    }


    /**
     * Upload file from FTP Server to configured storage
     *  
     * @param UserCredentials
     * @return MetaDataDTO
     */
    @Override
    public MetaDataDTO uploadFtpFile(UserCredentials userCredentials) throws IngestionException
    {
        Map<String, Object> fileData = null;
        try
        {
            logger.debug(" DOWNLOADING REQUESTED FILE FROM FTP SERVER ");
            fileData = this.ftpStorage.downloadFile(userCredentials);
        }
        catch (Exception e)
        {
            logger.error(" DOWNLOADING REQUESTED FILE FROM FTP SERVER FAILED ");
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        if (fileData.get(IngestionConstants.FTP_INPUTSTREAM) == null)
            throw new IngestionException(" FILE NOT DOWNLOAD FROM FTP SERVER.IT RETURN NULL AS VALUE FROM FTP SERVER ");

        String fileName = userCredentials.getRemoteFile().substring(userCredentials.getRemoteFile().lastIndexOf("/") + 1, userCredentials.getRemoteFile().length());

        String mediaContentType = "video";
        InputStream mediaFileStream = (InputStream) fileData.get(IngestionConstants.FTP_INPUTSTREAM);
        Long contentSize = (Long) fileData.get(IngestionConstants.FTP_CONTENTLENGTH);
        Map<String, Object> fileMetaData = null;

        try
        {
            logger.debug(" UPLOADING DOWNLOADED FTP FILE TO AMAZONE S3 BUCKET ");
            fileMetaData = ingestionStorageService.uploadFile(fileName, mediaFileStream, mediaContentType, contentSize);
        }
        catch (Exception e)
        {
            logger.error(" UPLOADING FILE TO AMAZONE S3 BUCKET FAILED ");
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        logger.debug(" FTP FILE SUCCESSFULLY UPLOADED TO STORAGE ");
        MetaDataDTO metaDataDTO = getMetaDataDTO(fileMetaData, contentSize, mediaContentType);

        MetaDataModel metaDataModel = MetaDataMapper.makeMetaDataDBO(metaDataDTO);

        MetaDataModel metaDataDBO = this.ingestionDao.saveMetaData(metaDataModel);
        logger.debug(" META - DATA OF " + metaDataDBO.getTitle() + " IS ADDED TO DATABASE SUCCESSFULLY ");

        MetaDataDTO metaDataDT = MetaDataMapper.makeMetaDataDTO(metaDataDBO);

        return metaDataDT;
    }


    /**
     * Upload file meta-data after processing-Transcoding 
     *  
     * @param MetaDataDTO - Object which is to be update
     * @return MetaDataDTO - Object after updating
     * @throws IngestionException 
     */
    @Override
    public MetaDataDTO updateMetatdateStatus(MetaDataDTO metaDataDTO) throws IngestionException
    {
        logger.debug(" UPDATING STATUS OF FILE'S META-DATA ");

        MetaDataModel metaDataDBO;
        try
        {
            logger.debug(" GET FIRST FILE METADATA OBJECT FROM DATABASE TO UPDATE ");
            metaDataDBO = this.ingestionDao.getMetaDataById(metaDataDTO.getId());
        }
        catch (NoSuchElementException e)
        {
            logger.error(" COULD NOT FIND ENTITY WITH ID : " + metaDataDTO.getId());
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        logger.debug(" SETTING DATABASE MODEL OBJECT ACCORDING TO STATUS OF FILE ");
        MetaDataModel metaDataModel = setStatusMetaDataDBO(metaDataDBO, metaDataDTO);

        logger.debug(" UPDATE DATABASE MODEL OBJECT ACCORDING TO STATUS OF FILE ");
        MetaDataModel updatedMetaDataDBO = this.ingestionDao.saveMetaData(metaDataModel);

        logger.debug(" SUCCESSFULLY UPDATED STATUS OF FILE'S META-DATA ");
        MetaDataDTO metaDataDT = MetaDataMapper.makeMetaDataDTO(updatedMetaDataDBO);

        return metaDataDT;
    }


    /**
     * Set MetaDataDTO object's attribute's to get Model object  
     * 
     * @param fileMetaData
     * @param fileSize
     * @param contentType
     * @return MetaDataDTO
     */
    private MetaDataDTO getMetaDataDTO(Map<String, Object> fileMetaData, Long fileSize, String contentType)
    {
        MetaDataDTO metaDataDTO = new MetaDataDTO();
        metaDataDTO.setFileKey((String) fileMetaData.get(IngestionConstants.FILE_KEY));
        metaDataDTO.setIngestionFileLocation((String) fileMetaData.get(IngestionConstants.FILE_LOCATION));
        metaDataDTO.setIngestionURL((String) fileMetaData.get(IngestionConstants.FILE_INGESTION_URL));
        metaDataDTO.setFileSize(fileSize);
        metaDataDTO.setFileContentType(contentType);
        metaDataDTO.setFileStatus(IngestionConstants.FILE_STATUS_INGESTION);
        return metaDataDTO;
    }


    /**
     * Set MetaDataModelDBO object's some attributes according to requested status 
     * 
     * @param metaDataDBO
     * @param metaDataDTO
     * @return MetaDataModel
     */
    private MetaDataModel setStatusMetaDataDBO(MetaDataModel metaDataDBO, MetaDataDTO metaDataDTO)
    {
        if (metaDataDBO.getFileStatus().equalsIgnoreCase(IngestionConstants.FILE_STATUS_INGESTION)
            || metaDataDBO.getFileStatus().equalsIgnoreCase(IngestionConstants.FILE_STATUS_PROCESS))
        {
            if (metaDataDTO.getFileStatus().equalsIgnoreCase(IngestionConstants.FILE_STATUS_PROCESS))
            {
                metaDataDBO.setProcessFileLocation(metaDataDTO.getProcessFileLocation());
                metaDataDBO.setProcessURL(metaDataDTO.getProcessURL());
                metaDataDBO.setFileStatus(metaDataDTO.getFileStatus());
                metaDataDBO.setProcessFormat(metaDataDTO.getProcessFormat());
            }

            if (metaDataDTO.getFileStatus().equalsIgnoreCase(IngestionConstants.FILE_STATUS_PUBLISH))
            {
                metaDataDBO.setPublishFileLocation(metaDataDTO.getPublishFileLocation());
                metaDataDBO.setPublishURL(metaDataDTO.getPublishURL());
                metaDataDBO.setFileStatus(metaDataDTO.getFileStatus());
            }
        }
        return metaDataDBO;

    }


    /** 
     * Returns MetaDataDTO by id 
     *
     * @param id
     * @return MetaDataDTO
     * @throws IngestionException 
     */
    @Override
    public MetaDataDTO getMetaData(String id) throws IngestionException
    {
        Long metaDataId = Long.parseLong(id);

        logger.debug(" GET METADATA OBJECT FROM DATABASE TO BY ID ");
        MetaDataModel metaDataDBO;
        try
        {
            metaDataDBO = this.ingestionDao.getMetaDataById(metaDataId);
        }
        catch (NoSuchElementException e)
        {
            logger.error(" COULD NOT FIND ENTITY WITH ID : " + metaDataId);
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        MetaDataDTO metaDataDTO = MetaDataMapper.makeMetaDataDTO(metaDataDBO);

        return metaDataDTO;
    }

}
