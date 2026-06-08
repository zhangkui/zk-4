package com.microgrid.platform.controller;

import com.microgrid.platform.annotation.OperationLog;
import com.microgrid.platform.annotation.RequirePermission;
import com.microgrid.platform.common.AlertConstants;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.Result;
import com.microgrid.platform.dto.AlertDTO;
import com.microgrid.platform.dto.AlertQueryDTO;
import com.microgrid.platform.dto.AlertRuleCreateDTO;
import com.microgrid.platform.dto.AlertRuleDTO;
import com.microgrid.platform.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/rules/constants")
    @RequirePermission("alert-rule")
    public Result<Map<String, Object>> getRuleConstants() {
        Map<String, Object> constants = new HashMap<>();
        constants.put("ruleTypes", AlertConstants.RULE_TYPE_MAP);
        constants.put("alertLevels", AlertConstants.ALERT_LEVEL_MAP);
        constants.put("scopeTypes", AlertConstants.SCOPE_TYPE_MAP);
        constants.put("operators", AlertConstants.OPERATOR_MAP);
        constants.put("alertStatuses", AlertConstants.ALERT_STATUS_MAP);
        return Result.success(constants);
    }

    @GetMapping("/rules/{id}")
    @RequirePermission("alert-rule")
    public Result<AlertRuleDTO> getRuleById(@PathVariable Long id) {
        return Result.success(alertService.getRuleById(id));
    }

    @GetMapping("/rules/code/{ruleCode}")
    @RequirePermission("alert-rule")
    public Result<AlertRuleDTO> getRuleByCode(@PathVariable String ruleCode) {
        return Result.success(alertService.getRuleByCode(ruleCode));
    }

    @GetMapping("/rules/page")
    @RequirePermission("alert-rule")
    public Result<PageResult<AlertRuleDTO>> pageRules(@RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "20") Integer pageSize,
                                                     @RequestParam(required = false) String ruleType,
                                                     @RequestParam(required = false) String alertLevel,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(required = false) String keyword) {
        return Result.success(alertService.pageRules(pageNum, pageSize, ruleType, alertLevel, status, keyword));
    }

    @GetMapping("/rules/list")
    @RequirePermission("alert-rule")
    public Result<List<AlertRuleDTO>> listRules(@RequestParam(required = false) String ruleType,
                                                @RequestParam(required = false) Integer status) {
        return Result.success(alertService.listRules(ruleType, status));
    }

    @PostMapping("/rules")
    @RequirePermission("alert-rule")
    @OperationLog(operationType = "CREATE", operationModule = "告警规则", description = "创建告警规则")
    public Result<AlertRuleDTO> createRule(@Valid @RequestBody AlertRuleCreateDTO dto) {
        return Result.success(alertService.createRule(dto));
    }

    @PutMapping("/rules/{id}")
    @RequirePermission("alert-rule")
    @OperationLog(operationType = "UPDATE", operationModule = "告警规则", description = "更新告警规则")
    public Result<AlertRuleDTO> updateRule(@PathVariable Long id, @RequestBody AlertRuleCreateDTO dto) {
        return Result.success(alertService.updateRule(id, dto));
    }

    @DeleteMapping("/rules/{id}")
    @RequirePermission("alert-rule")
    @OperationLog(operationType = "DELETE", operationModule = "告警规则", description = "删除告警规则")
    public Result<Void> deleteRule(@PathVariable Long id) {
        alertService.deleteRule(id);
        return Result.success();
    }

    @PutMapping("/rules/{id}/status")
    @RequirePermission("alert-rule")
    @OperationLog(operationType = "UPDATE", operationModule = "告警规则", description = "更新告警规则状态")
    public Result<Void> updateRuleStatus(@PathVariable Long id, @RequestParam Integer status) {
        alertService.updateRuleStatus(id, status);
        return Result.success();
    }

    @GetMapping("/{id}")
    @RequirePermission("alert-center")
    public Result<AlertDTO> getAlertById(@PathVariable Long id) {
        return Result.success(alertService.getAlertById(id));
    }

    @GetMapping("/page")
    @RequirePermission("alert-center")
    public Result<PageResult<AlertDTO>> pageAlerts(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "20") Integer pageSize,
                                                   AlertQueryDTO query) {
        return Result.success(alertService.pageAlerts(pageNum, pageSize, query));
    }

    @PostMapping("/process")
    @RequirePermission("alert-rule")
    public Result<Void> processAlertRules() {
        alertService.processAlertRules();
        return Result.success();
    }
}
