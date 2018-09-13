package com.hcl.ott.ingestion.controller.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

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
        MetaDataModel model = new MetaDataModel();
        if (metaDataDTO.getId() == null)
        {
            model.setFileKey(metaDataDTO.getFileKey());
            model.setIngestionFileLocation(metaDataDTO.getIngestionFileLocation());
            model.setIngestionURL(metaDataDTO.getIngestionURL());
            model.setFileSize(metaDataDTO.getFileSize());
            model.setFileContentType(metaDataDTO.getFileContentType());
            model.setFileStatus(metaDataDTO.getFileStatus());
            return model;
        }
        model.setId(metaDataDTO.getId());
        model.setTitle(metaDataDTO.getTitle());
        model.setTags(metaDataDTO.getTags());
        model.setDescription(metaDataDTO.getDescription());
        model.setFileContentType(metaDataDTO.getFileContentType());
        model.setFileKey(metaDataDTO.getFileKey());
        model.setFileSize(metaDataDTO.getFileSize());
        model.setIngestionFileLocation(metaDataDTO.getIngestionFileLocation());
        model.setIngestionURL(metaDataDTO.getIngestionURL());
        model.setProcessFileLocation(metaDataDTO.getIngestionFileLocation());
        model.setProcessURL(metaDataDTO.getProcessURL());
        model.setJobId(metaDataDTO.getJobId());
        model.setPublishFileLocation(metaDataDTO.getPublishFileLocation());
        model.setPublishURL(metaDataDTO.getPublishURL());
        model.setTags(metaDataDTO.getTags());
        model.setFileStatus(metaDataDTO.getFileStatus());
        return model;
    }


    /**
     * Return Data Trasfer Object DTO
     * 
     * @param metaDataDBO
     * @return MetaDataDTO 
     */
    public static MetaDataDTO makeMetaDataDTO(MetaDataModel metaDataDBO)
    {
        MetaDataDTO metaDataDTO = new MetaDataDTO();
        metaDataDTO.setId(metaDataDBO.getId());
        metaDataDTO.setDescription(metaDataDBO.getDescription());
        metaDataDTO.setFileContentType(metaDataDBO.getFileContentType());
        metaDataDTO.setFileKey(metaDataDBO.getFileKey());
        metaDataDTO.setFileSize(metaDataDBO.getFileSize());
        metaDataDTO.setIngestionFileLocation(metaDataDBO.getIngestionFileLocation());
        metaDataDTO.setIngestionURL(metaDataDBO.getIngestionURL());
        metaDataDTO.setProcessFileLocation(metaDataDBO.getProcessFileLocation());
        metaDataDTO.setProcessURL(metaDataDBO.getProcessURL());
        metaDataDTO.setJobId(metaDataDBO.getJobId());
        metaDataDTO.setPublishFileLocation(metaDataDBO.getPublishFileLocation());
        metaDataDTO.setPublishURL(metaDataDBO.getPublishURL());
        metaDataDTO.setTags(metaDataDBO.getTags());
        metaDataDTO.setTitle(metaDataDBO.getTitle());
        metaDataDTO.setProcessFormat(metaDataDBO.getProcessFormat());
        metaDataDTO.setFileStatus(metaDataDBO.getFileStatus());
        return metaDataDTO;
    }


    public static List<MetaDataDTO> makeMetaDataDTOList(Page<MetaDataModel> metaDataList)
    {
        // TODO Auto-generated method stub
        return metaDataList
            .stream()
            .map(
                m -> makeMetaDataDTO(m))
            .collect(Collectors.toList());
    }

}
