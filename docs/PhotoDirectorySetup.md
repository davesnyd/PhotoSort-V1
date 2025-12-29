# Photo Directory Setup Guide
Copyright 2025, David Snyderman

This guide explains how to connect a photo directory on your local machine to the PhotoSort Docker container.

## Overview

PhotoSort uses a Git-based workflow for photo management:
1. Photos are stored in a Git repository
2. The repository is mounted into the Docker container
3. The application scans for photos and automatically pulls updates

## Prerequisites

- Docker and Docker Compose installed
- Git installed on your host machine

---

## Quick Start

```bash
# 1. Copy the example configuration
cp docker-compose.yml.example docker-compose.yml

# 2. Edit docker-compose.yml - update the photo directory paths (see Step 3 below)

# 3. Create/configure your .env file with git settings

# 4. Start the application
./scripts/start.sh

# 5. Log in and click "Rescan Photos Now" on the Configuration page
```

---

## Configuration Scenarios

Choose the scenario that matches your setup:

| Scenario | Description | Automatic Git Pull? |
|----------|-------------|---------------------|
| [Scenario 1](#scenario-1-local-photos-no-git-sync) | Photos on local disk, no remote sync | No |
| [Scenario 2](#scenario-2-sync-with-github-or-remote-server) | Sync with GitHub/GitLab | Yes |
| [Scenario 3](#scenario-3-local-git-server-on-host-machine) | Host machine acts as git server | Yes |

---

## Scenario 1: Local Photos, No Git Sync

Use this if your photos are only on your local machine and you don't need automatic git pull.

### Step 1: Prepare your photo directory

```bash
# Create directory if needed
mkdir -p /path/to/your/photos

# Initialize as a git repository
cd /path/to/your/photos
git init
git add .
git commit -m "Initial photo import"
```

### Step 2: Configure docker-compose.yml

```yaml
services:
  backend:
    volumes:
      - backend_logs:/app/logs
      - /path/to/your/photos:/app/photos
```

### Step 3: Configure .env

```bash
GIT_REPO_PATH=/app/photos
GIT_REPO_URL=
```

### Step 4: Fix permissions

```bash
chmod -R o+w /path/to/your/photos/.git
```

### Workflow

Add photos manually and use "Rescan Photos Now" in the UI:
```bash
cp /camera/*.jpg /path/to/your/photos/
cd /path/to/your/photos
git add . && git commit -m "Added photos"
# Then click "Rescan Photos Now" in the Configuration page
```

---

## Scenario 2: Sync with GitHub or Remote Server

Use this if your photos sync with a GitHub/GitLab repository.

### Step 1: Clone the repository

```bash
git clone https://github.com/your-username/your-photos-repo.git /path/to/photos
```

### Step 2: Configure docker-compose.yml

```yaml
services:
  backend:
    volumes:
      - backend_logs:/app/logs
      - /path/to/photos:/app/photos
```

### Step 3: Configure .env

```bash
GIT_REPO_PATH=/app/photos
GIT_REPO_URL=https://github.com/your-username/your-photos-repo
GIT_USERNAME=your-username
GIT_TOKEN=your-personal-access-token
```

### Step 4: Fix permissions

```bash
chmod -R o+w /path/to/photos/.git
```

### Workflow

The application automatically pulls from GitHub every 5 minutes and processes new photos.

---

## Scenario 3: Local Git Server on Host Machine

Use this if you want your host machine to act as a git server for the container. This allows automatic git pull without needing GitHub.

### Architecture

```
Host Machine:
├── /path/to/Photos.git          (bare repo - acts as "server")
└── /path/to/photosWorkingCopy   (working copy - mounted in container)

Container:
├── /app/repos/Photos.git        (mounted bare repo)
└── /app/photos                  (mounted working copy)
```

### Step 1: Create the bare repository (git server)

A bare repository is a directory containing only git metadata (no working files). It acts as a central server.

```bash
# Create directory for bare repo
mkdir -p /home/user/dockerpaths

# If you have an existing photo repository, create bare clone from it:
git clone --bare /path/to/existing/photos /home/user/dockerpaths/Photos.git

# OR if starting fresh, initialize empty bare repo:
git init --bare /home/user/dockerpaths/Photos.git
```

### Step 2: Create the working copy for the container

```bash
# Clone from the bare repo
git clone /home/user/dockerpaths/Photos.git /home/user/dockerpaths/photosWorkingCopy

# Verify photos are there
ls /home/user/dockerpaths/photosWorkingCopy
```

### Step 3: Set the git remote to use container paths

The working copy's remote must point to where the bare repo will be mounted inside the container:

```bash
cd /home/user/dockerpaths/photosWorkingCopy
git remote set-url origin /app/repos/Photos.git

# Verify
git remote -v
# Should show: origin  /app/repos/Photos.git (fetch)
#              origin  /app/repos/Photos.git (push)
```

### Step 4: Configure docker-compose.yml

Mount both the working copy AND the bare repo:

```yaml
services:
  backend:
    volumes:
      - backend_logs:/app/logs
      - /home/user/dockerpaths/photosWorkingCopy:/app/photos
      - /home/user/dockerpaths/Photos.git:/app/repos/Photos.git:ro
```

**Note:** The bare repo is mounted read-only (`:ro`) since the container only pulls from it.

### Step 5: Configure .env

```bash
GIT_REPO_PATH=/app/photos
GIT_REPO_URL=file:///app/repos/Photos.git
GIT_USERNAME=
GIT_TOKEN=
```

### Step 6: Fix permissions (CRITICAL)

The container runs as user `photosort` (uid 100), but the mounted directories are owned by your host user. The container needs write access to the `.git` directory:

```bash
chmod -R o+w /home/user/dockerpaths/photosWorkingCopy/.git
```

### Step 7: Start and test

```bash
./scripts/start.sh

# Check logs for successful processing
docker compose logs -f backend | grep -i "git\|photo\|processed"
```

You should see:
```
Starting Git repository poll
Successfully processed image file: photo1.jpg
...
Git repository poll completed successfully, processed N files
```

### Workflow for Adding New Photos

```bash
# 1. Add photos to the working copy on host
cp /camera/*.jpg /home/user/dockerpaths/photosWorkingCopy/

# 2. Commit and push to bare repo
cd /home/user/dockerpaths/photosWorkingCopy
git add .
git commit -m "Added vacation photos"
git push origin main

# 3. Container automatically pulls on next poll cycle (every 5 min)
#    Or click "Rescan Photos Now" in the Configuration page
```

---

## Configuration Reference

### Environment Variables (.env file)

| Variable | Description | Example |
|----------|-------------|---------|
| `GIT_REPO_PATH` | Path inside container where photos are mounted | `/app/photos` |
| `GIT_REPO_URL` | Remote URL for git pull (or empty for no auto-pull) | `file:///app/repos/Photos.git` |
| `GIT_USERNAME` | Username for authenticated repos | `myuser` |
| `GIT_TOKEN` | Personal access token for authenticated repos | `ghp_xxx` |
| `GIT_POLL_INTERVAL_MINUTES` | How often to pull (default: 5) | `10` |

### docker-compose.yml Volume Formats

```yaml
volumes:
  # Simple path
  - /home/user/photos:/app/photos

  # Path with spaces (use quotes)
  - "/home/user/My Photos:/app/photos"

  # Read-only mount
  - /path/to/repo.git:/app/repos/repo.git:ro
```

---

## Troubleshooting

### Error: "Creating lock file failed" or permission errors

**Cause:** Container user can't write to mounted `.git` directory.

**Solution:**
```bash
chmod -R o+w /path/to/your/photos/.git
```

### Error: "Invalid remote: origin"

**Cause:** The git remote URL points to a path that doesn't exist inside the container.

**Solution:** Ensure the remote URL uses container paths:
```bash
cd /path/to/working/copy
git remote set-url origin /app/repos/Photos.git  # Container path, not host path
```

### Photos not appearing after scan

1. **Check logs:**
   ```bash
   docker compose logs backend | grep -i "git\|photo\|error"
   ```

2. **Verify mounts:**
   ```bash
   docker compose exec backend sh -c "ls -la /app/photos"
   docker compose exec backend sh -c "ls -la /app/repos/Photos.git"
   ```

3. **Check git remote inside container context:**
   ```bash
   cat /path/to/workingcopy/.git/config
   # Remote URL should use /app/... paths
   ```

### Container won't start - volume path error

**Error:** `service "backend" refers to undefined volume`

**Solution:** Ensure paths have leading `/` and use quotes if they contain spaces:
```yaml
# Wrong
- home/user/photos:/app/photos

# Correct
- /home/user/photos:/app/photos
- "/home/user/My Photos:/app/photos"
```

### Manual operations

```bash
# Get shell in container
docker compose exec backend sh

# Check what container sees
ls -la /app/photos
ls -la /app/repos/Photos.git

# View container user
id
# Output: uid=100(photosort) gid=101(photosort)
```

---

## File Organization

```
PhotoSort-V1/
├── docker-compose.yml.example    # Template (tracked in git)
├── docker-compose.yml            # Your local config (gitignored)
├── .env.example                  # Environment template
├── .env                          # Your local settings (gitignored)
└── docs/
    └── PhotoDirectorySetup.md    # This guide

Host directories (example for Scenario 3):
/home/user/dockerpaths/
├── Photos.git/                   # Bare repository (git server)
└── photosWorkingCopy/            # Working copy (mounted in container)
    ├── .git/                     # Must have o+w permissions
    ├── photo1.jpg
    └── photo2.jpg
```

---

## Summary Checklist

### For Scenario 3 (Local Git Server):

- [ ] Create bare repository: `git clone --bare /source /path/Photos.git`
- [ ] Create working copy: `git clone /path/Photos.git /path/photosWorkingCopy`
- [ ] Set remote to container path: `git remote set-url origin /app/repos/Photos.git`
- [ ] Update `docker-compose.yml` with both volume mounts
- [ ] Update `.env` with `GIT_REPO_URL=file:///app/repos/Photos.git`
- [ ] Fix permissions: `chmod -R o+w /path/photosWorkingCopy/.git`
- [ ] Start containers: `./scripts/start.sh`
- [ ] Verify in logs: `docker compose logs backend | grep -i "processed"`
- [ ] Check Photos page in UI
