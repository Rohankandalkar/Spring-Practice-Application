package com.hcl.ott.ingestion.dao.impl;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<MetaDataModel> findAll(Pageable pageable)
    {
        return this.metaDataRepository.findAll(pageable);

    }


    /** 
     * Returns singal MetaData Object from database
     * 
     * @return MetaDataModel
     */
    @Override
    public MetaDataModel getMetaDataById(String id) throws NoSuchElementException
    {
        MetaDataModel metaDataModel = this.metaDataRepository.findById(id).get();

        return metaDataModel;
    }


    /** 
     * Returns singal MetaData Object from database when update request came from aws lambda function
     * 
     * @param JobId - JobId created by Media converter AWS service
     * @return MetaDataModel
     */
    @Override
    public MetaDataModel getMetaDataByJobId(String jobId) throws NoSuchElementException
    {
        MetaDataModel metaDataModel = this.metaDataRepository.findByJobId(jobId);

        return metaDataModel;
    }

    /** 
     * Returns List of MetaData Object from database 
     * 
     * @param List<MetaDataModel> - List of MetaData Object
     * @return List<MetaDataModel> - List of MetaData saved Objects 
     */
    @Override
    public List<MetaDataModel> saveMetaDataList(List<MetaDataModel> metaDataList)
    {
        return this.metaDataRepository.saveAll(metaDataList);
    }

}
