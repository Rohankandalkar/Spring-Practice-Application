package com.hcl.ott.ingestion.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Database Model(POJO) class 
 * 
 * @author kandalakar.r
 *
 */
@Entity
@Table(name = "metadata")
public class MetaDataModel
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "Title")
    private String title;

    @Column(name = "Desciption")
    private String description;

    @Column(name = "Tags")
    private String[] tags;

    @Column(name = "FileKey")
    private String fileKey;

    @Column(name = "FileSize")
    private Long fileSize;

    @Column(name = "FileContentType")
    private String fileContentType;

    @Column(name = "IngestionLocation")
    private String ingestionFileLocation;

    @Column(name = "IngestionURL")
    private String ingestionURL;

    @Column(name = "ProcessFileLocation")
    private String ProcessFileLocation;

    @Column(name = "ProcessURL")
    private String processURL;
    
    @Column(name = "processFormat")
    private String processFormat;

    @Column(name = "PublishFileLocation")
    private String PublishFileLocation;

    @Column(name = "PublishURL")
    private String publishURL;

    @Column(name = "FileStatus")
    private String fileStatus;


    public MetaDataModel()
    {
        // TODO Auto-generated constructor stub
    }


    

    public MetaDataModel(
        String title, String description, String[] tags, String fileKey, Long fileSize, String fileContentType, String ingestionFileLocation, String ingestionURL,
        String processFileLocation, String processURL, String processFormat, String publishFileLocation, String publishURL, String fileStatus)
    {
        super();
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.fileKey = fileKey;
        this.fileSize = fileSize;
        this.fileContentType = fileContentType;
        this.ingestionFileLocation = ingestionFileLocation;
        this.ingestionURL = ingestionURL;
        ProcessFileLocation = processFileLocation;
        this.processURL = processURL;
        this.processFormat = processFormat;
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




    public Long getId()
    {
        return id;
    }


    public void setId(Long id)
    {
        this.id = id;
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

}
