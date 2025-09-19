package ru.practicum.statsserver.hits;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.statsdto.NewHitDto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Month;

@JsonTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HitDtoTest {

    private final JacksonTester<NewHitDto> jsonHit;

    @Test
    void testHitDtoJson() throws Exception {

        var ldt = LocalDateTime.of(2010, Month.JANUARY, 10, 15, 26, 27);
        var dto = new NewHitDto("app1", "uri1", "192.168.0.4", ldt);
        var jsonContent = jsonHit.write(dto);
        assertThat(jsonContent).extractingJsonPathStringValue("$.app").isEqualTo(dto.getApp());
        assertThat(jsonContent).extractingJsonPathStringValue("$.uri").isEqualTo(dto.getUri());
        assertThat(jsonContent).extractingJsonPathStringValue("$.ip").isEqualTo(dto.getIp());
        assertThat(jsonContent).extractingJsonPathStringValue("$.timestamp").isEqualTo("2010-01-10 15:26:27");
    }
}
