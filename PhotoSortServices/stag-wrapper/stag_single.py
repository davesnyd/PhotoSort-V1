#!/usr/bin/env python3
"""
STAG Single File Wrapper
Copyright 2025, David Snyderman

Wrapper script to run STAG on a single image and output tags to stdout.
Designed for integration with PhotoSort application.

Usage: python3 stag_single.py <image_path>
Output: comma-separated tags to stdout
"""

import argparse
import os
import sys
from pathlib import Path

# Add STAG directory to path
STAG_DIR = os.environ.get('STAG_DIR', '/app/stag')
sys.path.insert(0, STAG_DIR)

import torch
from huggingface_hub import hf_hub_download
from PIL import Image
from pillow_heif import register_heif_opener
from ram import get_transform, inference_ram as inference
from ram.models import ram_plus

# Try to import rawpy for RAW file support
try:
    import rawpy
    HAS_RAWPY = True
except ImportError:
    HAS_RAWPY = False

# RAW file extensions
RAW_EXTENSIONS = {
    ".3fr", ".ari", ".arw", ".bay", ".cr2", ".cr3", ".cap", ".data",
    ".dcr", ".dng", ".drf", ".eip", ".erf", ".fff", ".gpr", ".iiq",
    ".k25", ".kdc", ".mdc", ".mef", ".mos", ".mrw", ".nef", ".nrw",
    ".orf", ".pef", ".ptx", ".pxn", ".r3d", ".raf", ".raw", ".rwl",
    ".rw2", ".rwz", ".sr2", ".srf", ".srw", ".x3f"
}

# Global model cache to avoid reloading
_model = None
_transform = None
_device = None


def get_model():
    """Load and cache the STAG model."""
    global _model, _transform, _device

    if _model is None:
        register_heif_opener()

        # Download model from Hugging Face
        pretrained = hf_hub_download(
            repo_id="xinyu1205/recognize-anything-plus-model",
            filename="ram_plus_swin_large_14m.pth"
        )

        _device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        _transform = get_transform(image_size=384)

        _model = ram_plus(pretrained=pretrained, image_size=384, vit='swin_l')
        _model.eval()
        _model = _model.to(_device)

        # Log to stderr so it doesn't interfere with tag output
        print(f"STAG model loaded, using device: {_device}", file=sys.stderr)

    return _model, _transform, _device


def load_image(image_path):
    """Load an image using PIL or rawpy."""
    ext = Path(image_path).suffix.lower()

    # Skip XMP files
    if ext == ".xmp":
        return None

    image = None

    # Try PIL first for common formats
    if ext not in RAW_EXTENSIONS:
        try:
            image = Image.open(image_path)
            return image
        except Exception as e:
            print(f"PIL cannot read {image_path}: {e}", file=sys.stderr)

    # Try rawpy for RAW files
    if image is None and HAS_RAWPY:
        try:
            with rawpy.imread(image_path) as raw:
                rgb = raw.postprocess()
                image = Image.fromarray(rgb)
                return image
        except Exception as e:
            print(f"rawpy cannot read {image_path}: {e}", file=sys.stderr)

    return image


def generate_tags(image_path):
    """Generate tags for a single image."""
    # Load image
    image = load_image(image_path)
    if image is None:
        print(f"Could not load image: {image_path}", file=sys.stderr)
        return []

    # Get model
    model, transform, device = get_model()

    try:
        # Transform and inference
        torch_image = transform(image).unsqueeze(0).to(device)
        result = inference(torch_image, model)

        # Parse tags (STAG returns pipe-separated string)
        tag_string = result[0] if result else ""
        tags = [tag.strip() for tag in tag_string.split("|") if tag.strip()]

        return tags
    except Exception as e:
        print(f"Error generating tags: {e}", file=sys.stderr)
        return []


def main():
    parser = argparse.ArgumentParser(description='Generate AI tags for a single image')
    parser.add_argument('image_path', help='Path to the image file')
    args = parser.parse_args()

    if not os.path.exists(args.image_path):
        print(f"Error: File not found: {args.image_path}", file=sys.stderr)
        sys.exit(1)

    tags = generate_tags(args.image_path)

    # Output comma-separated tags to stdout
    if tags:
        print(",".join(tags))

    sys.exit(0)


if __name__ == "__main__":
    main()
