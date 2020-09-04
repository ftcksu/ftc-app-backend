package com.ftcksu.app.controller.v2;

import com.ftcksu.app.model.entity.MOTD;
import com.ftcksu.app.model.response.AcceptedResponse;
import com.ftcksu.app.service.MOTDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v2/motd")
public class MOTDController {

    private final MOTDService motdService;

    @Autowired
    public MOTDController(MOTDService motdService) {
        this.motdService = motdService;
    }

    @GetMapping
    public ResponseEntity<?> getMOTD() {
        return ResponseEntity.ok(new AcceptedResponse<>(motdService.getMOTD()));
    }

    @PostMapping
    public ResponseEntity<?> addMOTD(@RequestBody MOTD motd) {
        motdService.createNewMOTD(motd);
        return ResponseEntity.ok(new AcceptedResponse<>("Message of the day added successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMOTD(@PathVariable Integer id) {
        motdService.deleteMOTD(id);
        return ResponseEntity.ok(new AcceptedResponse<>("Message of the day deleted successfully."));
    }

}