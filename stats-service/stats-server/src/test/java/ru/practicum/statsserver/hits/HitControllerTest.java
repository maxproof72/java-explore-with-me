package ru.practicum.statsserver.hits;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.statsdto.NewHitDto;
import ru.practicum.statsdto.StatsItem;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class HitControllerTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class StatsItemDto implements StatsItem {
        private String app;
        private String uri;
        private long hits;
    }

    @Mock
    private HitService hitService;
    @InjectMocks
    private HitController hitController;
    private final ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
    private MockMvc mvc;


    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(hitController).build();
    }

    @Test
    void testAddHit() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        mvc.perform(post("/hit")
                .content(mapper.writeValueAsString(
                        new NewHitDto("app1", "uri1", "192.168.77.71", now)))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void testAddHitWithEmptyApp() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        mvc.perform(post("/hit")
                .content(mapper.writeValueAsString(
                        new NewHitDto("", "uri1", "192.168.77.71", now)))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testAddHitTooMuchApp() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        mvc.perform(post("/hit")
                .content(mapper.writeValueAsString(
                        new NewHitDto("a".repeat(65), "uri1", "192.168.77.71", now)))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testAddHitWithEmptyUri() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        mvc.perform(post("/hit")
                .content(mapper.writeValueAsString(
                        new NewHitDto("app1", "", "192.168.77.71", now)))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testAddHitWithTooMuchUri() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        mvc.perform(post("/hit")
                .content(mapper.writeValueAsString(
                        new NewHitDto("app1", "u".repeat(65), "192.168.77.71", now)))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testAddHitWithNullIp() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        mvc.perform(post("/hit")
                .content(mapper.writeValueAsString(
                        new NewHitDto("app1", "uri1", null, now)))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testAddHitWithBadIp() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        mvc.perform(post("/hit")
                .content(mapper.writeValueAsString(
                        new NewHitDto("app1", "uri1", "192.1685.77.71", now)))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testGetStatsWithoutAnyParameters() throws Exception {

        mvc.perform(get("/stats")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testGetStatsWithoutRequiredParameter() throws Exception {

        mvc.perform(get("/stats?start=2025-10-11 12:13:14")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testGetStats() throws Exception {

        var dto = new StatsItemDto("app1", "uri1", 5);
        Mockito
                .when(hitService.getStats(Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(),
                        Mockito.anyBoolean()))
                .thenReturn(List.of(dto));

        mvc.perform(get("/stats?start=2025-10-11 12:13:14&end=2030-10-11 12:13:14")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].app", is(dto.getApp())))
                .andExpect(jsonPath("$.[0].uri", is(dto.getUri())))
                .andExpect(jsonPath("$.[0].hits", is(dto.getHits()), Long.class));
    }
}
