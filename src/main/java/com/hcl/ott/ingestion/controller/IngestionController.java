package com.hcl.ott.ingestion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.hcl.ott.ingestion.service.IngestionService;

/**
 * File Injestion RestController 
 * 
 * @author kandalakar.r
 * 
 */
@RestController
@CrossOrigin(origins = {"http://localhost:4200"})
@RequestMapping(value = "/injestion")
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
    public ResponseEntity<IngestionResponseData<String>> uploadFile(@RequestParam(value = "file", required = true) MultipartFile mediaFile) throws IngestionException
    {
        String fileLocation = this.ingestionService.uploadFile(mediaFile);
        IngestionResponseData<String> IngestionResponseData = ContentMapper.makeIngestionResponseData(fileLocation);
        return new ResponseEntity<>(IngestionResponseData, HttpStatus.CREATED);

    }


    @RequestMapping(value = "/metadata", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<MetaDataDTO>> saveMetaData(@RequestBody MetaDataDTO metaDataDTO)
    {
        MetaDataDTO metaDataDT = this.ingestionService.saveFileMetaData(metaDataDTO);
        IngestionResponseData<MetaDataDTO> IngestionResponseData = ContentMapper.makeIngestionResponseData(metaDataDT);
        return new ResponseEntity<IngestionResponseData<MetaDataDTO>>(IngestionResponseData, HttpStatus.OK);
    }


    @RequestMapping(value = "/ftp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IngestionResponseData<String>> uploadFtpFile(
        @RequestParam(value = "host") String host, @RequestParam(value = "user") String user, @RequestParam(value = "password") String password,
        @RequestParam(value = "port") String port, @RequestParam(value = "file") String remoteFile)
        throws IngestionException

    {
        String fileLocation = this.ingestionService.uploadFtpFile(host, user, password, port, remoteFile);
        IngestionResponseData<String> IngestionResponseData = ContentMapper.makeIngestionResponseData(fileLocation);
        return new ResponseEntity<IngestionResponseData<String>>(IngestionResponseData, HttpStatus.CREATED);

    }

}
