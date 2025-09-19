package ru.practicum.statsserver.hits;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.statsdto.NewHitDto;
import ru.practicum.statsdto.StatsItem;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;

    @Override
    public void addHit(NewHitDto dto) {

        Hit hit = HitMapper.toEntity(dto);
        hitRepository.save(hit);
        log.debug("Added a new hit with id {}", hit.getId());
    }

    @Override
    public List<StatsItem> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        if (start.isAfter(end))
            throw new IllegalArgumentException("Start date is after end date");

        List<StatsItem> viewStats;

        if (uris == null || uris.isEmpty()) {
            viewStats = unique ?
                    hitRepository.findAllUniqueHits(start, end) :
                    hitRepository.findAllNonUniqueHits(start, end);
        } else {
            viewStats = unique ?
                    hitRepository.findAllUniqueHitsInUris(start, end, uris) :
                    hitRepository.findAllNonUniqueHitsInUris(start, end, uris);
        }

        log.debug("Found {} view stats", viewStats.size());
        return viewStats;
    }
}
