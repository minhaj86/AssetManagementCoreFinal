package com.viaplaygroup.hackday.mapper;

import com.viaplaygroup.hackday.dto.TranscodeDto;
import com.viaplaygroup.hackday.entity.TranscodeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TranscodeMapper {

    TranscodeMapper INSTANCE = Mappers.getMapper( TranscodeMapper.class );

    TranscodeDto map(TranscodeEntity transcodeEntity);

    @Mapping(source = "mediaId", target = "sourceMedia.id")
    TranscodeEntity map(TranscodeDto transcodeDto);

}
