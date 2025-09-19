package ru.practicum.statsserver.hits;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statsdto.NewHitDto;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


@Transactional
@SpringBootTest(properties = {"spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:stats"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HitServiceImplTest {

    private final EntityManager em;
    private final HitService hitService;
    private final HitRepository hitRepository;

    private void makeHits() {
        hitRepository.save(new Hit(null, "app1", "uri1", "169.124.159.122", LocalDateTime.of(2018, Month.JULY, 15, 16, 40,10)));
        hitRepository.save(new Hit(null, "app2", "uri2", "169.124.159.122", LocalDateTime.of(2019, Month.JULY, 15, 16, 40,10)));
        hitRepository.save(new Hit(null, "app1", "uri1", "169.124.159.199", LocalDateTime.of(2020, Month.JULY, 15, 16, 40,10)));
        hitRepository.save(new Hit(null, "app3", "uri3", "169.124.159.199", LocalDateTime.of(2021, Month.JULY, 15, 16, 40,10)));
        hitRepository.save(new Hit(null, "app3", "uri3", "169.124.159.122", LocalDateTime.of(2022, Month.JULY, 15, 16, 40,10)));
        hitRepository.save(new Hit(null, "app3", "uri3", "169.124.159.122", LocalDateTime.of(2023, Month.JULY, 15, 16, 40,10)));
        hitRepository.save(new Hit(null, "app1", "uri1", "169.124.159.199", LocalDateTime.of(2024, Month.JULY, 15, 16, 40,10)));
        hitRepository.save(new Hit(null, "app1", "uri1", "169.124.159.201", LocalDateTime.of(2025, Month.JULY, 15, 16, 40,10)));
    }

    @Test
    void testAddHits() {

        LocalDateTime now = LocalDateTime.now();
        NewHitDto dto = new NewHitDto("app1", "event/1", "192.168.77.71", now);
        hitService.addHit(dto);

        TypedQuery<Hit> query = em.createQuery("select h from hits h where h.app = :app", Hit.class);
        query.setParameter("app", dto.getApp());
        Hit hit = query.getSingleResult();

        assertThat(hit, notNullValue());
        assertThat(hit.getApp(), equalTo(dto.getApp()));
        assertThat(hit.getUri(), equalTo(dto.getUri()));
        assertThat(hit.getIp(), equalTo(dto.getIp()));
        assertThat(hit.getTimestamp(), equalTo(dto.getTimestamp()));
    }

    @Test
    void testBadTimeParams() {

        LocalDateTime start = LocalDateTime.of(2010, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2030, Month.DECEMBER, 31, 23, 59, 59);
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> hitService.getStats(end, start, null, false));
    }

    @Test
    void testGetAllNonUniqueHits() {

        makeHits();

        LocalDateTime start = LocalDateTime.of(2010, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2030, Month.DECEMBER, 31, 23, 59, 59);
        var stats = hitService.getStats(start, end, null, false);
        assertThat(stats, notNullValue());
        assertThat(stats.size(), equalTo(3));
        assertThat(stats.get(0).getHits(), equalTo(4L));
        assertThat(stats.get(1).getHits(), equalTo(3L));
        assertThat(stats.get(2).getHits(), equalTo(1L));
        assertThat(stats.get(0).getApp(), equalTo("app1"));
        assertThat(stats.get(1).getApp(), equalTo("app3"));
        assertThat(stats.get(2).getApp(), equalTo("app2"));
    }

    @Test
    void testGetAllUniqueHits() {

        makeHits();

        LocalDateTime start = LocalDateTime.of(2010, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2030, Month.DECEMBER, 31, 23, 59, 59);
        var stats = hitService.getStats(start, end, null, true);
        assertThat(stats, notNullValue());
        assertThat(stats.size(), equalTo(3));
        assertThat(stats.get(0).getHits(), equalTo(3L));
        assertThat(stats.get(1).getHits(), equalTo(2L));
        assertThat(stats.get(2).getHits(), equalTo(1L));
        assertThat(stats.get(0).getApp(), equalTo("app1"));
        assertThat(stats.get(1).getApp(), equalTo("app3"));
        assertThat(stats.get(2).getApp(), equalTo("app2"));
    }

    @Test
    void testGetTimeRangedNonUniqueHits() {

        makeHits();

        LocalDateTime start = LocalDateTime.of(2019, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, Month.DECEMBER, 31, 23, 59, 59);
        var stats = hitService.getStats(start, end, null, false);
        assertThat(stats, notNullValue());
        assertThat(stats.size(), equalTo(3));
        assertThat(stats.get(0).getHits(), equalTo(3L));
        assertThat(stats.get(1).getHits(), equalTo(2L));
        assertThat(stats.get(2).getHits(), equalTo(1L));
        assertThat(stats.get(0).getApp(), equalTo("app3"));
        assertThat(stats.get(1).getApp(), equalTo("app1"));
        assertThat(stats.get(2).getApp(), equalTo("app2"));
    }

    @Test
    void testGetTimeRangedUniqueHits() {

        makeHits();

        LocalDateTime start = LocalDateTime.of(2019, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, Month.DECEMBER, 31, 23, 59, 59);
        var stats = hitService.getStats(start, end, null, true);
        assertThat(stats, notNullValue());
        assertThat(stats.size(), equalTo(3));
        assertThat(stats.get(0).getHits(), equalTo(2L));
        assertThat(stats.get(1).getHits(), equalTo(1L));
        assertThat(stats.get(2).getHits(), equalTo(1L));
        assertThat(stats.get(0).getApp(), equalTo("app3"));
        if (stats.get(1).getApp().equals("app1")) {
            assertThat(stats.get(2).getApp(), equalTo("app2"));
        } else {
            assertThat(stats.get(1).getApp(), equalTo("app2"));
            assertThat(stats.get(2).getApp(), equalTo("app1"));
        }
    }

    @Test
    void testGetSelectedNonUniqueHits() {

        makeHits();

        LocalDateTime start = LocalDateTime.of(2010, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2030, Month.DECEMBER, 31, 23, 59, 59);
        var stats = hitService.getStats(start, end, List.of("uri1", "uri3"), false);
        assertThat(stats, notNullValue());
        assertThat(stats.size(), equalTo(2));
        assertThat(stats.get(0).getHits(), equalTo(4L));
        assertThat(stats.get(1).getHits(), equalTo(3L));
        assertThat(stats.get(0).getApp(), equalTo("app1"));
        assertThat(stats.get(1).getApp(), equalTo("app3"));
    }

    @Test
    void testGetSelectedUniqueHits() {

        makeHits();

        LocalDateTime start = LocalDateTime.of(2010, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2030, Month.DECEMBER, 31, 23, 59, 59);
        var stats = hitService.getStats(start, end, List.of("uri1", "uri3"), true);
        assertThat(stats, notNullValue());
        assertThat(stats.size(), equalTo(2));
        assertThat(stats.get(0).getHits(), equalTo(3L));
        assertThat(stats.get(1).getHits(), equalTo(2L));
        assertThat(stats.get(0).getApp(), equalTo("app1"));
        assertThat(stats.get(1).getApp(), equalTo("app3"));
    }

    @Test
    void testGetTimeRangedSelectedNonUniqueHits() {

        makeHits();

        LocalDateTime start = LocalDateTime.of(2019, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, Month.DECEMBER, 31, 23, 59, 59);
        var stats = hitService.getStats(start, end, List.of("uri1", "uri3"), false);
        assertThat(stats, notNullValue());
        assertThat(stats.size(), equalTo(2));
        assertThat(stats.get(0).getHits(), equalTo(3L));
        assertThat(stats.get(1).getHits(), equalTo(2L));
        assertThat(stats.get(0).getApp(), equalTo("app3"));
        assertThat(stats.get(1).getApp(), equalTo("app1"));
    }

    @Test
    void testGetTimeRangedSelectedUniqueHits() {

        makeHits();

        LocalDateTime start = LocalDateTime.of(2019, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, Month.DECEMBER, 31, 23, 59, 59);
        var stats = hitService.getStats(start, end, List.of("uri1", "uri3"), true);
        assertThat(stats, notNullValue());
        assertThat(stats.size(), equalTo(2));
        assertThat(stats.get(0).getHits(), equalTo(2L));
        assertThat(stats.get(1).getHits(), equalTo(1L));
        assertThat(stats.get(0).getApp(), equalTo("app3"));
        assertThat(stats.get(1).getApp(), equalTo("app1"));
    }
}
