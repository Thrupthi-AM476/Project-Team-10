package com.vps.service;

import com.vps.dto.RateScheduleRequest;
import com.vps.dto.RateScheduleResponse;
import com.vps.entity.RateSchedule;
import com.vps.entity.Vehicle.VehicleType;
import com.vps.repository.RateScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RateScheduleService {

    private final RateScheduleRepository rateScheduleRepository;

    @Transactional(readOnly = true)
    public List<RateScheduleResponse> getAll() {
        return rateScheduleRepository.findAll().stream()
                .map(RateScheduleResponse::from)
                .toList();
    }

    @Transactional
    public RateScheduleResponse create(RateScheduleRequest request) {
        boolean active = request.getActive() == null || request.getActive();
        if (active) {
            ensureNoOtherActiveSchedule(request.getVehicleType(), null);
        }

        RateSchedule schedule = new RateSchedule();
        apply(schedule, request);
        schedule.setActive(active);
        return RateScheduleResponse.from(rateScheduleRepository.save(schedule));
    }

    @Transactional
    public RateScheduleResponse update(Long id, RateScheduleRequest request) {
        RateSchedule schedule = rateScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rate schedule not found: " + id));

        boolean active = request.getActive() == null
                ? schedule.isActive()
                : request.getActive();

        if (active) {
            ensureNoOtherActiveSchedule(request.getVehicleType(), id);
        }

        apply(schedule, request);
        schedule.setActive(active);
        return RateScheduleResponse.from(rateScheduleRepository.save(schedule));
    }

    @Transactional
    public RateScheduleResponse deactivate(Long id) {
        RateSchedule schedule = rateScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rate schedule not found: " + id));
        schedule.setActive(false);
        return RateScheduleResponse.from(rateScheduleRepository.save(schedule));
    }

    private void apply(RateSchedule schedule, RateScheduleRequest request) {
        schedule.setName(request.getName().trim());
        schedule.setVehicleType(request.getVehicleType());
        schedule.setRatePerHour(request.getRatePerHour());
        schedule.setGracePeriodMinutes(request.getGracePeriodMinutes());
    }

    private void ensureNoOtherActiveSchedule(VehicleType vehicleType, Long excludedId) {
        boolean conflict = rateScheduleRepository.findByVehicleTypeAndIsActiveTrue(vehicleType)
                .stream()
                .anyMatch(existing -> excludedId == null || !existing.getId().equals(excludedId));
        if (conflict) {
            throw new IllegalStateException(
                    "An active rate schedule already exists for vehicle type " + vehicleType
                            + ". Deactivate it before creating or activating another.");
        }
    }
}
