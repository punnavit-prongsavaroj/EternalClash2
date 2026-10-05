package com.eternalclash2.service;

import com.eternalclash2.domain.entity.Marshal;
import com.eternalclash2.repository.MarshalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MarshalService {
    private final MarshalRepository marshalRepository;

    @Transactional
    public List<Marshal> ensureDefaultMarshals() {
        Map<String, Marshal> existing = marshalRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Marshal::getName, m -> m));
        List<Marshal> defaults = List.of(
                marshal("จูล่ง", "เดินทัพเร็ว", "ลดเวลาเดินทัพ 1 Turn.", "ไม่มี", 20, 20, 1.00, false, "FAST_MARCH"),
                marshal("ลิโป้", "โจมตีรุนแรง", "ทหาร 1 คนฆ่าศัตรูได้ 2 คน.", "ผลิตอาหารได้ 15 ต่อ Action.", 15, 20, 2.00, false, null),
                marshal("จิวยี่", "ไม่มีอุบัติเหตุ", "ไม่ถูก Accident ระหว่างเดินทาง.", "การใช้อาหารเพิ่มขึ้น 25%.", 20, 20, 1.00, false, "NO_ACCIDENT"),
                marshal("ขงเบ้ง", "รอดจากเมืองแตก", "มีโอกาส 50% ที่เมืองจะยังอยู่เมื่อทหารหมด.", "มีโอกาส 20% ที่ส่งกองทัพไม่ออกและเสีย Action.", 20, 20, 1.00, false, "SURVIVE_DESTRUCTION"),
                marshal("ซุนกวน", "ผลิตอาหารเก่ง", "ผลิตอาหารได้ 25 ต่อ Action.", "ทหาร 1.5 คนฆ่าศัตรูได้ 1 คน.", 25, 20, 0.67, false, null),
                marshal("เล่าปี่", "สร้างทหารเก่ง", "สร้างทหารได้ 25 ต่อ Action.", "เปิดเผยเป้าหมายการโจมตี.", 20, 25, 1.00, true, null),
                marshal("โจโฉ", "กบฏ", "ผลิตอาหารและสร้างทหารได้ 25 ต่อ Action.", "มีโอกาส 5% ต่อ Turn ที่อาหารและทหารจะลดครึ่งหนึ่ง.", 25, 25, 1.00, false, "REBELLION")
        );
        for (Marshal candidate : defaults) {
            Marshal saved = existing.get(candidate.getName());
            if (saved == null) marshalRepository.save(candidate);
        }
        return marshalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Marshal> findAll() { return marshalRepository.findAll(); }

    @Transactional(readOnly = true)
    public Marshal findById(Long id) {
        return marshalRepository.findById(id).orElseThrow(() -> new com.eternalclash2.exception.ResourceNotFoundException("Marshal not found with id: " + id));
    }

    private Marshal marshal(String name, String abilityName, String ability, String disadvantage,
                            int food, int soldiers, double killRatio, boolean revealsTarget, String specialType) {
        return Marshal.builder().name(name).abilityName(abilityName).abilityDescription(ability)
                .disadvantageDescription(disadvantage).foodProduction(food).soldierProduction(soldiers)
                .attackKillRatio(killRatio).revealsAttackTarget(revealsTarget).specialAbilityType(specialType).build();
    }
}
