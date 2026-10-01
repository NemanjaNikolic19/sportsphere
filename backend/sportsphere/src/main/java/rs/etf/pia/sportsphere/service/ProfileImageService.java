package rs.etf.pia.sportsphere.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileImageService {

    private static final String DEFAULT_IMAGE_NAME =
        "default.png";

    private static final long MAX_IMAGE_SIZE =
        5 * 1024 * 1024;

    private static final Set<String>
        ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg"
        );

    private final Path uploadDirectory =
        Paths.get(
            "uploads",
            "profile-images"
        )
        .toAbsolutePath()
        .normalize();

    public ProfileImageService() {
        try {
            Files.createDirectories(uploadDirectory);
            createDefaultImageIfMissing();
        } catch(IOException e) {
            throw new IllegalStateException(
                "Could not create profile image directory",
                e
            );
        }
    }

    public String saveOrDefault(
            MultipartFile image) {

        if(image == null || image.isEmpty()) {
            return DEFAULT_IMAGE_NAME;
        }

        validateImage(image);

        String extension =
            "image/png".equals(image.getContentType())
                ? "png"
                : "jpg";

        String fileName =
            UUID.randomUUID() + "." + extension;

        Path destination =
            uploadDirectory.resolve(fileName);

        try {
            image.transferTo(destination);
        } catch(IOException e) {
            throw new IllegalArgumentException(
                "Could not save profile image"
            );
        }

        return "/api/profile-images/" + fileName;
    }

    public String saveRequired(MultipartFile image) {
        if(image == null || image.isEmpty()) {
            throw new IllegalArgumentException(
                "Profile image is required"
            );
        }

        return saveOrDefault(image);
    }

    public String getDefaultImagePath() {
        return DEFAULT_IMAGE_NAME;
    }

    public void deleteIfUploaded(String imagePath) {
        if(imagePath == null || imagePath.isBlank()) {
            return;
        }

        String fileName =
            Paths.get(imagePath).getFileName().toString();

        if(DEFAULT_IMAGE_NAME.equals(fileName)) {
            return;
        }

        try {
            Files.deleteIfExists(
                uploadDirectory.resolve(fileName)
            );
        } catch(IOException ignored) {
            // A failed cleanup must not undo a successful profile update.
        }
    }

    private void createDefaultImageIfMissing()
            throws IOException {

        Path defaultImagePath =
            uploadDirectory.resolve(DEFAULT_IMAGE_NAME);

        if(Files.exists(defaultImagePath)) {
            return;
        }

        BufferedImage image =
            new BufferedImage(
                512,
                512,
                BufferedImage.TYPE_INT_ARGB
            );

        Graphics2D graphics = image.createGraphics();

        try {
            graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.setColor(new Color(219, 234, 254));
            graphics.fillRect(0, 0, 512, 512);

            graphics.setColor(new Color(96, 165, 250));
            graphics.fillOval(156, 80, 200, 200);
            graphics.fillOval(76, 292, 360, 300);
        } finally {
            graphics.dispose();
        }

        ImageIO.write(
            image,
            "png",
            defaultImagePath.toFile()
        );
    }

    private void validateImage(
            MultipartFile image) {

        if(image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException(
                "Profile image must not exceed 5 MB"
            );
        }

        String contentType = image.getContentType();

        if(
            contentType == null ||
            !ALLOWED_CONTENT_TYPES.contains(contentType)
        ) {
            throw new IllegalArgumentException(
                "Profile image must be PNG or JPG"
            );
        }

        try(InputStream input = image.getInputStream()) {
            BufferedImage decodedImage =
                ImageIO.read(input);

            if(decodedImage == null) {
                throw new IllegalArgumentException(
                    "Invalid profile image file"
                );
            }
        } catch(IOException e) {
            throw new IllegalArgumentException(
                "Could not read profile image"
            );
        }
    }
}
