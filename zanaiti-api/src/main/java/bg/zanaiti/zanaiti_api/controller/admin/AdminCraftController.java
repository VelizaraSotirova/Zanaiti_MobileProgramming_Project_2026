package bg.zanaiti.zanaiti_api.controller.admin;

import bg.zanaiti.zanaiti_api.dto.Craft.CraftDto;
import bg.zanaiti.zanaiti_api.dto.Craft.CreateCraftDto;
import bg.zanaiti.zanaiti_api.security.CustomUserDetails;
import bg.zanaiti.zanaiti_api.service.admin.AdminCraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/crafts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCraftController {

    private final AdminCraftService adminCraftService;

    @PostMapping
    public ResponseEntity<CraftDto> createCraft(
            @RequestBody CreateCraftDto dto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long adminId = currentUser.getUser().getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCraftService.createCraft(dto, adminId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CraftDto> updateCraft(
            @PathVariable Long id,
            @RequestBody CraftDto dto) {
        return ResponseEntity.ok(adminCraftService.updateCraft(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCraft(@PathVariable Long id) {
        adminCraftService.deleteCraft(id);
        return ResponseEntity.noContent().build();
    }
}