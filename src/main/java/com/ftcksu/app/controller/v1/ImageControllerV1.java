package com.ftcksu.app.controller.v1;

import com.ftcksu.app.model.entity.ApprovalStatus;
import com.ftcksu.app.model.entity.ProfileImage;
import com.ftcksu.app.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/images")
@Deprecated
public class ImageControllerV1 {

    private final StorageService storageService;

    @Autowired
    public ImageControllerV1(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public List<ProfileImage> getAllImages() {
        return storageService.getAllImages();
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
    public String uploadFile(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        storageService.saveImage(file, id);
        return "Image saved successfully.";
    }

    @DeleteMapping("/{id}")
    public String deleteImage(@PathVariable Integer id) {
        storageService.deleteImage(id);
        return "Image deleted.";
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
    public List<ProfileImage> getAllPendingImages() {
        return storageService.getAllPendingImages();
    }

    @PutMapping("/pending/{id}")
    public String updatePendingImage(@PathVariable Integer id,
                                     @RequestParam(name = "approval_status") ApprovalStatus approvalStatus) {
        storageService.updateImage(id, approvalStatus);
        return "Pending image has been updated successfully.";
    }

}

