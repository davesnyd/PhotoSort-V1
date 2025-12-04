/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.jpeg.JpegDirectory;
import com.photoSort.model.ExifData;
import com.photoSort.model.GitPollState;
import com.photoSort.model.Photo;
import com.photoSort.repository.ExifDataRepository;
import com.photoSort.repository.GitPollStateRepository;
import com.photoSort.repository.PhotoRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for polling Git repository for new/changed photo files (Step 14)
 * Runs on a scheduled interval to detect and process image files
 */
@Service
public class GitPollingService {

    private static final Logger logger = LoggerFactory.getLogger(GitPollingService.class);

    // Supported image file extensions
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".tif", ".webp"
    );

    @Autowired
    private ConfigService configService;

    @Autowired
    private GitPollStateRepository gitPollStateRepository;

    @Autowired
    private ExifDataService exifDataService;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private ExifDataRepository exifDataRepository;

    /**
     * Poll Git repository at configured interval
     * Fixed delay ensures previous execution completes before next starts
     * Interval is configured in minutes, converted to milliseconds (* 60 * 1000)
     */
    @Scheduled(fixedDelayString = "#{${git.poll.interval.minutes:5} * 60 * 1000}",
               initialDelayString = "60000") // 1 minute initial delay
    public void pollRepository() {
        logger.info("Starting Git repository poll");

        try {
            // Get Git configuration
            String repoPath = configService.getProperty("git.repo.path", "/path/to/repo");
            String repoUrl = configService.getProperty("git.repo.url", "");
            String username = configService.getProperty("git.username", "");
            String token = configService.getProperty("git.token", "");

            // Validate configuration
            if (repoPath.equals("/path/to/repo") || repoPath.isEmpty()) {
                logger.warn("Git repository path not configured, skipping poll");
                return;
            }

            File repoDir = new File(repoPath);
            if (!repoDir.exists()) {
                logger.error("Git repository directory does not exist: {}", repoPath);
                return;
            }

            // Get or create poll state
            GitPollState pollState = gitPollStateRepository.findByRepositoryPath(repoPath)
                .orElse(new GitPollState(repoPath));

            String lastCommitHash = pollState.getLastCommitHash();

            // Open Git repository
            Repository repository = openRepository(repoDir);
            if (repository == null) {
                return;
            }

            try (Git git = new Git(repository)) {
                // Execute git pull
                PullResult pullResult = executePull(git, username, token);
                if (pullResult == null) {
                    return;
                }

                logger.info("Git pull completed successfully");

                // Get current HEAD commit
                ObjectId headCommit = repository.resolve("HEAD");
                if (headCommit == null) {
                    logger.warn("No HEAD commit found in repository");
                    return;
                }

                String currentCommitHash = headCommit.getName();

                // If this is first poll or commit changed, detect changes
                if (lastCommitHash == null || !lastCommitHash.equals(currentCommitHash)) {
                    logger.info("Detecting changes between commits: {} -> {}",
                        lastCommitHash != null ? lastCommitHash : "initial", currentCommitHash);

                    List<String> changedImageFiles = detectChangedImageFiles(
                        repository, lastCommitHash, currentCommitHash);

                    logger.info("Detected {} changed image file(s)", changedImageFiles.size());

                    // Process changed files (placeholder for future steps)
                    for (String filePath : changedImageFiles) {
                        processImageFile(new File(repoDir, filePath));
                    }

                    // Update poll state
                    pollState.setLastCommitHash(currentCommitHash);
                } else {
                    logger.info("No new commits since last poll");
                }

                // Update poll time
                pollState.setLastPollTime(LocalDateTime.now());
                gitPollStateRepository.save(pollState);

                logger.info("Git repository poll completed successfully");

            } finally {
                repository.close();
            }

        } catch (Exception e) {
            logger.error("Unexpected error during Git repository poll: {}", e.getMessage(), e);
        }
    }

    /**
     * Open Git repository
     */
    private Repository openRepository(File repoDir) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(new File(repoDir, ".git"))
                .readEnvironment()
                .findGitDir()
                .build();
            return repository;
        } catch (IOException e) {
            logger.error("Failed to open Git repository at {}: {}", repoDir, e.getMessage());
            return null;
        }
    }

    /**
     * Execute git pull with authentication
     */
    private PullResult executePull(Git git, String username, String token) {
        try {
            if (username != null && !username.isEmpty() && token != null && !token.isEmpty()) {
                return git.pull()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                    .call();
            } else {
                // No authentication (local repository or public repo)
                return git.pull().call();
            }
        } catch (GitAPIException e) {
            logger.error("Failed to execute git pull: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Detect changed image files between two commits
     */
    private List<String> detectChangedImageFiles(Repository repository, String oldCommitHash, String newCommitHash) {
        List<String> changedFiles = new ArrayList<>();

        try (RevWalk revWalk = new RevWalk(repository);
             ObjectReader reader = repository.newObjectReader()) {

            RevCommit newCommit = revWalk.parseCommit(repository.resolve(newCommitHash));

            // If no old commit (first poll), process all files in new commit
            if (oldCommitHash == null) {
                logger.info("First poll - processing all image files in repository");
                // List all files in the current commit
                try (org.eclipse.jgit.treewalk.TreeWalk treeWalk = new org.eclipse.jgit.treewalk.TreeWalk(repository)) {
                    treeWalk.addTree(newCommit.getTree());
                    treeWalk.setRecursive(true);
                    while (treeWalk.next()) {
                        String filePath = treeWalk.getPathString();
                        if (isImageFile(filePath)) {
                            changedFiles.add(filePath);
                        }
                    }
                }
                logger.info("Found {} image file(s) in repository", changedFiles.size());
                return changedFiles;
            }

            RevCommit oldCommit = revWalk.parseCommit(repository.resolve(oldCommitHash));

            // Get tree iterators for diff
            AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(reader, oldCommit);
            AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(reader, newCommit);

            // Compute diff
            try (Git git = new Git(repository)) {
                List<DiffEntry> diffs = git.diff()
                    .setOldTree(oldTreeIterator)
                    .setNewTree(newTreeIterator)
                    .call();

                // Filter for image files
                for (DiffEntry diff : diffs) {
                    String filePath = diff.getNewPath();
                    if (isImageFile(filePath)) {
                        changedFiles.add(filePath);
                        logger.debug("Detected changed image file: {} ({})", filePath, diff.getChangeType());
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error detecting changed files: {}", e.getMessage(), e);
        }

        return changedFiles;
    }

    /**
     * Get tree parser for commit
     */
    private AbstractTreeIterator getCanonicalTreeParser(ObjectReader reader, RevCommit commit) throws IOException {
        RevTree tree = commit.getTree();
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        treeParser.reset(reader, tree.getId());
        return treeParser;
    }

    /**
     * Check if file is an image based on extension
     */
    private boolean isImageFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        String lowerPath = filePath.toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(lowerPath::endsWith);
    }

    /**
     * Process image file - extract EXIF data and create/update photo record
     */
    private void processImageFile(File imageFile) {
        if (!imageFile.exists() || !imageFile.isFile()) {
            logger.warn("Image file does not exist or is not a file: {}", imageFile.getPath());
            return;
        }

        try {
            logger.info("Processing image file: {}", imageFile.getName());

            // Extract EXIF data
            ExifData exifData = exifDataService.extractExifData(imageFile);
            if (exifData != null) {
                logger.debug("Extracted EXIF data from: {}", imageFile.getName());
            } else {
                logger.debug("No EXIF data found in: {}", imageFile.getName());
            }

            // Extract image dimensions (not part of EXIF data, but file metadata)
            Integer imageWidth = null;
            Integer imageHeight = null;
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
                JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
                if (jpegDirectory != null) {
                    imageWidth = jpegDirectory.getImageWidth();
                    imageHeight = jpegDirectory.getImageHeight();
                }
            } catch (Exception e) {
                logger.debug("Could not extract image dimensions from {}: {}",
                    imageFile.getName(), e.getMessage());
            }

            // Get file metadata
            BasicFileAttributes attrs = Files.readAttributes(imageFile.toPath(), BasicFileAttributes.class);
            Long fileSize = attrs.size();
            LocalDateTime fileCreatedDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(attrs.creationTime().toMillis()), ZoneId.systemDefault());
            LocalDateTime fileModifiedDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(attrs.lastModifiedTime().toMillis()), ZoneId.systemDefault());

            // Check if photo already exists
            String filePath = imageFile.getAbsolutePath();
            Optional<Photo> existingPhotoOpt = photoRepository.findByFilePath(filePath);

            Photo photo;
            if (existingPhotoOpt.isPresent()) {
                // Update existing photo
                photo = existingPhotoOpt.get();
                logger.debug("Updating existing photo: {}", photo.getFileName());
            } else {
                // Create new photo
                photo = new Photo();
                photo.setFilePath(filePath);
                logger.debug("Creating new photo record for: {}", imageFile.getName());
            }

            // Set photo properties
            photo.setFileName(imageFile.getName());
            photo.setFileSize(fileSize);
            photo.setFileCreatedDate(fileCreatedDate);
            photo.setFileModifiedDate(fileModifiedDate);
            photo.setImageWidth(imageWidth);
            photo.setImageHeight(imageHeight);
            // Note: owner will be set to null for now (will be assigned by admin or script later)
            // Note: isPublic defaults to false (private by default)

            // Save photo
            photo = photoRepository.save(photo);
            logger.info("Saved photo record: {} (ID: {})", photo.getFileName(), photo.getPhotoId());

            // Save EXIF data if extracted
            if (exifData != null) {
                // Check if EXIF data already exists for this photo
                Optional<ExifData> existingExifOpt = exifDataRepository.findByPhoto(photo);
                if (existingExifOpt.isPresent()) {
                    // Delete old EXIF data (we'll replace it with new)
                    exifDataRepository.delete(existingExifOpt.get());
                }

                // Associate EXIF data with photo and save
                exifData.setPhoto(photo);
                exifDataRepository.save(exifData);
                logger.info("Saved EXIF data for photo: {}", photo.getFileName());
            }

        } catch (Exception e) {
            logger.error("Error processing image file {}: {}", imageFile.getName(), e.getMessage(), e);
        }
    }
}
