package com.hcl.ott.ingestion.dao;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hcl.ott.ingestion.model.MetaDataModel;

/**
 * Interface for Database Dao layer
 * 
 * @author kandalakar.r
 *
 */
public interface IngestionDao
{
    MetaDataModel saveMetaData(MetaDataModel metaData);


    Page<MetaDataModel> findAll(Pageable pageable);


    MetaDataModel getMetaDataById(String id) throws NoSuchElementException;


    MetaDataModel getMetaDataByJobId(String title);


    List<MetaDataModel> saveMetaDataList(List<MetaDataModel> metaDataList);

}
