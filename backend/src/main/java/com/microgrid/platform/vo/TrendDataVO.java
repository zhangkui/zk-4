package com.microgrid.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class TrendDataVO {

    private List<TrendPointVO> generation;
    private List<TrendPointVO> load;
    private List<TrendPointVO> ess;
    private List<TrendPointVO> pv;
    private List<TrendPointVO> wind;
}
