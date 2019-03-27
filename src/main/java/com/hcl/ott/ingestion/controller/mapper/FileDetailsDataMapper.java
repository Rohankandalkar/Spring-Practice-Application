package com.hcl.ott.ingestion.controller.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.hcl.ott.ingestion.data.MetaDataDTO;
import com.hcl.ott.ingestion.data.FileDetailsData;

public class FileDetailsDataMapper
{

    public static FileDetailsData makeStorageDetailsData(MetaDataDTO metaDataDTO)
    {
        FileDetailsData fileDetailsData = new FileDetailsData();
        fileDetailsData.setTitle(metaDataDTO.getTitle());
        fileDetailsData.setDescription(metaDataDTO.getFileChecksum());
        fileDetailsData.setFileChecksum(metaDataDTO.getFileChecksum());
        fileDetailsData.setTags(metaDataDTO.getTags());

        return fileDetailsData;
    }


    public static MetaDataDTO makeMetaDataDTO(FileDetailsData fileUploadResponce)
    {

        MetaDataDTO metaDataDTO = new MetaDataDTO();
        metaDataDTO.setFileContentType(fileUploadResponce.getFileContentType());
        metaDataDTO.setFileKey(fileUploadResponce.getFileKey());
        metaDataDTO.setFileSize(fileUploadResponce.getFileSize());
        metaDataDTO.setFileStatus(fileUploadResponce.getFileStatus());
        metaDataDTO.setFileChecksum(fileUploadResponce.getFileChecksum());
        metaDataDTO.setIngestionFileLocation(fileUploadResponce.getIngestionFileLocation());
        metaDataDTO.setIngestionURL(fileUploadResponce.getIngestionURL());
        return metaDataDTO;

    }


    public static List<MetaDataDTO> makeMetaDataDTOList(List<FileDetailsData> amazonUploadResponceList)
    {
        return amazonUploadResponceList.stream().map(uploadedFileMetaData -> makeMetaDataDTO(uploadedFileMetaData)).collect(Collectors.toList());

    }
}
