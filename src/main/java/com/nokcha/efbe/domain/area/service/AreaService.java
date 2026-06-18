package com.nokcha.efbe.domain.area.service;

import com.nokcha.efbe.domain.area.dto.response.AreaOptionRspDto;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;

    @Transactional(readOnly = true)
    public List<AreaOptionRspDto> getAreaOptions() {
        return areaRepository.findAll(Sort.by(Sort.Order.asc("id"))).stream()
                .map(AreaOptionRspDto::from)
                .toList();
    }
}
