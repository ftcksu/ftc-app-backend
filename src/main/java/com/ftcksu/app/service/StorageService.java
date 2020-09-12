package com.ftcksu.app.service;

import com.ftcksu.app.config.StorageProperties;
import com.ftcksu.app.exception.custom.FileNotFoundException;
import com.ftcksu.app.exception.custom.StorageException;
import com.ftcksu.app.model.entity.ApprovalStatus;
import com.ftcksu.app.model.entity.ProfileImage;
import com.ftcksu.app.model.entity.User;
import com.ftcksu.app.repository.ImageRepository;
import com.ftcksu.app.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class StorageService {

    private final ImageRepository imageRepository;

    private final UserService userService;

    private final Path rootLocation;

    private final String EXTENSTION = ".jpg";

    private final String FORMAT = "jpg";

    @Autowired
    public StorageService(ImageRepository imageRepository, UserService userService, StorageProperties properties) {
        this.imageRepository = imageRepository;
        this.userService = userService;
        this.rootLocation = Paths.get(properties.getLocation());
    }


    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation.resolve("thumbs"));
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage location");
        }
    }


    @Transactional
    public void saveImage(MultipartFile file, Integer userId) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + fileName);
            }

            if (fileName.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                                + fileName);
            }

            ProfileImage profileImage = saveImage(fileName, file.getInputStream(),
                    userService.getUserById(userId));

            imageRepository.save(profileImage);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + fileName);
        }
    }


    @Transactional
    public void updateImage(Integer imageId, ApprovalStatus approvalStatus) {
        ProfileImage profileImage = imageRepository.getOne(imageId);
        profileImage.setApproved(approvalStatus);
        imageRepository.save(profileImage);

        if (approvalStatus == ApprovalStatus.APPROVED) {
            userService.updateProfileImage(profileImage.getUser().getId(), profileImage.getId());
        }
    }


    public List<ProfileImage> getAllImages() {
        return imageRepository.findAll();
    }


    public List<ProfileImage> getAllPendingImages() {
        return imageRepository.findProfileImagesByApprovedEquals(ApprovalStatus.WAITING);
    }

    private ProfileImage saveImage(String fileName, InputStream inputStream, User user) throws IOException {
        String strippedFileName = StringUtils.stripFilenameExtension(fileName);
        BufferedImage bufferedImage = ImageIO.read(inputStream);

        if (bufferedImage.getWidth() > 1920) {
            bufferedImage = ImageUtils.resize(bufferedImage);
        }

        strippedFileName = getUniqueFileName(strippedFileName);

        String imageFile = getFile(strippedFileName, false).toString();
        saveImage(bufferedImage, imageFile);


        String thumbFile = getFile(strippedFileName, true).toString();
        saveImage(ImageUtils.resize(bufferedImage, 0.3), thumbFile);

        return new ProfileImage(strippedFileName, imageFile, thumbFile, user);
    }

    private String getUniqueFileName(String fileName) {
        File file = new File(getFile(fileName, false).toString());
        String originalName = fileName;

        // Check if file exists, and modify the file name accordingly.
        for (int i = 2; file.exists(); i++) {
            fileName = originalName + "_" + i;
            file = new File(getFile(fileName, false).toString());
        }

        return fileName;
    }

    private void saveImage(BufferedImage bufferedImage, String fileName) {
        try (OutputStream os = new FileOutputStream(new File(fileName))) {
            ImageIO.write(bufferedImage, FORMAT, os);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private Path getFile(String fileName, boolean isThumbnail) {
        return isThumbnail ? rootLocation.resolve("thumbs").resolve(fileName + "thumb" + EXTENSTION) :
                rootLocation.resolve(fileName + EXTENSTION);
    }


    public Resource getUserImage(Integer userId, boolean isThumbnail) {
        User user = userService.getUserById(userId);
        ProfileImage profileImage = user.getProfileImage();

        if (profileImage == null) {
            throw new EntityNotFoundException("User has no image.");
        }

        String fileName = isThumbnail ? profileImage.getThumbName() : profileImage.getImageName();
        return loadImage(fileName);
    }


    public Resource loadImage(Integer imageId, boolean isThumbnail) {
        ProfileImage profileImage = imageRepository.getOne(imageId);
        String fileName = isThumbnail ? profileImage.getThumbName() : profileImage.getImageName();
        return loadImage(fileName);
    }

    private Resource loadImage(String fileName) {
        try {
            Path file = Paths.get(fileName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not read file: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Could not read file: " + fileName);
        }
    }


    public void deleteAll() {
        // Very Dangerous x_x, do not try UwU!!! << Mohammed Aljasser
        // FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }


    @Transactional
    public void deleteImage(Integer imageId) {
        imageRepository.delete(imageRepository.getOne(imageId));
    }
}
