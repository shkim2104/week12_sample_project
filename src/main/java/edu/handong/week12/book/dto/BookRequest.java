package edu.handong.week12.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 150, message = "제목은 150자 이하여야 합니다.")
        String title,

        @NotBlank(message = "저자는 필수입니다.")
        @Size(max = 100, message = "저자는 100자 이하여야 합니다.")
        String author,

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        Integer price
) {
}
