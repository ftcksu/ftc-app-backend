package com.ftcksu.app.service;

import com.ftcksu.app.model.entity.MOTD;
import com.ftcksu.app.repository.MOTDRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MOTDService {

    private final MOTDRepository motdRepository;

    @Autowired
    public MOTDService(MOTDRepository motdRepository) {
        this.motdRepository = motdRepository;
    }


    public MOTD getMOTD() {
        return motdRepository.findFirstByOrderByIdDesc();
    }


    @Transactional
    public MOTD createNewMOTD(MOTD motd) {
        return motdRepository.save(motd);
    }


    @Transactional
    public void deleteMOTD(Integer motdId) {
        motdRepository.delete(motdRepository.getOne(motdId));
    }

}
