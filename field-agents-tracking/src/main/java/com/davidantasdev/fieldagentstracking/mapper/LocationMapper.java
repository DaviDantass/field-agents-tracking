package com.davidantasdev.fieldagentstracking.mapper;

import com.davidantasdev.fieldagentstracking.dto.CheckInResponse;
import com.davidantasdev.fieldagentstracking.dto.LocationDTO;
import com.davidantasdev.fieldagentstracking.dto.SyncHistoryResponse;
import com.davidantasdev.fieldagentstracking.entity.CheckIn;
import com.davidantasdev.fieldagentstracking.entity.Location;
import com.davidantasdev.fieldagentstracking.entity.SyncHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location toEntity(LocationDTO dto);

    @Mapping(source = "agent.id", target = "agentId")
    CheckInResponse toCheckInResponse(CheckIn checkIn);

    SyncHistoryResponse toSyncHistoryResponse(SyncHistory syncHistory);
}