package com.viaplaygroup.hackday.mapper;

import com.viaplaygroup.hackday.dto.MediaDto;
import com.viaplaygroup.hackday.entity.MediaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MediaMapper {

    MediaMapper INSTANCE = Mappers.getMapper( MediaMapper.class );

    @Mapping(source = "asset.title", target = "assetTitle")
    @Mapping(source = "asset.id", target = "assetId")
    MediaDto map(MediaEntity mediaEntity);

    MediaEntity map(MediaDto mediaDto);

}
