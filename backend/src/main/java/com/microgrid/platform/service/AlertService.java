package com.microgrid.platform.service;

import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.dto.AlertDTO;
import com.microgrid.platform.dto.AlertQueryDTO;
import com.microgrid.platform.dto.AlertRuleCreateDTO;
import com.microgrid.platform.dto.AlertRuleDTO;

import java.util.List;

public interface AlertService {

    AlertRuleDTO getRuleById(Long id);

    AlertRuleDTO getRuleByCode(String ruleCode);

    PageResult<AlertRuleDTO> pageRules(Integer pageNum, Integer pageSize, String ruleType, String alertLevel, Integer status, String keyword);

    List<AlertRuleDTO> listRules(String ruleType, Integer status);

    AlertRuleDTO createRule(AlertRuleCreateDTO dto);

    AlertRuleDTO updateRule(Long id, AlertRuleCreateDTO dto);

    void deleteRule(Long id);

    void updateRuleStatus(Long id, Integer status);

    AlertDTO getAlertById(Long id);

    PageResult<AlertDTO> pageAlerts(Integer pageNum, Integer pageSize, AlertQueryDTO query);

    void processAlertRules();
}
