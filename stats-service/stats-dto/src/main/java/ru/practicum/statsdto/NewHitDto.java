package ru.practicum.statsdto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewHitDto {

    // !! Пока установлен предел 64 символа. Может быть уточнен при дальнейшей разработке
    @NotNull @NotBlank
    @Length(min = 1, max = 64)
    private String app;

    // !! Пока установлен предел 64 символа. Может быть уточнен при дальнейшей разработке
    @NotNull @NotBlank
    @Length(min = 1, max = 64)
    private String uri;

    @NotNull @NotBlank
    @Pattern(regexp = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$")
    private String ip;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
