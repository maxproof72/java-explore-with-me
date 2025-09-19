package ru.practicum.statsserver.hits;

import ru.practicum.statsdto.NewHitDto;
import ru.practicum.statsdto.StatsItem;

import java.time.LocalDateTime;
import java.util.List;

public interface HitService {

    void addHit(NewHitDto dto);

    List<StatsItem> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
