package com.thecoderscorner.menu.web.controller;

import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.web.domain.CodeBuildInfo;
import com.thecoderscorner.menu.web.domain.PublishableCodePluginItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/v1/generator/plugins")
public class CodePluginController {
    private final CodePluginManager pluginManager;
    private final EmbeddedPlatforms embeddedPlatforms;
    private final Map<String, byte[]> imageCache = new ConcurrentHashMap<>();
    
    public CodePluginController(CodePluginManager pluginManager, EmbeddedPlatforms embeddedPlatforms) {
        this.pluginManager = pluginManager;
        this.embeddedPlatforms = embeddedPlatforms;
    }
    
    @PostMapping("byIdList")
    @ResponseBody
    public ResponseEntity<List<PublishableCodePluginItem>> getById(@RequestBody List<String> ids) {
        log.info("Getting plugins for {}", ids);

        var output = new ArrayList<PublishableCodePluginItem>();
        if(ids == null || ids.isEmpty() || ids.size() > 10) {
            log.info("Failing because either 0 or  too many plugins");
            return ResponseEntity.badRequest().body(List.of());
        }
        
        for(var id : ids) {
            pluginManager.getPluginById(id)
                    .map(PublishableCodePluginItem::fromPlugin)
                    .ifPresent(output::add);
        }
        log.info("Returning {} plugins", output.size());
        return ResponseEntity.ok(output);
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<PublishableCodePluginItem>> search(@RequestParam("query") String s,
                                                                  @RequestParam("subsystem") String subsystem,
                                                                  @RequestParam("platform") String platform) {
        log.info("Searching for {} as a {} on {}", s, subsystem, platform);
        var pl = embeddedPlatforms.getEmbeddedPlatformFromId(platform);
        var l = pluginManager.getPluginsThatMatch(pl, SubSystem.valueOf(subsystem));

        if(s.equals("*")) {
            return ResponseEntity.ok(l.stream().map(PublishableCodePluginItem::fromPlugin).toList());
        } else {
            return ResponseEntity.ok(l.stream()
                    .filter(p -> p.getDescription().toLowerCase().contains(s.toLowerCase()) ||
                            p.getExtendedDescription().toLowerCase().contains(s.toLowerCase()))
                    .map(PublishableCodePluginItem::fromPlugin)
                    .limit(10)
                    .toList());
        }
    }

    @GetMapping("/imgById/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getImageById(@PathVariable String id) {

        if (imageCache.containsKey(id)) {
            log.info("Returning cached image for {}", id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header("Cache-Control", "max-age=2592000") // 30 days
                    .body(imageCache.get(id));
        }

        log.info("Getting image for {}", id);

        var pl = pluginManager.getPluginById(id);
        if(pl.isEmpty()) return ResponseEntity.notFound().build();
        var image = pluginManager.getImageForName(pl.get());
        if(image.isEmpty()) return ResponseEntity.notFound().build();

        try(var baos = new ByteArrayOutputStream()) {
            var png = pl.get().isImagePng();
            if(ImageIO.write(image.get(), png ? "png" : "jpeg", baos)) {
                var bytes = baos.toByteArray();
                if (imageCache.size() < 100) {
                    imageCache.put(id, bytes);
                }
                return ResponseEntity.ok()
                        .contentType(png ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG)
                        .header("Cache-Control", "max-age=2592000") // 30 days
                        .body(bytes);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            log.error("Failed to write image {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
