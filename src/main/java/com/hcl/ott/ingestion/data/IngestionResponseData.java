package com.hcl.ott.ingestion.data;

/**
 * Class for custome Ingestion Response Data  
 * 
 * @author kandalakar.r
 *
 * @param <T>
 */
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
