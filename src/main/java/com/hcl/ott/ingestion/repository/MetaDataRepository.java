package com.hcl.ott.ingestion.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hcl.ott.ingestion.model.MetaDataModel;

/**
 * Interface for database operations
 * 
 * @author kandalakar.r
 *
 */
public interface MetaDataRepository extends MongoRepository<MetaDataModel,Object>
{
    MetaDataModel findByJobId(String jobId);
}
