package com.hcl.ott.ingestion.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.hcl.ott.ingestion.data.IngestionResponseData;
import com.hcl.ott.ingestion.exception.IngestionException;
import com.hcl.ott.ingestion.util.IngestionConstants;

/**
 * Exception Handler contoller 
 * 
 * @author kandalakar.r
 *
 */
@ControllerAdvice
public class IngestionExceptionController
{

    @ExceptionHandler(value = {IngestionException.class, IOException.class})
    protected ResponseEntity<IngestionResponseData<String>> AssetExceptionHandler(Exception exception)
    {

        IngestionResponseData<String> contentResponse = new IngestionResponseData<>();
        contentResponse.setStatus(IngestionConstants.STATUS_FAILURE);
        contentResponse.setMessage(exception.getMessage());

        return new ResponseEntity<IngestionResponseData<String>>(contentResponse, HttpStatus.OK);
    }

}
