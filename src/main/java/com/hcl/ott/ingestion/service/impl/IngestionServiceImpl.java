package com.hcl.ott.ingestion.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
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
import com.hcl.ott.ingestion.controller.mapper.FileDetailsDataMapper;
import com.hcl.ott.ingestion.controller.mapper.MetaDataMapper;
import com.hcl.ott.ingestion.dao.IngestionDao;
import com.hcl.ott.ingestion.data.FileDetailsData;
import com.hcl.ott.ingestion.data.MetaDataDTO;
import com.hcl.ott.ingestion.data.UserCredentials;
import com.hcl.ott.ingestion.exception.IngestionException;
import com.hcl.ott.ingestion.model.MetaDataModel;
import com.hcl.ott.ingestion.service.IngestionService;
import com.hcl.ott.ingestion.service.storage.IngestionStorageService;
import com.hcl.ott.ingestion.service.storage.Impl.FtpStorage;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * This method Upload's MultipartFile to configured storage - Amazon s3 bucket
     * 
     *@param MultipartFile - mediaFile
     *@param clientSideChecksum - checksum of mediaFile
     *@return MetaDataDTO - MetaData of uploaded file
     *@throws IngestionException , IOException
     *        
     */
    @Override
    public MetaDataDTO uploadFile(MultipartFile mediaFile, String clientSideChecksum) throws IngestionException, IOException
    {

        logger.debug(" INGESTION SERVICE : UPLOADING " + mediaFile.getOriginalFilename() + "FILE TO STORAGE START ");

        boolean basicValidationResult = basicFileValidation(mediaFile.getSize());

        if (!basicValidationResult)
            throw new IngestionException(
                " file size is above limit .please ingest file below 1 GB ");

        //CREATE UNIQUE KEY TO USE AS FILE NAME AT AWS S3 BUCKET
        String fileNameKey = generateFileKey(mediaFile.getOriginalFilename());

        //GENERATE FILE(TEMPERORY) TO UPLOAD FILE AT S3 STORAGE
        File serverSideTempFile = createTempFileFromInputStream(mediaFile.getInputStream(), fileNameKey);

        //IF USER NOT PROVIDE CHECKSUM THEN WE WILL CALCULATE AT SERVER SIDE FROM MULTIPART FILE 
        if (clientSideChecksum == null || clientSideChecksum.isEmpty())
            clientSideChecksum = calculateChecksum(mediaFile.getInputStream());

        boolean validationResult = fileIntegrityValidation(serverSideTempFile, clientSideChecksum);

        if (!validationResult)
        {
            serverSideTempFile.delete();
            throw new IngestionException(
                "validation of integrity of uploaded file gives negative feedback . "
                    + "Client calculated content hash contentMD5 didn't match hash calculated at server side ");

        }

        FileDetailsData fileDetailsUploadRequest = getFileDetailsUploadRequest(fileNameKey, serverSideTempFile, mediaFile.getContentType(), mediaFile.getSize());

        FileDetailsData uploadedFilesResponse = null;
        try
        {
            logger.debug(" INGESTION SERVICE : FILE TRANSFERED TO AMAZON SERVICE TO UPLOAD FILE IN S3 BUCKET ");
            uploadedFilesResponse = ingestionStorageService.uploadFile(fileDetailsUploadRequest);
        }
        catch (AmazonClientException | IOException | InterruptedException | ExecutionException exception)
        {
            logger.error(" FAILED TO UPLOAD FILE TO CONFIGURED STORAGE ");
            logger.error(exception.getMessage());
            throw new IngestionException(exception.getMessage());
        }

        if (uploadedFilesResponse.getFileStatus() == null || uploadedFilesResponse.getFileStatus().isEmpty())
            throw new IngestionException(" RESPONSE OF UPLOADED FILE REQUEST IS NULL FILE UPLOAD MAY BE FAILED ");

        //CHECK STATUS OF FILE IF ITS "DRAFTED" THEN ONLY CHANGE STATUS TO "INGESTED"
        if (uploadedFilesResponse.getFileStatus().equals(IngestionConstants.FILE_STATUS_INGESTION_PROCESS))
            uploadedFilesResponse.setFileStatus(IngestionConstants.FILE_STATUS_INGESTION);

        logger.debug(" INGESTION SERVICE : FILE SUCCESFULLY UPLOADED TO AMAZON S3 STORAGE ");
        //DELETE TEMP SERVER SIDE FILE AFTER UPLOADING TO AWS S3
        uploadedFilesResponse.getFile().delete();

        MetaDataDTO metaDataDTO = FileDetailsDataMapper.makeMetaDataDTO(uploadedFilesResponse);

        MetaDataModel metaDataModel = MetaDataMapper.makeMetaDataDBO(metaDataDTO);

        //SAVE METADATA OF FILE TO DATABASE WITH STATUS DRAFTED
        MetaDataModel metaDataDBO = this.ingestionDao.saveMetaData(metaDataModel);

        logger.debug(" META - DATA OF " + metaDataDBO.getTitle() + " IS ADDED TO DATABASE SUCCESSFULLY ");

        //MAPING META-DATA DATABASE OBJECT WITH DATA-TRANSFER OBJECT
        MetaDataDTO metaDataDT = MetaDataMapper.makeMetaDataDTO(metaDataDBO);

        return metaDataDT;
    }


    /**
     * This method returns FileDetailsData object by setting given parameters
     * 
     * @param fileNameKey
     * @param serverSideTempFile
     * @param contentType
     * @param size
     * @return FileDetailsData
     */
    private FileDetailsData getFileDetailsUploadRequest(String fileNameKey, File serverSideTempFile, String contentType, long size)
    {

        FileDetailsData fileDetailsUploadRequest = new FileDetailsData();
        fileDetailsUploadRequest.setFileKey(serverSideTempFile.getName());
        fileDetailsUploadRequest.setFile(serverSideTempFile);
        fileDetailsUploadRequest.setFileContentType(contentType);
        fileDetailsUploadRequest.setFileSize(size);
        return fileDetailsUploadRequest;
    }


    /** 
     * This method save uploaded file's meta-data to database
     * 
     *@param MetaDataDTO - Data transfer object
     *@return MetaDataDTO - returns saved DB object data using DTO.
     */
    @Override
    public MetaDataDTO saveFileMetaData(MetaDataDTO metaDataDTO)
    {
        logger.debug(" INGESTION SERVICE : SAVE FINAL META-DATA OF FILE TO DATABASE ");

        MetaDataModel metaDataDBO = MetaDataMapper.makeMetaDataDBO(metaDataDTO);

        //SAVE META-DATA OF FILE TO DATABASE WITH STATUS INGESTED
        MetaDataModel metaDataDB = ingestionDao.saveMetaData(metaDataDBO);

        MetaDataDTO metaDataDT = MetaDataMapper.makeMetaDataDTO(metaDataDB);

        logger.debug(" INGESTION SERVICE :  META - DATA OF " + metaDataDBO.getTitle() + " IS ADDED TO DATABASE SUCCESSFULLY ");

        return metaDataDT;

    }


    /**
     * This method returns all List of uploaded files Meta-Data 
     *
     *@param pageable 
     *@return List<MetaDataDTO>
     *
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
     * This method Upload file from FTP Server to configured storage
     *  
     * @param UserCredentials - contains Credentials of FTP server and File Details
     * @return MetaDataDTO
     * @throws IngestionException, IOException
     */
    @Override
    public MetaDataDTO uploadFtpFile(UserCredentials userCredentials) throws IngestionException, IOException
    {
        logger.debug(" INGESTION SERVICE : UPLOADING FTP FILE TO STORAGE  ");

        FileDetailsData ftpDownloadRequestFileDetails = new FileDetailsData();
        ftpDownloadRequestFileDetails.setTitle(userCredentials.getRemoteFile().get(0));
        ftpDownloadRequestFileDetails.setFileChecksum(userCredentials.getClientSideChecksum());

        FileDetailsData ftpDownloadedFileDetails = null;
        try
        {
            //DOWNLOADING REQUESTED FILE FROM FTP SERVER 
            logger.debug(" INGESTION SERVICE : DOWNLOADING REQUESTED FILE FROM FTP SERVER ");
            ftpDownloadedFileDetails = this.ftpStorage.downloadFile(userCredentials, ftpDownloadRequestFileDetails);
        }
        catch (Exception e)
        {
            logger.error(" INGESTION SERVICE : DOWNLOADING REQUESTED FILE FROM FTP SERVER FAILED ");
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        if (ftpDownloadedFileDetails == null || ftpDownloadedFileDetails.getFileInputStream() == null)
            throw new IngestionException(" FILE NOT DOWNLOADED FROM FTP SERVER.IT RETURN NULL AS VALUE FROM FTP SERVER ");

        logger.debug("  INGESTION SERVICE : SUCCESSFULLY DOWNLOADED REQUESTED FILE FROM FTP SERVER ");
        boolean basicValidationResult = basicFileValidation(ftpDownloadedFileDetails.getFileSize());

        if (!basicValidationResult)
            throw new IngestionException(
                " file size is above limit .please ingest file below 1 GB ");

        String fileNameKey = generateFileKey(ftpDownloadedFileDetails.getTitle());

        File serverSideTempFile = createTempFileFromInputStream(ftpDownloadedFileDetails.getFileInputStream(), fileNameKey);

        //IF USER DIDN'T PROVIDE CHECKSUM THEN WE WILL CALCULATE AT SERVER SIDE FROM MULTIPART FILE 
        if (ftpDownloadedFileDetails.getFileChecksum() == null || ftpDownloadedFileDetails.getFileChecksum().isEmpty())
            ftpDownloadedFileDetails.setFileChecksum(getMd5Digest(serverSideTempFile));

        boolean validationResult = fileIntegrityValidation(serverSideTempFile, ftpDownloadedFileDetails.getFileChecksum());

        if (!validationResult)
        {
            serverSideTempFile.delete();
            throw new IngestionException(
                "validation of integrity of uploaded file gives negative feedback . Client calculated content hash contentMD5 didn't match hash calculated at server side ");

        }

        ftpDownloadedFileDetails.setFileKey(serverSideTempFile.getName());
        ftpDownloadedFileDetails.setFile(serverSideTempFile);
        ftpDownloadedFileDetails.setFileContentType(IngestionConstants.FILE_CONTENT_TYPE_VIDEO);

        FileDetailsData uploadedFileResponseDetails = null;

        try
        {
            //UPLOADING FTP FILE TO AMAZONE S3 BUCKET 
            logger.debug(" INGESTION SERVICE : UPLOADING DOWNLOADED FTP FILE TO AMAZONE S3 BUCKET ");
            uploadedFileResponseDetails = ingestionStorageService.uploadFile(ftpDownloadedFileDetails);
        }
        catch (AmazonClientException | IOException | InterruptedException | IngestionException | ExecutionException e)
        {
            logger.error(" INGESTION SERVICE : UPLOADING FILE TO AMAZONE S3 BUCKET FAILED ");
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        if (uploadedFileResponseDetails == null || uploadedFileResponseDetails.getFileStatus() == null || uploadedFileResponseDetails.getFileStatus().isEmpty())
            throw new IngestionException(" RESPONSE OF UPLOADED FILE REQUEST IS NULL.FILE UPLOAD MAY BE FAILED ");

        if (uploadedFileResponseDetails.getFileStatus().equals(IngestionConstants.FILE_STATUS_INGESTION_PROCESS))
            uploadedFileResponseDetails.setFileStatus(IngestionConstants.FILE_STATUS_INGESTION);

        logger.debug(" INGESTION SERVICE : FTP FILE SUCCESSFULLY UPLOADED TO STORAGE ");

        MetaDataDTO metaDataDTO = FileDetailsDataMapper.makeMetaDataDTO(uploadedFileResponseDetails);

        MetaDataModel metaDataModel = MetaDataMapper.makeMetaDataDBO(metaDataDTO);

        //SAVE META-DATA OF FTP FILE TO DATABASE WITH STATUS DRAFTED
        MetaDataModel metaDataDBO = this.ingestionDao.saveMetaData(metaDataModel);

        logger.debug(" INGESTION SERVICE : META - DATA OF " + metaDataDBO.getTitle() + " IS ADDED TO DATABASE SUCCESSFULLY ");

        MetaDataDTO metaDataDT = MetaDataMapper.makeMetaDataDTO(metaDataDBO);

        return metaDataDT;
    }


    /**
     * This method update file's meta-data after processing 
     *  
     * @param MetaDataDTO - Object which is to be update
     * @return MetaDataDTO - Object after updating
     * @throws IngestionException 
     */
    @Override
    public MetaDataDTO updateMetatdateStatus(MetaDataDTO metaDataDTO) throws IngestionException
    {
        logger.debug(" INGESTION SERVICE : UPDATING STATUS OF FILE'S META-DATA ");

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
        logger.debug(" INGESTION SERVICE : SUCCESSFULLY UPDATED STATUS OF FILE'S META-DATA ");

        return metaDataDT;
    }


    /** 
     * This method returns MetaData of file by metaDataId 
     *
     * @param metaDataId
     * @return MetaDataDTO - Domain object mapped into DTO object and return DTO object
     * @throws IngestionException 
     */
    @Override
    public MetaDataDTO getMetaDataById(String metaDataId) throws IngestionException
    {

        logger.debug(" INGESTION SERVICE : GET METADATA OBJECT FROM DATABASE BY ID ");
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
     * This method uploads all specified files to the bucket named.
     * In this we read excel file first and then extract file's data from excel file.
     * and upload MultipalFile's to aws s3 .and save its meta-data to local database and return list of meta-data .
     * 
     * @param MultipartFile - Excel sheet contains name of files to be upload.
     * @return List<MetaDataDTO> - list of uploaded files meta-data.
     * 
     * @throws IngestionException 
     * 
     */
    @Override
    public List<MetaDataDTO> uploadMultipalFiles(MultipartFile excelFile)
        throws IngestionException
    {
        logger.debug(" INGESTION SERVICE :  READING " + excelFile.getOriginalFilename() + " EXCEL FILE TO DOWNLOAD FILES FROM FTP START ");

        //READ EXCEL SHEET AND ADD FILE-NAME'S INTO LIST TO DOWNLOAD FROM FTP SERVER
        //IN THIS LOGIC WE ASSUME THAT FILE NAME IS AT FIRST COLUMN OF EVERY ROWES. 
        List<FileDetailsData> ftpFileDownloadRequestDetails;
        try
        {
            ftpFileDownloadRequestDetails = getExcelFileDetails(excelFile);
        }
        catch (IngestionException | AmazonClientException e)
        {
            logger.error(" INGESTION SERVICE :  READING EXCEL SHEET FAILED ");
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        if (ftpFileDownloadRequestDetails == null || ftpFileDownloadRequestDetails.isEmpty() || ftpFileDownloadRequestDetails.size() == 0)
            throw new IngestionException(" Failed to read Excel file ");

        logger.debug(" INGESTION SERVICE :  EXCEL SHEET SUCCESFULLY READ AND EXTRACTED FILES DATA FROM EXCEL SHEET ");
        //OBJECT IT CONTAINS CREDITIALS OF FTP USER'S.
        UserCredentials userCredentials = new UserCredentials("localhost", "rohan@hcl.com", "hcl", "21", null);
        List<FileDetailsData> ftpFileDownloadedResponseDetails = new ArrayList<>(ftpFileDownloadRequestDetails.size());
        try
        {
            logger.debug(" INGESTION SERVICE :  DOWNLOADING REQUESTED FILE'S FROM FTP SERVER ");
            ftpFileDownloadedResponseDetails = this.ftpStorage.downloadMultipalFiles(userCredentials, ftpFileDownloadRequestDetails);

        }
        catch (IOException | IngestionException e)
        {
            logger.error(" INGESTION SERVICE :  DOWNLOADING REQUESTED FILE'S FROM FTP SERVER FAILED ");
            logger.error(e.getMessage());
            throw new IngestionException(e.getMessage());
        }

        ftpFileDownloadedResponseDetails.stream().forEach(ftpFile -> {

            String fileKey = generateFileKey(ftpFile.getTitle());
            File serverSideTempFile = createTempFileFromInputStream(ftpFile.getFileInputStream(), fileKey);

            ftpFile.setFile(serverSideTempFile);
            ftpFile.setFileKey(serverSideTempFile.getName());

        });

        List<FileDetailsData> amazonUploadResponseList = null;

        try
        {
            logger.debug(" INGESTION SERVICE :  SEND UPLOAD REQUESTE TO AWS COMPONANT ");
            amazonUploadResponseList = this.ingestionStorageService.uploadFileList(ftpFileDownloadedResponseDetails);
        }
        catch (AmazonClientException | InterruptedException exception)
        {
            logger.error(" FAILED TO UPLOAD FILE'S TO CONFIGURED STORAGE ");
            logger.error(exception.getMessage());
            throw new IngestionException(exception.getMessage());
        }

        if (amazonUploadResponseList == null || amazonUploadResponseList.isEmpty())
            throw new IngestionException(" RESPONSE OF UPLOADED FILE REQUEST IS NULL FILE UPLOAD MAY BE FAILED ");

        List<FileDetailsData> amazoneIngestedlist =
            amazonUploadResponseList
                .stream()
                .filter(fileMetaData -> fileMetaData.getFileStatus().equals(IngestionConstants.FILE_STATUS_INGESTION_PROCESS))
                .collect(Collectors.toList());

        amazoneIngestedlist.stream().forEach(fileMetaData -> fileMetaData.setFileStatus(IngestionConstants.FILE_STATUS_INGESTION));

        List<MetaDataDTO> metaDataDTOList = FileDetailsDataMapper.makeMetaDataDTOList(amazoneIngestedlist);

        List<MetaDataModel> metaDataModelList = MetaDataMapper.makeMetaDataDBOList(metaDataDTOList);

        //SAVE META-DATA OF FILE TO LOCAL DATA-BASE
        List<MetaDataModel> metaDataDBOList = ingestionDao.saveMetaDataList(metaDataModelList);

        List<MetaDataDTO> metaDataDTOListResponse = MetaDataMapper.makeMetaDataDTOListResponse(metaDataDBOList);

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
    private List<FileDetailsData> getExcelFileDetails(MultipartFile excelFile) throws IngestionException
    {
        List<FileDetailsData> ftpFileDetails = new ArrayList<FileDetailsData>();
        try
        {

            Workbook workbook = WorkbookFactory.create(excelFile.getInputStream());

            workbook
                .forEach(
                    sheet -> sheet
                        .forEach(
                            row -> {
                                if (row.getRowNum() != 0)
                                {
                                    Iterator<Cell> cell = row.cellIterator();
                                    while (cell.hasNext())
                                    {
                                        FileDetailsData ftpFileData = new FileDetailsData();

                                        ftpFileData.setTitle(cell.next().getStringCellValue());
                                        ftpFileData.setDescription(cell.next().getStringCellValue());

                                        String[] tags = new String[1];
                                        tags[0] = cell.next().getStringCellValue();
                                        ftpFileData.setTags(tags);
                                        ftpFileData.setFileContentType(cell.next().getStringCellValue());
                                        ftpFileData.setFileChecksum(cell.next().getStringCellValue());
                                        ftpFileDetails.add(ftpFileData);
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
        return ftpFileDetails;
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
     * Returns unique key.which is used as name at amazon s3 bucket.
     * 
     * @param fileName
     * @return key - unique key
     */
    private String generateFileKey(String fileName)
    {
        String uniqueID = UUID.randomUUID().toString();

        String key = uniqueID + "_" + fileName;

        String s3objectKey = key.replaceAll("\\.", "_");

        return s3objectKey;
    }


    /**
     * Calculate MD5checksum from InputStream  
     * 
     * @param MultipartFile
     * @return String - checksum of MultipartFile
     */
    private String calculateChecksum(InputStream mediaFileInputStream) throws IOException
    {
        return DigestUtils.md5Hex(mediaFileInputStream);
    }


    /**
     * Create file on server side before upload to aws s3  
     * so we create file using this function
     * 
     * @param fileStream
     * @param fileKey
     * @return tempFile
     * @throws IOException
     */
    private File createTempFileFromInputStream(InputStream fileStream, String fileKey)
    {

        String suffix = fileKey.substring(fileKey.lastIndexOf("_"));

        File tempFile = null;
        try
        {
            tempFile = File.createTempFile(fileKey, suffix);
            FileUtils.copyInputStreamToFile(fileStream, tempFile);

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return tempFile;
    }


    /**
     * Basic File validation.Compare file size of server side file.It should be less than 1 GB
     * RETURN true - if file size is less than 1 GB
     * RETURN FALSE - if file size is greater than 1 GB
     * @param fileSize
     * @return
     */
    private boolean basicFileValidation(long fileSize)
    {
        boolean validationResult = false;

        if (fileSize < IngestionConstants.FILE_UPLOAD_LIMIT)
            validationResult = true;

        return validationResult;
    }


    /**
     *  Calculates the MD5 digest of the given File serverTempFile .
     *  It will generate a 32 characters hex string.
     * @param serverTempFile
     * @return
     * @throws IOException 
     */
    private String getMd5Digest(File serverTempFile) throws IOException
    {
        return DigestUtils.md5Hex(new FileInputStream(serverTempFile));
    }


    /** 
     * validate serverSide file and client side file and return true or false
     * true - when checksum of local file and client side file are equal 
     * false - when checksum of local file and client side file are not equal 
     *@param tempFile - serverSideTempFile
     *@param clientSideChecksum - clientSideChecksum
     *@return boolean - returns true or false according to there comparison.
     */
    private boolean fileIntegrityValidation(File serverSideTempFile, String clientSideChecksum) throws IOException
    {
        boolean validationResult = false;
        String serverSideChecksum = getMd5Digest(serverSideTempFile);

        //VALIDATION OF DATA INTEGRITY
        if (serverSideChecksum.equals(clientSideChecksum))
            validationResult = true;

        return validationResult;
    }

}
