package com.viaplaygroup.hackday.mapper;

import com.viaplaygroup.hackday.dto.TranscodeDto;
import com.viaplaygroup.hackday.dto.TransferDto;
import com.viaplaygroup.hackday.entity.TranscodeEntity;
import com.viaplaygroup.hackday.entity.TransferEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransferMapper {

    TransferMapper INSTANCE = Mappers.getMapper( TransferMapper.class );

    TransferDto map(TransferEntity transferEntity);

    @Mapping(source = "mediaId", target = "sourceMedia.id")
    TransferEntity map(TransferDto transferDto);

}
