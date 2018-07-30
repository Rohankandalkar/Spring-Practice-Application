package com.hcl.ott.ingestion.repository;

import org.springframework.data.repository.CrudRepository;

import com.hcl.ott.ingestion.model.MetaDataModel;

/**
 * Interface for database operations
 * 
 * @author kandalakar.r
 *
 */
public interface MetaDataRepository extends CrudRepository<MetaDataModel,Long>
{

}
