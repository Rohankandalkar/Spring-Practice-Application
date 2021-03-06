package com.hcl.ott.ingestion.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * DTO class of MetaDataModel
 * 
 * @author kandalakar.r
 *
 */
@JsonInclude(Include.NON_NULL)
public class MetaDataDTO
{

    private String id;
    private String title;
    private String description;
    private String[] tags;
    private String fileKey;
    private Long fileSize;
    private String fileChecksum;
    private String fileContentType;
    private String ingestionFileLocation;
    private String ingestionURL;
    private List<String> processFileLocation;//change
    private String processURL;
    private List<String> processFormat; //change
    private String jobId;
    private String publishFileLocation;
    private String publishURL;
    private String fileStatus;


    public MetaDataDTO()
    {
        // TODO Auto-generated constructor stub
    }


    public String getJobId()
    {
        return jobId;
    }


    public void setJobId(String jobId)
    {
        this.jobId = jobId;
    }


    public MetaDataDTO(
        String id, String title, String description, String[] tags, String fileKey, Long fileSize, String fileContentType, String ingestionFileLocation, String ingestionURL,
        List<String> processFileLocation, String processURL, List<String> processFormat, String jobId, String publishFileLocation, String publishURL, String fileStatus)
    {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.fileKey = fileKey;
        this.fileSize = fileSize;
        this.fileContentType = fileContentType;
        this.ingestionFileLocation = ingestionFileLocation;
        this.ingestionURL = ingestionURL;
        this.processFileLocation = processFileLocation;
        this.processURL = processURL;
        this.processFormat = processFormat;
        this.jobId = jobId;
        this.publishFileLocation = publishFileLocation;
        this.publishURL = publishURL;
        this.fileStatus = fileStatus;
    }


    public List<String> getProcessFormat()
    {
        return processFormat;
    }


    public void setProcessFormat(List<String> processFormat)
    {
        this.processFormat = processFormat;
    }


    public String getId()
    {
        return id;
    }


    public void setId(String id)
    {
        this.id = id;
    }


    public String getFileChecksum()
    {
        return fileChecksum;
    }


    public void setFileChecksum(String fileChecksum)
    {
        this.fileChecksum = fileChecksum;
    }


    public String getTitle()
    {
        return title;
    }


    public void setTitle(String title)
    {
        this.title = title;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    public String[] getTags()
    {
        return tags;
    }


    public void setTags(String[] tags)
    {
        this.tags = tags;
    }


    public String getFileKey()
    {
        return fileKey;
    }


    public void setFileKey(String fileKey)
    {
        this.fileKey = fileKey;
    }


    public Long getFileSize()
    {
        return fileSize;
    }


    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }


    public String getFileContentType()
    {
        return fileContentType;
    }


    public void setFileContentType(String fileContentType)
    {
        this.fileContentType = fileContentType;
    }


    public String getIngestionFileLocation()
    {
        return ingestionFileLocation;
    }


    public void setIngestionFileLocation(String ingestionFileLocation)
    {
        this.ingestionFileLocation = ingestionFileLocation;
    }


    public String getIngestionURL()
    {
        return ingestionURL;
    }


    public void setIngestionURL(String ingestionURL)
    {
        this.ingestionURL = ingestionURL;
    }


    public List<String> getProcessFileLocation()
    {
        return processFileLocation;
    }


    public void setProcessFileLocation(List<String> processFileLocation)
    {
        this.processFileLocation = processFileLocation;
    }


    public String getProcessURL()
    {
        return processURL;
    }


    public void setProcessURL(String processURL)
    {
        this.processURL = processURL;
    }


    public String getPublishFileLocation()
    {
        return publishFileLocation;
    }


    public void setPublishFileLocation(String publishFileLocation)
    {
        this.publishFileLocation = publishFileLocation;
    }


    public String getPublishURL()
    {
        return publishURL;
    }


    public void setPublishURL(String publishURL)
    {
        this.publishURL = publishURL;
    }


    public String getFileStatus()
    {
        return fileStatus;
    }


    public void setFileStatus(String fileStatus)
    {
        this.fileStatus = fileStatus;
    }

}
