package com.hcl.ott.ingestion.data;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Class to transfer uploading file details to component 
 * 
 * @author kandalakar.r
 *
 */
public class FileDetailsData
{
    private String title;

    private String description;

    private String[] tags;

    private String fileKey;

    private Long fileSize;

    private String fileChecksum;

    private String fileContentType;

    private String ingestionFileLocation;

    private String ingestionURL;

    private String fileStatus;

    private File file;

    private InputStream fileInputStream;


    public FileDetailsData()
    {
        // TODO Auto-generated constructor stub
    }


    public FileDetailsData(
        String title, String description, String[] tags, String fileKey, Long fileSize, String fileChecksum, String fileContentType, String ingestionFileLocation,
        String ingestionURL, String fileStatus, File file, InputStream fileInputStream)
    {
        super();
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.fileKey = fileKey;
        this.fileSize = fileSize;
        this.fileChecksum = fileChecksum;
        this.fileContentType = fileContentType;
        this.ingestionFileLocation = ingestionFileLocation;
        this.ingestionURL = ingestionURL;
        this.fileStatus = fileStatus;
        this.file = file;
        this.fileInputStream = fileInputStream;
    }


    public InputStream getFileInputStream()
    {
        return fileInputStream;
    }


    public void setFileInputStream(InputStream fileInputStream)
    {
        this.fileInputStream = fileInputStream;
    }


    public File getFile()
    {
        return file;
    }


    public void setFile(File file)
    {
        this.file = file;
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


    public String getFileChecksum()
    {
        return fileChecksum;
    }


    public void setFileChecksum(String fileChecksum)
    {
        this.fileChecksum = fileChecksum;
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


    public String getFileStatus()
    {
        return fileStatus;
    }


    public void setFileStatus(String fileStatus)
    {
        this.fileStatus = fileStatus;
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


    public String getFileKey()
    {
        return fileKey;
    }


    public void setFileKey(String fileKey)
    {
        this.fileKey = fileKey;
    }


    @Override
    public String toString()
    {
        return "FileDetailsData [title="
            + title + ", description=" + description + ", tags=" + Arrays.toString(tags) + ", fileKey=" + fileKey + ", fileSize=" + fileSize + ", fileChecksum=" + fileChecksum
            + ", fileContentType=" + fileContentType + ", ingestionFileLocation=" + ingestionFileLocation + ", ingestionURL=" + ingestionURL + ", fileStatus=" + fileStatus
            + ", file=" + file + ",  fileInputStream=" + fileInputStream + "]";
    }

}
