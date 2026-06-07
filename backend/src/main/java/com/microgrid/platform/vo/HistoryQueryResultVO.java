package com.microgrid.platform.vo;

import com.microgrid.platform.common.PageResult;
import lombok.Data;

import java.util.List;

@Data
public class HistoryQueryResultVO {

    private PageResult<HistoryDataVO> pageData;
    private List<HistoryDataVO> chartData;
}
