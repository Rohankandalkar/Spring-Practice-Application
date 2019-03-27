package com.hcl.ott.ingestion.controller.mapper;

import com.hcl.ott.ingestion.data.IngestionResponseData;
import com.hcl.ott.ingestion.util.IngestionConstants;

/**
 * Mapper for content-response . It make custom content-response as per service result 
 * 
 * @author kandalakar.r
 *
 */
public class ContentMapper
{

    /**
     * Make IngestionResponseData to send custom response to user
     * 
     * @param data
     * @return IngestionResponseData
     */
    public static <T> IngestionResponseData<T> makeIngestionResponseData(T data)
    {
        IngestionResponseData<T> contentResponse = new IngestionResponseData<>(IngestionConstants.STATUS_FAILURE, " FAILED TO ADD FILE", data);

        if (data != null)
        {
            contentResponse.setStatus(IngestionConstants.STATUS_SUCCESS);
            contentResponse.setMessage(IngestionConstants.SUCCESS_MESSAGE);
            contentResponse.setData(data);
        }

        return contentResponse;
    }

}
