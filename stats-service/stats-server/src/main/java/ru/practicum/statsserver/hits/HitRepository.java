package ru.practicum.statsserver.hits;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.statsdto.StatsItem;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {

    @Query("""
            select h.app as app, h.uri as uri, count(h.id) as hits
            from hits as h
            where h.timestamp between :start and :end
            group by h.app, h.uri
            order by hits desc
            """)
    List<StatsItem> findAllNonUniqueHits(LocalDateTime start, LocalDateTime end);

    @Query("""
            select h.app as app, h.uri as uri, count(h.id) as hits
            from hits as h
            where (h.timestamp between :start and :end) and h.uri in :uris
            group by h.app, h.uri
            order by hits desc
            """)
    List<StatsItem> findAllNonUniqueHitsInUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("""
            select h.app as app, h.uri as uri, count(distinct h.ip) as hits
            from hits as h
            where h.timestamp between :start and :end
            group by h.app, h.uri
            order by hits desc
            """)
    List<StatsItem> findAllUniqueHits(LocalDateTime start, LocalDateTime end);

    @Query("""
            select h.app as app, h.uri as uri, count(distinct h.ip) as hits
            from hits as h
            where (h.timestamp between :start and :end) and h.uri in :uris
            group by h.app, h.uri
            order by hits desc
            """)
    List<StatsItem> findAllUniqueHitsInUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}
