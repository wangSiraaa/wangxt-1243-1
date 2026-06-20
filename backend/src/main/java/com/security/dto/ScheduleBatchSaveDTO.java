package com.security.dto;

import com.security.entity.ShiftSchedule;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleBatchSaveDTO {

    private List<ShiftSchedule> schedules;
}
