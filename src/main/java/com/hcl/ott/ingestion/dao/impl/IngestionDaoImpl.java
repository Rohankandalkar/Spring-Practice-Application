package com.hcl.ott.ingestion.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hcl.ott.ingestion.dao.IngestionDao;
import com.hcl.ott.ingestion.model.MetaDataModel;
import com.hcl.ott.ingestion.repository.MetaDataRepository;

/**
 * Repository of Injection 
 * 
 * @author kandalakar.r
 *
 */
@Repository
public class IngestionDaoImpl implements IngestionDao
{

    @Autowired
    private MetaDataRepository metaDataRepository;


    /** 
     * Saves Meta-Data of file to Database
     * 
     * @param metaData
     * @return MataDataModel 
     */
    public MetaDataModel saveMetaData(MetaDataModel metaData)
    {
        return this.metaDataRepository.save(metaData);

    }


    /** 
     * Returns list of all stored metadata model objects
     * 
     * @return List<MetaDataModel>
     */
    @Override
    public List<MetaDataModel> findAll()
    {
        List<MetaDataModel> dataModelList = new ArrayList<MetaDataModel>();
        this.metaDataRepository.findAll().forEach(m -> dataModelList.add(new MetaDataModel(m.getTitle(), m.getLocation(), m.getTags(), m.getDescription())));
        return dataModelList;
    }
}
