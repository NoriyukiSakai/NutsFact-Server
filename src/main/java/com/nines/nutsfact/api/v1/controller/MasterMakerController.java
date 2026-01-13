package com.nines.nutsfact.api.v1.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nines.nutsfact.api.v1.request.MakerRequest;
import com.nines.nutsfact.domain.model.master.Maker;
import com.nines.nutsfact.domain.service.MakerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/apix/MasterMaker")
@RequiredArgsConstructor
public class MasterMakerController {

    private final MakerService makerService;

    @GetMapping("/getData")
    public ResponseEntity<Map<String, Object>> getData() {
        List<Maker> makers = makerService.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", makers.size());
        response.put("item", makers);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/findById")
    public ResponseEntity<Map<String, Object>> findById(@RequestParam("id") Integer id) {
        Maker maker = makerService.findById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("makerId", maker.getMakerId());
        response.put("makerName", maker.getMakerName());
        response.put("contactInfo", maker.getContactInfo());
        response.put("address", maker.getAddress());
        response.put("phoneNumber", maker.getPhoneNumber());
        response.put("email", maker.getEmail());
        response.put("isActive", maker.getIsActive());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insert(@Valid @RequestBody MakerRequest request) {
        Maker maker = convertToEntity(request);
        Maker created = makerService.create(maker);
        return ResponseEntity.ok(buildResponse(created));
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody MakerRequest request) {
        Maker maker = convertToEntity(request);
        Maker updated = makerService.update(request.getMakerId(), maker);
        return ResponseEntity.ok(buildResponse(updated));
    }

    @GetMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam("id") Integer id) {
        makerService.delete(id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getSelect")
    public ResponseEntity<Map<String, Object>> getSelect() {
        List<Maker> makers = makerService.findAll();
        List<Map<String, Object>> selectItems = makers.stream()
            .filter(m -> m.getIsActive() != null && m.getIsActive())
            .map(m -> {
                Map<String, Object> item = new HashMap<>();
                item.put("value", m.getMakerId());
                item.put("label", m.getMakerName());
                return item;
            })
            .toList();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("records", selectItems.size());
        response.put("item", selectItems);
        return ResponseEntity.ok(response);
    }

    private Maker convertToEntity(MakerRequest request) {
        Maker maker = new Maker();
        maker.setMakerId(request.getMakerId());
        maker.setMakerName(request.getMakerName());
        maker.setContactInfo(request.getContactInfo());
        maker.setAddress(request.getAddress());
        maker.setPhoneNumber(request.getPhoneNumber());
        maker.setEmail(request.getEmail());
        maker.setIsActive(request.getIsActive());
        return maker;
    }

    private Map<String, Object> buildResponse(Maker maker) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("makerId", maker.getMakerId());
        response.put("makerName", maker.getMakerName());
        response.put("contactInfo", maker.getContactInfo());
        response.put("address", maker.getAddress());
        response.put("phoneNumber", maker.getPhoneNumber());
        response.put("email", maker.getEmail());
        response.put("isActive", maker.getIsActive());
        return response;
    }
}
