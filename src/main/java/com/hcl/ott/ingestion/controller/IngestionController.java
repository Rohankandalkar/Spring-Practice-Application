package com.hcl.ott.ingestion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.hcl.ott.ingestion.exception.IngestionException;
import com.hcl.ott.ingestion.model.UserCredentials;
import com.hcl.ott.ingestion.service.IngestionService;

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
    private IngestionService ingestionService;


    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<List<MetaDataDTO>>> getAllFiles() throws IngestionException
    {
        List<MetaDataDTO> metaDataList = this.ingestionService.getAllFiles();
        IngestionResponseData<List<MetaDataDTO>> IngestionResponseData = ContentMapper.makeIngestionResponseData(metaDataList);
        return new ResponseEntity<IngestionResponseData<List<MetaDataDTO>>>(IngestionResponseData, HttpStatus.OK);

    }


    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<MetaDataDTO>> uploadFile(@RequestParam(value = "file", required = true) MultipartFile mediaFile) throws IngestionException
    {
        MetaDataDTO fileMetaData = this.ingestionService.uploadFile(mediaFile);
        IngestionResponseData<MetaDataDTO> IngestionResponseData = ContentMapper.makeIngestionResponseData(fileMetaData);
        return new ResponseEntity<IngestionResponseData<MetaDataDTO>>(IngestionResponseData, HttpStatus.CREATED);

    }


    @RequestMapping(value = "/metadata", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<MetaDataDTO>> saveMetaData(@RequestBody MetaDataDTO metaDataDTO)
    {
        MetaDataDTO metaDataDT = this.ingestionService.saveFileMetaData(metaDataDTO);
        IngestionResponseData<MetaDataDTO> IngestionResponseData = ContentMapper.makeIngestionResponseData(metaDataDT);
        return new ResponseEntity<IngestionResponseData<MetaDataDTO>>(IngestionResponseData, HttpStatus.OK);
    }


    @RequestMapping(value = "/ftp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<MetaDataDTO>> uploadFtpFile(@RequestBody UserCredentials userCredentials)
        throws IngestionException

    {
        MetaDataDTO metaDataDT = this.ingestionService.uploadFtpFile(userCredentials);
        IngestionResponseData<MetaDataDTO> IngestionResponseData = ContentMapper.makeIngestionResponseData(metaDataDT);
        return new ResponseEntity<IngestionResponseData<MetaDataDTO>>(IngestionResponseData, HttpStatus.CREATED);

    }


    @RequestMapping(value = "/status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<MetaDataDTO>> updateMetaDataStatus(@RequestBody MetaDataDTO metaDataDTO) throws IngestionException
    {
        MetaDataDTO metaDataDT = this.ingestionService.updateMetatdateStatus(metaDataDTO);
        IngestionResponseData<MetaDataDTO> IngestionResponseData = ContentMapper.makeIngestionResponseData(metaDataDT);
        return new ResponseEntity<IngestionResponseData<MetaDataDTO>>(IngestionResponseData, HttpStatus.OK);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<MetaDataDTO>> getMetaData(@PathVariable String id) throws IngestionException
    {
        MetaDataDTO metaDataDT = this.ingestionService.getMetaData(id);
        IngestionResponseData<MetaDataDTO> IngestionResponseData = ContentMapper.makeIngestionResponseData(metaDataDT);
        return new ResponseEntity<IngestionResponseData<MetaDataDTO>>(IngestionResponseData, HttpStatus.OK);
    }

}
