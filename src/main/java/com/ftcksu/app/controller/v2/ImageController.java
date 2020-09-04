package com.ftcksu.app.controller.v2;

import com.ftcksu.app.model.entity.ApprovalStatus;
import com.ftcksu.app.model.response.AcceptedResponse;
import com.ftcksu.app.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/v2/images")
public class ImageController {

    private final StorageService storageService;

    @Autowired
    public ImageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<?> getAllImages() {
        return ResponseEntity.ok(new AcceptedResponse<>(storageService.getAllImages()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> loadImage(@PathVariable Integer id) {
        Resource resource = storageService.loadImage(id, false);
        String resourceName = resource == null ? "default" : resource.getFilename();

        return ResponseEntity.ok().header(
                HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceName + "\"")
                .contentType(MediaType.IMAGE_JPEG).body(resource);
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> uploadFile(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        storageService.saveImage(file, id);
        return ResponseEntity.ok(new AcceptedResponse<>("Image saved successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Integer id) {
        storageService.deleteImage(id);
        return ResponseEntity.ok(new AcceptedResponse<>("Image deleted successfully."));
    }

    @GetMapping("/{id}/thumb")
    public ResponseEntity<Resource> loadThumbnail(@PathVariable Integer id) {
        Resource resource = storageService.loadImage(id, true);
        String resourceName = resource == null ? "default" : resource.getFilename();

        return ResponseEntity.ok().header(
                HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceName + "\"")
                .contentType(MediaType.IMAGE_JPEG).body(resource);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getAllPendingImages() {
        return ResponseEntity.ok(new AcceptedResponse<>(storageService.getAllPendingImages()));
    }

    @PutMapping("/pending/{id}")
    public ResponseEntity<?> updatePendingImage(@PathVariable Integer id,
                                                @RequestParam(name = "approval_status") ApprovalStatus approvalStatus) {
        storageService.updateImage(id, approvalStatus);
        return ResponseEntity.ok(new AcceptedResponse<>("Pending image has been updated successfully."));
    }

}
