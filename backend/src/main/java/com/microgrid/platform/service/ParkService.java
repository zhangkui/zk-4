package com.microgrid.platform.service;

import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.dto.ParkCreateDTO;
import com.microgrid.platform.dto.ParkDTO;

import java.util.List;

public interface ParkService {

    ParkDTO getById(Long id);

    ParkDTO getByCode(String parkCode);

    PageResult<ParkDTO> page(Integer pageNum, Integer pageSize, String keyword);

    List<ParkDTO> list();

    ParkDTO create(ParkCreateDTO dto);

    ParkDTO update(Long id, ParkCreateDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);
}
