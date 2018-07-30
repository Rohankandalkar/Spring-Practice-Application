package com.hcl.ott.ingestion.dao;

import java.util.List;

import com.hcl.ott.ingestion.model.MetaDataModel;

/**
 * Interface for Database Dao layer
 * 
 * @author kandalakar.r
 *
 */
public interface IngestionDao
{
    public MetaDataModel saveMetaData(MetaDataModel metaData);

    public List<MetaDataModel> findAll();
}
