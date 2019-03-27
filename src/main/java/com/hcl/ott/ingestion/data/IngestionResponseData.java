package com.hcl.ott.ingestion.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Class for custom Response Data  
 * 
 * @author kandalakar.r
 *
 * @param <T>
 */
@JsonInclude(Include.NON_NULL)
public class IngestionResponseData<T>
{
    private String message;
    private String status;
    private T data;


    public IngestionResponseData()
    {
        // TODO Auto-generated constructor stub
    }


    public IngestionResponseData(String message, String status, T data)
    {
        super();
        this.message = message;
        this.status = status;
        this.data = data;
    }


    public String getMessage()
    {
        return message;
    }


    public void setMessage(String message)
    {
        this.message = message;
    }


    public String getStatus()
    {
        return status;
    }


    public void setStatus(String status)
    {
        this.status = status;
    }


    public T getData()
    {
        return data;
    }


    public void setData(T data)
    {
        this.data = data;
    }

}
