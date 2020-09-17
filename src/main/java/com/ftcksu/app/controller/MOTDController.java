package com.ftcksu.app.controller;

import com.ftcksu.app.model.entity.MOTD;
import com.ftcksu.app.model.response.ResponseTemplate;
import com.ftcksu.app.service.MOTDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/motd")
public class MOTDController {

    private final MOTDService motdService;

    @Autowired
    public MOTDController(MOTDService motdService) {
        this.motdService = motdService;
    }

    @GetMapping
    public ResponseEntity<?> getMOTD() {
        return ResponseEntity.ok(new ResponseTemplate<>(motdService.getMOTD()));
    }

    @PostMapping
    public ResponseEntity<?> addMOTD(@AuthenticationPrincipal UserDetails principal, @RequestBody MOTD motd) {
        Integer userId = Integer.parseInt(principal.getUsername());
        motd.setUserId(userId);
        motdService.createNewMOTD(motd);
        return ResponseEntity.ok(new ResponseTemplate<>("Message of the day added successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMOTD(@PathVariable Integer id) {
        motdService.deleteMOTD(id);
        return ResponseEntity.ok(new ResponseTemplate<>("Message of the day deleted successfully."));
    }

}
