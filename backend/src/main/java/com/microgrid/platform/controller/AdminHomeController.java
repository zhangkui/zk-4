package com.microgrid.platform.controller;

import com.microgrid.platform.annotation.RequirePermission;
import com.microgrid.platform.common.Result;
import com.microgrid.platform.service.AdminHomeService;
import com.microgrid.platform.vo.AdminHomeStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin-home")
@RequiredArgsConstructor
public class AdminHomeController {

    private final AdminHomeService adminHomeService;

    @GetMapping("/stats")
    @RequirePermission("admin-home")
    public Result<AdminHomeStatsVO> getStats() {
        return Result.success(adminHomeService.getStats());
    }
}
