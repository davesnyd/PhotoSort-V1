/*
 * Copyright 2025, David Snyderman
 *
 * Tags Section - Editable photo tags
 */

import React, { useState } from 'react';

const TagsSection = ({ tags, onUpdate }) => {
  const [newTag, setNewTag] = useState('');

  const handleAddTag = () => {
    if (newTag.trim()) {
      const tagValues = tags.map(t => t.tagValue);
      if (!tagValues.includes(newTag.trim())) {
        onUpdate([...tagValues, newTag.trim()]);
      }
      setNewTag('');
    }
  };

  const handleDeleteTag = (tagToDelete) => {
    const updatedTags = tags
      .filter(t => t.tagValue !== tagToDelete.tagValue)
      .map(t => t.tagValue);
    onUpdate(updatedTags);
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleAddTag();
    }
  };

  return (
    <div className="tags-section">
      <h3>Tags</h3>
      <div className="tags-list">
        {tags.length === 0 && (
          <p className="no-tags">No tags</p>
        )}

        {tags.map((tag) => (
          <div key={tag.tagId} className="tag-chip">
            <span>{tag.tagValue}</span>
            <button
              onClick={() => handleDeleteTag(tag)}
              className="tag-delete-btn"
              title="Remove tag"
            >
              ✕
            </button>
          </div>
        ))}
      </div>

      <div className="add-tag-input">
        <input
          type="text"
          value={newTag}
          onChange={(e) => setNewTag(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Add tag..."
        />
        <button onClick={handleAddTag} className="add-tag-btn">
          Add
        </button>
      </div>
    </div>
  );
};

export default TagsSection;
