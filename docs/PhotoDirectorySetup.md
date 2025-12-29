# Photo Directory Setup Guide
Copyright 2025, David Snyderman

This guide explains how to connect a photo directory on your local machine to the PhotoSort Docker container and configure the application to work with Git repositories.

## Overview

PhotoSort uses a Git-based workflow for photo management:
1. Photos are stored in a Git repository (local or remote like GitHub)
2. The repository directory is mounted into the Docker container
3. The application scans the directory for photos
4. Optionally, the application can automatically pull updates from a remote repository

## Prerequisites

- Docker and Docker Compose installed
- Git installed on your host machine
- A directory containing your photos (with or without a Git repository)

---

## Quick Start

```bash
# 1. Copy the example configuration
cp docker-compose.yml.example docker-compose.yml

# 2. Edit docker-compose.yml - update the photo directory path (see Step 2 below)

# 3. Start the application
./scripts/start.sh

# 4. Log in and click "Rescan Photos Now" on the Configuration page
```

---

## Step 1: Prepare Your Photo Directory

You have three options depending on your setup:

### Option A: Local-Only Repository (No Remote Sync)

Use this if your photos are only on your local machine and you don't need to sync with GitHub or another remote.

```bash
# Create directory if needed
mkdir -p /path/to/your/photos

# Initialize as a git repository
cd /path/to/your/photos
git init

# Add existing photos
git add .
git commit -m "Initial photo import"
```

**Configuration for local-only:**
- `GIT_REPO_PATH`: Set to container path (e.g., `/app/photos`)
- `GIT_REPO_URL`: Leave empty or omit entirely
- `GIT_USERNAME`: Not needed
- `GIT_TOKEN`: Not needed

The application will scan the directory but won't attempt git pull operations.

### Option B: Clone from a Remote Repository (GitHub, GitLab, etc.)

Use this if your photos are stored in a remote Git repository.

```bash
# Choose a location for your photos
mkdir -p /path/to/your/photos
cd /path/to/your/photos

# Clone the repository
git clone https://github.com/your-username/your-photos-repo.git .
```

**For private repositories**, use a personal access token:
```bash
git clone https://your-username:your-token@github.com/your-username/your-photos-repo.git .
```

**Configuration for remote sync:**
- `GIT_REPO_PATH`: Set to container path (e.g., `/app/photos`)
- `GIT_REPO_URL`: `https://github.com/your-username/your-photos-repo`
- `GIT_USERNAME`: Your GitHub username
- `GIT_TOKEN`: Your personal access token

### Option C: Existing Local Repository with Remote

If you already have a local repository that's connected to a remote:

```bash
# Verify remote is configured
cd /path/to/your/photos
git remote -v
# Should show: origin  https://github.com/... (fetch/push)
```

Use the same configuration as Option B.

---

## Step 2: Configure Docker Compose

### Initial Setup

```bash
# Copy the example file (only needed once)
cp docker-compose.yml.example docker-compose.yml
```

**Important:** `docker-compose.yml` is gitignored - your local configuration won't be committed to the repository.

### Edit the Photo Directory Mount

Open `docker-compose.yml` and find the backend volumes section. Update the photo directory path:

```yaml
services:
  backend:
    volumes:
      - backend_logs:/app/logs
      # Update this line with YOUR photo directory:
      - "/path/to/your/photos:/app/photos"
```

**Path Format Rules:**
| Scenario | Example |
|----------|---------|
| Simple path | `/home/user/Photos:/app/photos` |
| Path with spaces | `"/home/user/My Photos:/app/photos"` |
| Network drive | `/mnt/nas/family-photos:/app/photos` |

**The container path (`/app/photos`) must match the `GIT_REPO_PATH` environment variable.**

---

## Step 3: Configure Git Settings

### Option A: Environment Variables in docker-compose.yml

Edit the environment section of the backend service:

```yaml
services:
  backend:
    environment:
      # Path inside container (must match volume mount)
      GIT_REPO_PATH: /app/photos

      # Remote URL - set for automatic git pull, or leave empty for local-only
      GIT_REPO_URL: https://github.com/user/repo    # or leave empty

      # Authentication (only needed for private remote repositories)
      GIT_USERNAME: your-username
      GIT_TOKEN: your-personal-access-token

      # How often to check for updates (minutes)
      GIT_POLL_INTERVAL_MINUTES: 5
```

### Option B: Using a .env File (Recommended for Secrets)

Create a `.env` file in the project root:

```bash
# Git Configuration
GIT_REPO_URL=https://github.com/your-username/your-photos-repo
GIT_USERNAME=your-username
GIT_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxx
GIT_POLL_INTERVAL_MINUTES=10
```

**Note:** `.env` is already in `.gitignore` - your secrets won't be committed.

### Option C: Using the Configuration UI

After starting the application:

1. Log in as an admin user
2. Navigate to **Configuration** (`/configuration`)
3. Find **Git Configuration** section
4. Fill in the fields and click **Save**

---

## Step 4: Start the Application

```bash
# Start all containers
./scripts/start.sh

# Or using docker compose directly
docker compose up -d

# Verify containers are running
docker compose ps
```

---

## Step 5: Initial Photo Scan

After startup, trigger the initial scan:

1. Open `http://localhost:3000` in your browser
2. Log in with your admin account
3. Go to **Configuration** page
4. Click **"Rescan Photos Now"**

Monitor progress:
```bash
docker compose logs -f backend
```

---

## Configuration Scenarios

### Scenario 1: Local Photos, No Sync

Your photos are on your computer, no remote repository needed.

```yaml
# docker-compose.yml
environment:
  GIT_REPO_PATH: /app/photos
  # GIT_REPO_URL: (leave empty or omit)
volumes:
  - "/home/user/Pictures:/app/photos"
```

**Behavior:** Application scans `/app/photos` but doesn't attempt git pull.

### Scenario 2: Sync with GitHub

Your photos sync with a GitHub repository.

```yaml
# docker-compose.yml
environment:
  GIT_REPO_PATH: /app/photos
  GIT_REPO_URL: https://github.com/myuser/photos
  GIT_USERNAME: myuser
  GIT_TOKEN: ${GIT_TOKEN}  # From .env file
  GIT_POLL_INTERVAL_MINUTES: 10
volumes:
  - "/home/user/Pictures:/app/photos"
```

**Behavior:** Every 10 minutes, application runs `git pull` and processes new photos.

### Scenario 3: Git Repository Remote is on the Host Machine

Your photo repository's remote origin is on the same machine running Docker (or on your local network), not on GitHub. You want the application to automatically pull from this local remote.

**Understanding the Setup:**
- The container runs in an isolated network
- To access the host machine from inside the container, use `host.docker.internal` or the host's IP address
- A "bare repository" (e.g., `photos.git`) is a **directory** containing only git metadata, used as a central remote

**What is a bare repository?**
```bash
# A bare repo is a FOLDER, not a file. Create one with:
git init --bare /home/user/repos/photos.git

# This creates a directory structure like:
# /home/user/repos/photos.git/
#   ├── HEAD
#   ├── config
#   ├── objects/
#   ├── refs/
#   └── ...

# Then configure your working repo to use it as origin:
cd /home/user/Pictures
git remote add origin /home/user/repos/photos.git
git push -u origin main
```

**Option A: Mount the bare repository into the container (Recommended)**

Mount both your working directory AND the bare repo, then use a `file://` URL:

```yaml
# docker-compose.yml
services:
  backend:
    environment:
      GIT_REPO_PATH: /app/photos
      GIT_REPO_URL: file:///app/repos/photos.git  # file:// URL to mounted bare repo
    volumes:
      - "/home/user/Pictures:/app/photos"                       # Working directory
      - "/home/user/repos/photos.git:/app/repos/photos.git:ro"  # Bare repo (read-only)
```

**Behavior:** Every poll interval, the application runs `git pull` from the mounted bare repo and processes new photos.

**Option B: Use host.docker.internal with git daemon or SSH**

If you prefer not to mount the bare repo, you can expose it via network:

```yaml
# docker-compose.yml
services:
  backend:
    extra_hosts:
      - "host.docker.internal:host-gateway"  # Allows container to reach host
    environment:
      GIT_REPO_PATH: /app/photos
      # For git daemon (requires `git daemon` running on host):
      GIT_REPO_URL: git://host.docker.internal/repos/photos.git
      # Or for SSH (requires SSH server on host):
      # GIT_REPO_URL: ssh://user@host.docker.internal/home/user/repos/photos.git
    volumes:
      - "/home/user/Pictures:/app/photos"
```

**Note:** This requires running a git daemon or SSH server on your host.

**Option C: Local network server (Gitea, GitLab, etc.)**

If the git server is another machine on your network:

```yaml
# docker-compose.yml
environment:
  GIT_REPO_PATH: /app/photos
  GIT_REPO_URL: http://192.168.1.100:3000/user/photos.git  # Gitea/GitLab on LAN
  # Or SSH:
  # GIT_REPO_URL: ssh://git@192.168.1.100/repos/photos.git
  GIT_USERNAME: your-username
  GIT_TOKEN: your-token
```

### Scenario 4: No Remote - Manual Scans Only

Use this if you don't have or want a remote repository. Photos are added directly to the directory on the host, and you trigger scans manually.

```yaml
# docker-compose.yml
environment:
  GIT_REPO_PATH: /app/photos
  # GIT_REPO_URL: (leave empty or omit - no automatic git pull)
volumes:
  - "/home/user/Pictures:/app/photos"
```

**Workflow:**
```bash
# On your host machine, add photos and commit
cd /home/user/Pictures
cp /camera/DCIM/*.jpg .
git add .
git commit -m "Added vacation photos"
```

Then in the application UI, click **"Rescan Photos Now"** to process new photos.

**Behavior:** No automatic git pull. The application only scans when you manually trigger it. Use this for simple single-machine setups where you don't need remote sync.

### Scenario 5: Multiple Machines Syncing

You have the same repository cloned on multiple machines.

1. Each machine has its own `docker-compose.yml` with its local path
2. All point to the same remote `GIT_REPO_URL`
3. Each machine pulls updates automatically
4. Commit and push new photos from any machine to share with others

---

## How Automatic Syncing Works

When `GIT_REPO_URL` is configured:

1. **Scheduled Polling** (every N minutes):
   - Application executes `git pull` on the repository
   - Compares current commit with last processed commit
   - Identifies new or changed image files

2. **Photo Processing** (for each new/changed photo):
   - Extracts EXIF metadata (date, location, camera)
   - Generates thumbnails
   - Runs AI tagging (if configured)
   - Associates with user based on git commit author email

3. **Supported Formats:**
   `.jpg`, `.jpeg`, `.png`, `.gif`, `.bmp`, `.tiff`, `.tif`, `.webp`

When `GIT_REPO_URL` is empty:
- No automatic git pull
- Use "Rescan Photos Now" button to manually trigger scans
- Good for local-only setups where you add photos directly to the directory

---

## Configuration Reference

| Setting | Env Variable | Default | Description |
|---------|--------------|---------|-------------|
| Repository Path | `GIT_REPO_PATH` | `/app/photos` | Container path where photos are mounted |
| Repository URL | `GIT_REPO_URL` | (empty) | Remote Git URL; leave empty for local-only |
| Username | `GIT_USERNAME` | (empty) | Git username for private repos |
| Token | `GIT_TOKEN` | (empty) | Personal access token for private repos |
| Poll Interval | `GIT_POLL_INTERVAL_MINUTES` | `5` | Minutes between automatic git pulls |

---

## Troubleshooting

### Container won't start - Volume path error

**Error:** `service "backend" refers to undefined volume`

**Solution:** Ensure the path:
- Has a leading `/`
- Is quoted if it contains spaces
- Exists on the host machine

```yaml
# Wrong
- home/user/photos:/app/photos

# Correct
- "/home/user/photos:/app/photos"
```

### Photos not being detected

1. **Verify the mount:**
   ```bash
   docker compose exec backend ls -la /app/photos
   ```

2. **Check for .git directory:**
   ```bash
   docker compose exec backend ls -la /app/photos/.git
   ```

3. **View logs:**
   ```bash
   docker compose logs backend | grep -i "photo\|git\|scan"
   ```

### Git pull fails

**For private repositories**, ensure:
1. `GIT_USERNAME` is set correctly
2. `GIT_TOKEN` is a valid personal access token
3. The token has appropriate permissions

**Creating a GitHub Personal Access Token:**
1. GitHub → Settings → Developer settings → Personal access tokens
2. Generate new token (classic)
3. Select scopes: `repo` (private) or `public_repo` (public)
4. Copy and use as `GIT_TOKEN`

### Manual operations inside container

```bash
# Get a shell in the container
docker compose exec backend bash

# Check git status
cd /app/photos && git status

# Manual git pull
cd /app/photos && git pull

# List photos
ls -la /app/photos/*.jpg
```

### Permission denied errors

```bash
# Check ownership on host
ls -la /path/to/your/photos

# Fix permissions if needed
chmod -R 755 /path/to/your/photos
```

---

## File Organization

```
PhotoSort-V1/
├── docker-compose.yml.example    # Template (tracked in git)
├── docker-compose.yml            # Your local config (gitignored)
├── .env.example                  # Environment template (tracked)
├── .env                          # Your secrets (gitignored)
└── docs/
    └── PhotoDirectorySetup.md    # This guide
```

---

## Summary Checklist

- [ ] Prepare photo directory (with or without git init)
- [ ] Copy `docker-compose.yml.example` to `docker-compose.yml`
- [ ] Update volume mount with your photo directory path
- [ ] Configure `GIT_REPO_PATH` to match container mount point
- [ ] (Optional) Configure `GIT_REPO_URL` for automatic sync
- [ ] (Optional) Set `GIT_USERNAME` and `GIT_TOKEN` for private repos
- [ ] Start containers with `./scripts/start.sh`
- [ ] Click "Rescan Photos Now" on Configuration page
- [ ] Verify photos appear in the application
