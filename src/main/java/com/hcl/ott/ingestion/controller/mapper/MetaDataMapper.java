package com.hcl.ott.ingestion.controller.mapper;

import com.hcl.ott.ingestion.data.MetaDataDTO;
import com.hcl.ott.ingestion.model.MetaDataModel;

/**
 * Mapper for Database Model object .
 * 
 * @author kandalakar.r
 *
 */
public class MetaDataMapper
{
    /**
     * Return Database Model object DBO
     * 
     * @param metaDataDTO
     * @return MetaDataModel
     */
    public static MetaDataModel makeMetaDataDBO(MetaDataDTO metaDataDTO)
    {
        return new MetaDataModel(metaDataDTO.getTitle(), metaDataDTO.getDescription(), metaDataDTO.getTags(), metaDataDTO.getLocation());
    }


    /**
     * Return Data Trasfer Object DTO
     * 
     * @param metaDataDBO
     * @return MetaDataDTO 
     */
    public static MetaDataDTO makeMetaDataDTO(MetaDataModel metaDataDBO)
    {
        return new MetaDataDTO(metaDataDBO.getTitle(), metaDataDBO.getDescription(), metaDataDBO.getTags(), metaDataDBO.getLocation());
    }

}
