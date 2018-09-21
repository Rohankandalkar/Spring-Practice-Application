package com.hcl.ott.ingestion.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.hcl.ott.ingestion.controller.mapper.MetaDataMapper;
import com.hcl.ott.ingestion.dao.IngestionDao;
import com.hcl.ott.ingestion.data.MetaDataDTO;
import com.hcl.ott.ingestion.data.UserCredentials;
import com.hcl.ott.ingestion.exception.IngestionException;
import com.hcl.ott.ingestion.model.MetaDataModel;
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
@Qualifier("IngestionService")
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
     * @throws InterruptedException 
     */
    @Override
    public MetaDataDTO uploadFile(MultipartFile mediaFile) throws IngestionException
    {

        logger.info(" INGESTION SERVICE : UPLOADING " + mediaFile.getOriginalFilename() + "FILE TO STORAGE START ");
        Map<String, Object> fileMetaData = null;
        try
        {
            logger.debug(" INGESTION SERVICE : FILE TRANSFERED TO AMAZON SERVICE TO UPLOAD FILE IN S3 BUCKET ");
            fileMetaData = ingestionStorageService.uploadFile(mediaFile.getOriginalFilename(), mediaFile.getInputStream(), mediaFile.getContentType(), mediaFile.getSize());
        }
        catch (AmazonClientException | IOException | InterruptedException exception)
        {
            logger.error(" FAILED TO UPLOAD FILE TO CONFIGURED STORAGE ");
            logger.error(exception.getMessage());
            throw new IngestionException(exception.getMessage());
        }

        MetaDataDTO metaDataDTO = getMetaDataDTO(fileMetaData, mediaFile.getSize(), mediaFile.getContentType());

        MetaDataModel metaDataModel = MetaDataMapper.makeMetaDataDBO(metaDataDTO);

        //SAVE METADATA OF FILE TO DATABASE WITH STATUS DRAFTED
        MetaDataModel metaDataDBO = this.ingestionDao.saveMetaData(metaDataModel);

        logger.info(" META - DATA OF " + metaDataDBO.getTitle() + " IS ADDED TO DATABASE SUCCESSFULLY ");

        //MAPING META-DATA DATABASE OBJECT WITH DATA-TRANSFER OBJECT
        MetaDataDTO metaDataDT = MetaDataMapper.makeMetaDataDTO(metaDataDBO);

        return metaDataDT;
    }


    /** 
     * Save uploaded file's meta-data to database
     * 
     *@param MetaDataDTO - Data transfer object
     *@return MetaDataDTO - returns saved DB object data using DTO.
     */
    @Override
    public MetaDataDTO saveFileMetaData(MetaDataDTO metaDataDTO)
    {
        logger.info(" INGESTION SERVICE : SAVE FINAL META-DATA OF FILE TO DATABASE ");

        MetaDataModel metaDataDBO = MetaDataMapper.makeMetaDataDBO(metaDataDTO);

        //SAVE META-DATA OF FILE TO DATABASE WITH STATUS INGESTED
        MetaDataModel metaDataDB = ingestionDao.saveMetaData(metaDataDBO);

        MetaDataDTO metaDataDT = MetaDataMapper.makeMetaDataDTO(metaDataDB);

        logger.info(" INGESTION SERVICE :  META - DATA OF " + metaDataDBO.getTitle() + " IS ADDED TO DATABASE SUCCESSFULLY ");

        return metaDataDT;

    }


    /**
     * Returns all List of uploaded files Meta-Data 
     *  
     *@return List<MetaDataDTO>
     *@throws IngestionException - If Metadata list is empty 
     */
    @Override
    public List<MetaDataDTO> getAllFiles(Pageable pageable)
    {
        logger.debug(" INGESTION SERVICE : FINDING ALL FILES META-DATA FROM DATABASE  ");
        Page<MetaDataModel> metaDataList = this.ingestionDao.findAll(pageable);

        logger.debug(" INGESTION SERVICE : SUCCEFULLY FOUND ALL FILES META-DATA FROM DATABASE  ");
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
        logger.info(" INGESTION SERVICE : DOWNLOADING REQUESTED FILE FROM FTP SERVER ");

        Map<String, Object> fileData = null;
        try
        {
            //DOWNLOADING REQUESTED FILE FROM FTP SERVER 
            logger.debug(" INGESTION SERVICE : DOWNLOADING REQUESTED FILE FROM FTP SERVER ");
            fileData = this.ftpStorage.downloadFile(userCredentials);
        }
        catch (Exception e)
        {
            logger.error(" INGESTION SERVICE : DOWNLOADING REQUESTED FILE FROM FTP SERVER FAILED ");
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        if (fileData.get(IngestionConstants.FTP_INPUTSTREAM) == null)
            throw new IngestionException(" FILE NOT DOWNLOAD FROM FTP SERVER.IT RETURN NULL AS VALUE FROM FTP SERVER ");

        String fileName = userCredentials.getRemoteFile().get(0);

        String mediaContentType = "video";
        InputStream mediaFileStream = (InputStream) fileData.get(IngestionConstants.FTP_INPUTSTREAM);
        Long contentSize = (Long) fileData.get(IngestionConstants.FTP_CONTENTLENGTH);
        Map<String, Object> fileMetaData = null;

        try
        {

            //UPLOADING FTP FILE TO AMAZONE S3 BUCKET 
            logger.debug(" INGESTION SERVICE : UPLOADING DOWNLOADED FTP FILE TO AMAZONE S3 BUCKET ");
            fileMetaData = ingestionStorageService.uploadFile(fileName, mediaFileStream, mediaContentType, contentSize);
        }
        catch (Exception e)
        {
            logger.error(" INGESTION SERVICE : UPLOADING FILE TO AMAZONE S3 BUCKET FAILED ");
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        logger.debug(" INGESTION SERVICE : FTP FILE SUCCESSFULLY UPLOADED TO STORAGE ");
        MetaDataDTO metaDataDTO = getMetaDataDTO(fileMetaData, contentSize, mediaContentType);

        MetaDataModel metaDataModel = MetaDataMapper.makeMetaDataDBO(metaDataDTO);

        //SAVE META-DATA OF FTP FILE TO DATABASE WITH STATUS DRAFTED
        MetaDataModel metaDataDBO = this.ingestionDao.saveMetaData(metaDataModel);

        logger.debug(" INGESTION SERVICE : META - DATA OF " + metaDataDBO.getTitle() + " IS ADDED TO DATABASE SUCCESSFULLY ");

        MetaDataDTO metaDataDT = MetaDataMapper.makeMetaDataDTO(metaDataDBO);

        return metaDataDT;
    }


    /**
     * Update file's meta-data after processing-Transcoding 
     *  
     * @param MetaDataDTO - Object which is to be update
     * @return MetaDataDTO - Object after updating
     * @throws IngestionException 
     */
    @Override
    public MetaDataDTO updateMetatdateStatus(MetaDataDTO metaDataDTO) throws IngestionException
    {
        logger.info(" INGESTION SERVICE : UPDATING STATUS OF FILE'S META-DATA ");

        MetaDataModel metaDataDBO;
        try
        {
            if (metaDataDTO.getFileStatus().equalsIgnoreCase(IngestionConstants.FILE_STATUS_COMPLETE))
            {
                //THIS METHOD WORK ONLY IF UPDATE STATUS API REQUESTED BY AWS LAMBDA FUNCTION.
                logger.debug(" INGESTION SERVICE : UPDATE REQUEST FROM AWS LAMBDA FUNCTION FIND METADATA OBJECT FROM DATABASE USING JOBID ");
                metaDataDBO = this.ingestionDao.getMetaDataByJobId(metaDataDTO.getJobId());
            }
            else
            {
                //THIS METHOD WORK FOR OTHER USERS.
                logger.debug(" INGESTION SERVICE : GET FIRST FILE METADATA OBJECT FROM DATABASE TO UPDATE ");
                metaDataDBO = this.ingestionDao.getMetaDataById(metaDataDTO.getId());
            }
        }
        catch (NoSuchElementException e)
        {
            logger.error(" INGESTION SERVICE : COULD NOT FIND ENTITY WITH ID : " + metaDataDTO.getId());
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        logger.debug(" INGESTION SERVICE : SETTING DATABASE MODEL OBJECT ACCORDING TO STATUS OF FILE ");
        MetaDataModel metaDataModel = setStatusMetaDataDBO(metaDataDBO, metaDataDTO);

        logger.debug(" INGESTION SERVICE : UPDATE DATABASE MODEL OBJECT ACCORDING TO STATUS OF FILE ");
        MetaDataModel updatedMetaDataDBO = this.ingestionDao.saveMetaData(metaDataModel);

        MetaDataDTO metaDataDT = MetaDataMapper.makeMetaDataDTO(updatedMetaDataDBO);
        logger.info(" INGESTION SERVICE : SUCCESSFULLY UPDATED STATUS OF FILE'S META-DATA ");

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
            || metaDataDBO.getFileStatus().equalsIgnoreCase(IngestionConstants.FILE_STATUS_PROCESS)
            || metaDataDBO.getFileStatus().equalsIgnoreCase(IngestionConstants.FILE_STATUS_COMPLETE))
        {
            if (metaDataDTO.getFileStatus().equalsIgnoreCase(IngestionConstants.FILE_STATUS_PROCESS))
            {
                metaDataDBO.setProcessFileLocation(metaDataDTO.getProcessFileLocation());
                metaDataDBO.setProcessURL(metaDataDTO.getProcessURL());
                metaDataDBO.setFileStatus(metaDataDTO.getFileStatus());
                metaDataDBO.setProcessFormat(metaDataDTO.getProcessFormat());
                metaDataDBO.setJobId(metaDataDTO.getJobId());
            }

            if (metaDataDTO.getFileStatus().equalsIgnoreCase(IngestionConstants.FILE_STATUS_COMPLETE))
            {
                metaDataDBO.setJobId(metaDataDTO.getJobId());
                metaDataDBO.setFileStatus(metaDataDTO.getFileStatus());
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
     * Returns MetaDataDTO by metaDataId 
     *
     * @param metaDataId
     * @return MetaDataDTO
     * @throws IngestionException 
     */
    @Override
    public MetaDataDTO getMetaDataById(String metaDataId) throws IngestionException
    {

        logger.debug(" INGESTION SERVICE : GET METADATA OBJECT FROM DATABASE TO BY ID ");
        MetaDataModel metaDataDBO;
        try
        {
            //GET META-DATA OF FILE BY ID OF PARTICULAR FILE
            metaDataDBO = this.ingestionDao.getMetaDataById(metaDataId);
        }
        catch (NoSuchElementException e)
        {
            logger.error(" INGESTION SERVICE : COULD NOT FIND ENTITY WITH ID : " + metaDataId);
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        //MAP INTO DATA-TRANSFER OBJECT 
        MetaDataDTO metaDataDTO = MetaDataMapper.makeMetaDataDTO(metaDataDBO);

        return metaDataDTO;
    }


    /** 
     * Returns List<MetaDataDTO>  
     *
     * @param MultipartFile - Excel sheet contains name of files
     * @return List<MetaDataDTO> - list of uploaded files to amazon s3
     * @throws IngestionException 
     */
    @Override
    public List<MetaDataDTO> uploadMultipalFiles(MultipartFile excelFile) throws IngestionException
    {
        logger.info(" INGESTION SERVICE :  READING " + excelFile.getOriginalFilename() + " EXCEL FILE TO DOWNLOAD FILES FROM FTP START ");

        Map<String, Object> fileData = null;

        //READ EXCEL SHEET AND ADD FILE-NAME'S INTO LIST TO DOWNLOAD FROM FTP SERVER
        //IN THIS LOGIC WE ASSUME THAT FILE NAME IS AT FIRST COLUMN OF EVERY ROWES. 
        List<MetaDataDTO> metaDataDTOList = getExcelFileDetails(excelFile);

        //GET DISTINCT FILE NAMES FROM EXCEL SHEET
        List<String> excelFileNames = metaDataDTOList.stream().map(m -> m.getTitle()).distinct().collect(Collectors.toList());

        //TEMPRERY HARDCODED OBJECT IT CONTAINS CREDITIALS OF FTP USER'S.
        UserCredentials userCredentials = new UserCredentials("localhost", "rohan@hcl.com", "hcl", "21", excelFileNames);
        try
        {
            logger.debug(" INGESTION SERVICE :  DOWNLOADING REQUESTED FILE'S FROM FTP SERVER ");
            fileData = this.ftpStorage.downloadMultipalFiles(userCredentials);

        }
        catch (IOException | IngestionException e)
        {
            logger.error(" INGESTION SERVICE :  DOWNLOADING REQUESTED FILE'S FROM FTP SERVER FAILED ");
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        List<MetaDataModel> metaDataModelList = uploadMultipalFTPFiles(metaDataDTOList, fileData);

        List<MetaDataModel> metaDataDBOList = ingestionDao.saveMetaDataList(metaDataModelList);

        List<MetaDataDTO> metaDataDTOListResponse = MetaDataMapper.makeMetaDataDTOListResponse(metaDataDBOList);

        logger.info(" INGESTION SERVICE :  FILES FROM INPUT EXCEL SHEET " + excelFile.getOriginalFilename() + " INGESTED SUCCESSFULL ");

        return metaDataDTOListResponse;
    }


    /** 
     * Returns List<MetaDataDTO> - This Function reads Excel sheet and add details of files in MetaDataDTO and 
     *                             return it's list
     *
     * @param MultipartFile - Excel sheet contains name of files
     * @return List<MetaDataDTO> - list of files Details which we have to download from FTP server
     * @throws IngestionException 
     */
    private List<MetaDataDTO> getExcelFileDetails(MultipartFile excelFile) throws IngestionException
    {
        List<MetaDataDTO> metaDataDTOList = new ArrayList<MetaDataDTO>();
        try
        {
            Workbook workbook = WorkbookFactory.create(excelFile.getInputStream());
            workbook.forEach(
                sheet -> sheet.forEach(
                    row -> {
                        if (row.getRowNum() != 0)
                        {
                            Iterator<Cell> cell = row.cellIterator();
                            while (cell.hasNext())
                            {
                                MetaDataDTO ftpFileMetaData = new MetaDataDTO();
                                ftpFileMetaData.setTitle(cell.next().getStringCellValue());
                                ftpFileMetaData.setDescription(cell.next().getStringCellValue());
                                String[] strArray = new String[1];
                                strArray[0] = cell.next().getStringCellValue();
                                ftpFileMetaData.setTags(strArray);
                                ftpFileMetaData.setFileContentType(cell.next().getStringCellValue());
                                metaDataDTOList.add(ftpFileMetaData);
                            }
                        }
                    }));

            workbook.close();

        }
        catch (Exception e)
        {
            logger.error(" INGESTION SERVICE :   READING EXCEL FILE TO DOWNLOAD FILES FROM FTP IS FAILED EXCEPTION IS :- " + e.getMessage());
            throw new IngestionException(e.getMessage());
        }
        return metaDataDTOList;
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<MetaDataModel> uploadMultipalFTPFiles(List<MetaDataDTO> metaDataDTOList, Map<String, Object> fileData) throws IngestionException
    {
        List<MetaDataModel> metaDataModelListResponse = new ArrayList<>();
        Map<String, Object> fileMetaData = null;

        //MAP CONTAINS INPUTSTREAMS OF FILES . KEY IS NAME OF REQUESTED FILE AND VALUE IS INPUTSTREAM OF THAT FILE.
        Map<String, InputStream> mediaFileStreamMap = (Map<String, InputStream>) fileData.get(IngestionConstants.FTP_INPUTSTREAM);

        //MAP CONTAINS SIZE OF FILES . KEY IS NAME OF REQUESTED FILE AND VALUE IS SIZE OF THAT FILE..
        Map mediaFileSizeMap = (Map) fileData.get(IngestionConstants.FTP_CONTENTLENGTH);

        for (MetaDataDTO tempMetaDataDto : metaDataDTOList)
        {
            String ftpFileName = tempMetaDataDto.getTitle();

            //SET DOWNLOADED FILE'S INPUTSTRAM FROM MAP ACCORDING TO KEY
            InputStream mediaFileStream = (InputStream) mediaFileStreamMap.get(ftpFileName);

            //SET DOWNLOADED FILE'S SIZE FROM MAP ACCORDING TO KEY 
            Long contentSize = (Long) mediaFileSizeMap.get(ftpFileName);

            try
            {
                //SEND FILE'S INPUTSTREAM AMD OTHER DETAILS TO AMZONE SERVICE TO UPLOAD IN AMAZON S3 BUCKET  
                logger.debug(" INGESTION SERVICE :   UPLOADING " + ftpFileName + " FILE FROM FTP TO AMAZONE S3 BUCKET ");
                fileMetaData = ingestionStorageService.uploadFile(ftpFileName, mediaFileStream, tempMetaDataDto.getFileContentType(), contentSize);
            }
            catch (Exception e)
            {
                logger.error(" INGESTION SERVICE :   UPLOADING FILE TO AMAZONE S3 BUCKET FAILED ");
                throw new IngestionException(e.getMessage());
            }

            logger.debug(" INGESTION SERVICE :   FTP FILE " + ftpFileName + " SUCCESSFULLY UPLOADED TO AWS S3 BUCKET  ");

            MetaDataDTO metaDataDTO = getMetaDataDTO(fileMetaData, contentSize, tempMetaDataDto.getFileContentType());

            MetaDataModel metaDataModel = MetaDataMapper.makeMetaDataDBO(metaDataDTO);

            metaDataModel.setTitle(ftpFileName);
            metaDataModel.setDescription(tempMetaDataDto.getDescription());
            metaDataModel.setTags(tempMetaDataDto.getTags());

            metaDataModelListResponse.add(metaDataModel);

        }

        return metaDataModelListResponse;
    }

}
