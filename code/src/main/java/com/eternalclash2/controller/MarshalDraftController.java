package com.eternalclash2.controller;

import com.eternalclash2.dto.MarshalCandidateDto;
import com.eternalclash2.dto.MarshalDto;
import com.eternalclash2.service.MarshalCandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players/{playerId}")
@RequiredArgsConstructor
public class MarshalDraftController {

    private final MarshalCandidateService marshalCandidateService;

    // คืนจอมพลเฉพาะช่องที่ผู้เล่นกำลังเห็น ตามกฎ Reroll สูงสุด 2 ครั้ง
    @GetMapping("/marshal-candidates")
    public ResponseEntity<List<MarshalCandidateDto>> getCurrentCandidate(@PathVariable Long playerId) {
        return ResponseEntity.ok(marshalCandidateService.findForPlayer(playerId).stream()
                .map(MarshalCandidateDto::from).toList());
    }

    @PostMapping("/marshal-candidates/reroll")
    public ResponseEntity<MarshalCandidateDto> reroll(@PathVariable Long playerId) {
        return ResponseEntity.ok(MarshalCandidateDto.from(marshalCandidateService.reroll(playerId)));
    }

    @PostMapping("/marshal-candidates/choose")
    public ResponseEntity<MarshalDto> chooseCurrent(@PathVariable Long playerId) {
        return ResponseEntity.ok(MarshalDto.from(marshalCandidateService.chooseCurrent(playerId)));
    }
}
