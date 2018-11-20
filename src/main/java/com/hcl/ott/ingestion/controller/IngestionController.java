package com.hcl.ott.ingestion.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hcl.ott.ingestion.controller.mapper.ContentMapper;
import com.hcl.ott.ingestion.data.IngestionResponseData;
import com.hcl.ott.ingestion.data.MetaDataDTO;
import com.hcl.ott.ingestion.data.UserCredentials;
import com.hcl.ott.ingestion.exception.IngestionException;
import com.hcl.ott.ingestion.service.Async.IngestionAsyncService;

/**
 * File Ingestion RestController 
 * 
 * @author kandalakar.r
 * 
 */
@RestController
@RequestMapping(value = "/ingestion")
public class IngestionController
{
    @Autowired
    @Qualifier("IngestionAsyncService")
    private IngestionAsyncService ingestionAsyncService;


    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<List<MetaDataDTO>>> getAllFiles(Pageable pageable) throws IngestionException, InterruptedException, ExecutionException
    {
        CompletableFuture<List<MetaDataDTO>> metaDataList = this.ingestionAsyncService.getAllFiles(pageable);
        IngestionResponseData<List<MetaDataDTO>> IngestionResponseData = ContentMapper.makeIngestionResponseData(metaDataList.get());
        return new ResponseEntity<IngestionResponseData<List<MetaDataDTO>>>(IngestionResponseData, HttpStatus.OK);

    }


    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<MetaDataDTO>> uploadFile(
        @RequestBody(required = true) MultipartFile file, @RequestParam(value = "fileChecksum", required = false) String fileChecksum)
        throws IngestionException, InterruptedException, ExecutionException, IOException
    {
        CompletableFuture<MetaDataDTO> fileMetaData = this.ingestionAsyncService.uploadFile(file, fileChecksum);
        IngestionResponseData<MetaDataDTO> IngestionResponseData = ContentMapper.makeIngestionResponseData(fileMetaData.get());
        return new ResponseEntity<IngestionResponseData<MetaDataDTO>>(IngestionResponseData, HttpStatus.CREATED);

    }


    @RequestMapping(value = "/ftp/files", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<List<MetaDataDTO>>> uploadMultipalFiles(@RequestBody MultipartFile excelFile)
        throws IngestionException, InterruptedException, ExecutionException, IOException
    {
        CompletableFuture<List<MetaDataDTO>> fileMetaData = this.ingestionAsyncService.uploadMultipalFiles(excelFile);
        IngestionResponseData<List<MetaDataDTO>> IngestionResponseData = ContentMapper.makeIngestionResponseData(fileMetaData.get());
        return new ResponseEntity<IngestionResponseData<List<MetaDataDTO>>>(IngestionResponseData, HttpStatus.CREATED);

    }


    @RequestMapping(value = "/metadata", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<MetaDataDTO>> saveMetaData(@RequestBody MetaDataDTO metaDataDTO) throws InterruptedException, ExecutionException
    {
        CompletableFuture<MetaDataDTO> metaDataDT = this.ingestionAsyncService.saveFileMetaData(metaDataDTO);
        IngestionResponseData<MetaDataDTO> IngestionResponseData = ContentMapper.makeIngestionResponseData(metaDataDT.get());
        return new ResponseEntity<IngestionResponseData<MetaDataDTO>>(IngestionResponseData, HttpStatus.OK);
    }


    @RequestMapping(value = "/ftp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<MetaDataDTO>> uploadFtpFile(@RequestBody UserCredentials userCredentials)
        throws IngestionException, InterruptedException, ExecutionException, IOException

    {
        CompletableFuture<MetaDataDTO> metaDataDT = this.ingestionAsyncService.uploadFtpFile(userCredentials);
        IngestionResponseData<MetaDataDTO> IngestionResponseData = ContentMapper.makeIngestionResponseData(metaDataDT.get());
        return new ResponseEntity<IngestionResponseData<MetaDataDTO>>(IngestionResponseData, HttpStatus.CREATED);
    }


    @RequestMapping(value = "/status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<MetaDataDTO>> updateMetaDataStatus(@RequestBody MetaDataDTO metaDataDTO)
        throws IngestionException, InterruptedException, ExecutionException
    {
        CompletableFuture<MetaDataDTO> metaDataDT = this.ingestionAsyncService.updateMetatdateStatus(metaDataDTO);
        IngestionResponseData<MetaDataDTO> IngestionResponseData = ContentMapper.makeIngestionResponseData(metaDataDT.get());
        return new ResponseEntity<IngestionResponseData<MetaDataDTO>>(IngestionResponseData, HttpStatus.OK);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<MetaDataDTO>> getMetaData(@PathVariable("id") String id) throws IngestionException, InterruptedException, ExecutionException
    {
        CompletableFuture<MetaDataDTO> metaDataDT = this.ingestionAsyncService.getMetaDataById(id);
        IngestionResponseData<MetaDataDTO> IngestionResponseData = ContentMapper.makeIngestionResponseData(metaDataDT.get());
        return new ResponseEntity<IngestionResponseData<MetaDataDTO>>(IngestionResponseData, HttpStatus.OK);
    }

}
