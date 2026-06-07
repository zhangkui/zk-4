package com.microgrid.platform.service.impl;

import com.microgrid.platform.common.BusinessException;
import com.microgrid.platform.common.Constants;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.ResultCode;
import com.microgrid.platform.dto.ParkCreateDTO;
import com.microgrid.platform.dto.ParkDTO;
import com.microgrid.platform.entity.Park;
import com.microgrid.platform.repository.DeviceRepository;
import com.microgrid.platform.repository.ParkRepository;
import com.microgrid.platform.service.ParkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkServiceImpl implements ParkService {

    private final ParkRepository parkRepository;
    private final DeviceRepository deviceRepository;

    @Override
    public ParkDTO getById(Long id) {
        Park park = parkRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.PARK_NOT_FOUND));
        return convertToDTO(park);
    }

    @Override
    public ParkDTO getByCode(String parkCode) {
        Park park = parkRepository.findByParkCode(parkCode)
                .orElseThrow(() -> new BusinessException(ResultCode.PARK_NOT_FOUND));
        return convertToDTO(park);
    }

    @Override
    public PageResult<ParkDTO> page(Integer pageNum, Integer pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Park> page;

        if (StringUtils.hasText(keyword)) {
            List<Park> list = parkRepository.searchByKeyword(keyword);
            long total = list.size();
            List<Park> pageList = list.stream()
                    .skip((long) (pageNum - 1) * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.toList());
            page = new org.springframework.data.domain.PageImpl<>(pageList, pageable, total);
        } else {
            page = parkRepository.findAll(pageable);
        }

        List<ParkDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), pageNum, pageSize, dtoList);
    }

    @Override
    public List<ParkDTO> list() {
        return parkRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParkDTO create(ParkCreateDTO dto) {
        if (parkRepository.existsByParkCode(dto.getParkCode())) {
            throw new BusinessException(ResultCode.PARK_CODE_EXISTS);
        }

        Park park = new Park();
        park.setParkCode(dto.getParkCode());
        park.setParkName(dto.getParkName());
        park.setLocation(dto.getLocation());
        park.setLongitude(dto.getLongitude());
        park.setLatitude(dto.getLatitude());
        park.setDescription(dto.getDescription());
        park.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        park = parkRepository.save(park);

        log.info("创建园区成功: {}", park.getParkCode());
        return convertToDTO(park);
    }

    @Override
    @Transactional
    public ParkDTO update(Long id, ParkCreateDTO dto) {
        Park park = parkRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.PARK_NOT_FOUND));

        if (dto.getParkCode() != null) park.setParkCode(dto.getParkCode());
        if (dto.getParkName() != null) park.setParkName(dto.getParkName());
        if (dto.getLocation() != null) park.setLocation(dto.getLocation());
        if (dto.getLongitude() != null) park.setLongitude(dto.getLongitude());
        if (dto.getLatitude() != null) park.setLatitude(dto.getLatitude());
        if (dto.getDescription() != null) park.setDescription(dto.getDescription());
        if (dto.getStatus() != null) park.setStatus(dto.getStatus());

        park = parkRepository.save(park);
        log.info("更新园区成功: {}", park.getParkCode());
        return convertToDTO(park);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!parkRepository.existsById(id)) {
            throw new BusinessException(ResultCode.PARK_NOT_FOUND);
        }
        parkRepository.deleteById(id);
        log.info("删除园区成功: id={}", id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        parkRepository.updateStatus(id, status);
        log.info("更新园区状态: id={}, status={}", id, status);
    }

    private ParkDTO convertToDTO(Park park) {
        ParkDTO dto = new ParkDTO();
        dto.setId(park.getId());
        dto.setParkCode(park.getParkCode());
        dto.setParkName(park.getParkName());
        dto.setLocation(park.getLocation());
        dto.setLongitude(park.getLongitude());
        dto.setLatitude(park.getLatitude());
        dto.setDescription(park.getDescription());
        dto.setStatus(park.getStatus());
        dto.setCreatedAt(park.getCreatedAt());
        dto.setUpdatedAt(park.getUpdatedAt());

        Long deviceCount = deviceRepository.countByParkId(park.getId());
        dto.setDeviceCount(deviceCount != null ? deviceCount.intValue() : 0);

        LocalDateTime threshold = LocalDateTime.now().minusSeconds(Constants.DEVICE_TIMEOUT_SECONDS);
        Long onlineCount = deviceRepository.countOnlineByParkId(park.getId(), threshold);
        dto.setOnlineDeviceCount(onlineCount != null ? onlineCount.intValue() : 0);

        return dto;
    }
}
