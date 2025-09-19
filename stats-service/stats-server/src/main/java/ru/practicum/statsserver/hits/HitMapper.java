package ru.practicum.statsserver.hits;

import lombok.experimental.UtilityClass;
import ru.practicum.statsdto.NewHitDto;

@UtilityClass
public class HitMapper {

    public Hit toEntity(NewHitDto dto) {
        return new Hit(null, dto.getApp(), dto.getUri(), dto.getIp(), dto.getTimestamp());
    }
}
