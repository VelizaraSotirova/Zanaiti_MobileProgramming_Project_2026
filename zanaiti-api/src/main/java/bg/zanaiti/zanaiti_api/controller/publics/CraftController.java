package bg.zanaiti.zanaiti_api.controller.publics;

import bg.zanaiti.zanaiti_api.dto.Craft.CraftDto;
import bg.zanaiti.zanaiti_api.service.publics.PublicCraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crafts")
@RequiredArgsConstructor
public class CraftController {

    private final PublicCraftService craftService;

    @GetMapping
    public ResponseEntity<List<CraftDto>> getAllCrafts() {
        return ResponseEntity.ok(craftService.getAllCrafts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CraftDto> getCraftById(@PathVariable Long id) {
        return ResponseEntity.ok(craftService.getCraftById(id));
    }

    @GetMapping("/{id}/language/{languageCode}")
    public ResponseEntity<CraftDto> getCraftByIdAndLanguage(
            @PathVariable Long id,
            @PathVariable String languageCode) {
        return ResponseEntity.ok(craftService.getCraftByIdAndLanguage(id, languageCode));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<CraftDto>> getCraftsNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radius) {
        return ResponseEntity.ok(craftService.getCraftsInRadius(lat, lng, radius));
    }
}