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

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Desciption")
    private String description;

    @Column(name = "Tags")
    private String[] tags;

    @Column(name = "Location")
    private String location;


    public MetaDataModel()
    {
        // TODO Auto-generated constructor stub
    }


    public MetaDataModel(String title, String description, String[] tags, String location)
    {
        super();
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.location = location;
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


    public String getLocation()
    {
        return location;
    }


    public void setLocation(String location)
    {
        this.location = location;
    }

}
