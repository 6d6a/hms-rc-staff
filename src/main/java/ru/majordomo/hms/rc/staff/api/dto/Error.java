package ru.majordomo.hms.rc.staff.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class Error {
    private final String property;
    private final List<String> errors;
}
