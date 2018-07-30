package com.hcl.ott.ingestion.data;

/**
 * Mapper class for Database Model object
 * 
 * @author kandalakar.r
 *
 */
public class MetaDataDTO
{

    private String title;

    private String description;

    private String[] tags;

    private String location;


    public MetaDataDTO()
    {

    }


    public MetaDataDTO(String title, String description, String[] tags, String location)
    {
        super();
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.location = location;
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


    public String getLocation()
    {
        return location;
    }


    public void setLocation(String location)
    {
        this.location = location;
    }

}
