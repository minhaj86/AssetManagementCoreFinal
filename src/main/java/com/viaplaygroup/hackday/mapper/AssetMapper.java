package com.viaplaygroup.hackday.mapper;

import com.viaplaygroup.hackday.dto.AssetDto;
import com.viaplaygroup.hackday.entity.AssetEntity;
import com.viaplaygroup.hackday.dto.MediaDto;
import com.viaplaygroup.hackday.entity.MediaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface AssetMapper {

    AssetMapper INSTANCE = Mappers.getMapper( AssetMapper.class );

    @Mapping(source = "media", target = "media", qualifiedByName = "mapToMedia")
    AssetDto map(AssetEntity assetEntity);

    AssetEntity map(AssetDto assetDto);

    @Named("mapToMedia")
    static Set<MediaDto> mapToMedia(Set<MediaEntity> media) {
        return media.stream().map(x-> { return MediaMapper.INSTANCE.map(x); }).collect(Collectors.toSet());
    }
}
