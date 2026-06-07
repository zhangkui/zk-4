package com.microgrid.platform.service;

import com.microgrid.platform.dto.HistoryQueryDTO;
import com.microgrid.platform.vo.HistoryDataVO;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.vo.HistoryQueryResultVO;

import java.util.List;

public interface HistoryService {

    PageResult<HistoryDataVO> queryPage(HistoryQueryDTO query);

    List<HistoryDataVO> queryList(HistoryQueryDTO query);

    HistoryQueryResultVO query(HistoryQueryDTO query);
}
