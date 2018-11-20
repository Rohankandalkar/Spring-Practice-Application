package com.hcl.ott.ingestion.model;

import java.util.Arrays;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Database Model(POJO) class 
 * 
 * @author kandalakar.r
 *
 */
@Document(collection = "metadata_model")
public class MetaDataModel
{
    @Id
    private String metaDataId;

    private String title;

    private String description;

    private String[] tags;

    private String fileKey;

    private Long fileSize;

    private String fileContentType;

    private String fileChecksum;

    private String ingestionFileLocation;

    private String ingestionURL;


    @Override
    public String toString()
    {
        return "MetaDataModel [metaDataId="
            + metaDataId + ", title=" + title + ", description=" + description + ", tags=" + Arrays.toString(tags) + ", fileKey=" + fileKey + ", fileSize=" + fileSize
            + ", fileContentType=" + fileContentType + ", fileChecksum=" + fileChecksum + ", ingestionFileLocation=" + ingestionFileLocation + ", ingestionURL=" + ingestionURL
            + ", ProcessFileLocation=" + ProcessFileLocation + ", processURL=" + processURL + ", processFormat=" + processFormat + ", jobId=" + jobId + ", PublishFileLocation="
            + PublishFileLocation + ", publishURL=" + publishURL + ", fileStatus=" + fileStatus + "]";
    }

    private String ProcessFileLocation;

    private String processURL;

    private String processFormat;

    private String jobId;

    private String PublishFileLocation;

    private String publishURL;

    private String fileStatus;


    public MetaDataModel()
    {
        // TODO Auto-generated constructor stub
    }


    public String getMetaDataId()
    {
        return metaDataId;
    }


    public void setMetaDataId(String metaDataId)
    {
        this.metaDataId = metaDataId;
    }


    public String getJobId()
    {
        return jobId;
    }


    public void setJobId(String jobId)
    {
        this.jobId = jobId;
    }


    public MetaDataModel(
        String title, String description, String[] tags, String fileKey, Long fileSize, String fileContentType, String fileChecksum, String ingestionFileLocation,
        String ingestionURL,
        String processFileLocation, String processURL, String processFormat, String jobId, String publishFileLocation, String publishURL, String fileStatus)
    {
        super();
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.fileKey = fileKey;
        this.fileSize = fileSize;
        this.fileContentType = fileContentType;
        this.fileChecksum = fileChecksum;
        this.ingestionFileLocation = ingestionFileLocation;
        this.ingestionURL = ingestionURL;
        ProcessFileLocation = processFileLocation;
        this.processURL = processURL;
        this.processFormat = processFormat;
        this.jobId = jobId;
        PublishFileLocation = publishFileLocation;
        this.publishURL = publishURL;
        this.fileStatus = fileStatus;
    }


    public String getProcessFormat()
    {
        return processFormat;
    }


    public void setProcessFormat(String processFormat)
    {
        this.processFormat = processFormat;
    }


    public String getId()
    {
        return metaDataId;
    }


    public void setId(String metaDataId)
    {
        this.metaDataId = metaDataId;
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


    public String getProcessFileLocation()
    {
        return ProcessFileLocation;
    }


    public void setProcessFileLocation(String processLocation)
    {
        this.ProcessFileLocation = processLocation;
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
        return PublishFileLocation;
    }


    public void setPublishFileLocation(String publishLocation)
    {
        PublishFileLocation = publishLocation;
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


    public String getFileChecksum()
    {
        return fileChecksum;
    }


    public void setFileChecksum(String fileChecksum)
    {
        this.fileChecksum = fileChecksum;
    }

}
