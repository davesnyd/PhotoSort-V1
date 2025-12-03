/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.controller;

import com.photoSort.dto.ApiResponse;
import com.photoSort.model.Script;
import com.photoSort.repository.ScriptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for script management endpoints (Step 11: Scripts Table Page)
 * Provides CRUD operations for automated scripts
 */
@RestController
@RequestMapping("/api/scripts")
public class ScriptController {

    @Autowired
    private ScriptRepository scriptRepository;

    /**
     * Get all scripts
     *
     * @return List of all scripts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Script>>> getAllScripts() {
        try {
            List<Script> scripts = scriptRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(scripts));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                          "Error retrieving scripts: " + e.getMessage()));
        }
    }

    /**
     * Get script by ID
     *
     * @param id Script ID
     * @return Script details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Script>> getScriptById(@PathVariable Long id) {
        try {
            Script script = scriptRepository.findById(id).orElse(null);
            if (script == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("SCRIPT_NOT_FOUND", "Script not found"));
            }
            return ResponseEntity.ok(ApiResponse.success(script));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                          "Error retrieving script: " + e.getMessage()));
        }
    }

    /**
     * Create new script
     *
     * @param script Script data
     * @return Created script
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Script>> createScript(@RequestBody Script script) {
        try {
            // Clear ID to ensure new entity
            script.setScriptId(null);

            Script savedScript = scriptRepository.save(script);
            return ResponseEntity.ok(ApiResponse.success(savedScript));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                          "Error creating script: " + e.getMessage()));
        }
    }

    /**
     * Update existing script
     *
     * @param id Script ID
     * @param script Updated script data
     * @return Updated script
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Script>> updateScript(
            @PathVariable Long id,
            @RequestBody Script script) {
        try {
            // Verify script exists
            Script existing = scriptRepository.findById(id).orElse(null);
            if (existing == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("SCRIPT_NOT_FOUND", "Script not found"));
            }

            // Ensure ID matches path parameter
            script.setScriptId(id);

            Script savedScript = scriptRepository.save(script);
            return ResponseEntity.ok(ApiResponse.success(savedScript));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                          "Error updating script: " + e.getMessage()));
        }
    }

    /**
     * Delete script
     *
     * @param id Script ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScript(@PathVariable Long id) {
        try {
            // Verify script exists
            Script existing = scriptRepository.findById(id).orElse(null);
            if (existing == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("SCRIPT_NOT_FOUND", "Script not found"));
            }

            scriptRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR",
                          "Error deleting script: " + e.getMessage()));
        }
    }
}
