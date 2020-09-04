package com.ftcksu.app.controller.v1;

import com.ftcksu.app.model.entity.MOTD;
import com.ftcksu.app.service.MOTDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/motd")
@Deprecated
public class MOTDControllerV1 {

    private final MOTDService motdService;

    @Autowired
    public MOTDControllerV1(MOTDService motdService) {
        this.motdService = motdService;
    }

    @GetMapping
    public MOTD getMOTD() {
        return motdService.getMOTD();
    }

    @PostMapping
    public String addMOTD(@RequestBody MOTD motd) {
        motdService.createNewMOTD(motd);
        return "Message of the day added successfully.";
    }

    @DeleteMapping("/{id}")
    public String deleteMOTD(@PathVariable Integer id) {
        motdService.deleteMOTD(id);
        return "Message of the day deleted successfully.";
    }

}
