package com.hcl.ott.ingestion.dao.impl;

import java.util.List;
import java.util.NoSuchElementException;

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
        return this.metaDataRepository.findAll();
    }


    /** 
     * Returns singal MetaData Object from database
     * 
     * @return MetaDataModel
     */
    @Override
    public MetaDataModel getMetaDataById(Long id) throws NoSuchElementException
    {
        return this.metaDataRepository.findById(id).get();
    }

}
