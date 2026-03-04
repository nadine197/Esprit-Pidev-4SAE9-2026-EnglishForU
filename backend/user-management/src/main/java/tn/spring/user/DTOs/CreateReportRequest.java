package tn.spring.user.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tn.spring.user.Enums.ReportCategory;
import tn.spring.user.Enums.ReportSeverity;

@Data
public class CreateReportRequest {

    @NotBlank
    @Size(max = 180)
    private String title;

    @NotNull
    private ReportCategory category;

    @NotNull
    private ReportSeverity severity;

    @NotBlank
    private String description;

    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;

    @Size(max = 1000)
    private String pageUrl;

    private String userAgent;

    @Size(max = 120)
    private String appVersion;
}
