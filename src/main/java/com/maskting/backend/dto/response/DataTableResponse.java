package com.maskting.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataTableResponse {

    private int draw;
    private int recordsTotal;
    private int recordsFiltered;
    private List<ReviewResponse> data;
}
