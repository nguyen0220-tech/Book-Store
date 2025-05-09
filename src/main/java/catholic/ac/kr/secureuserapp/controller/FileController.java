package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("files")
public class FileController {
    private final FileService fileService;

    @PostMapping("upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile file) {
        String fileName = fileService.storeFile(file);
        return ResponseEntity.ok("Upload thành công: "+fileName);
    }

    @GetMapping("download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        Resource resource = fileService.loadFile(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=\""+resource.getFilename()+"\"")
                .body(resource);
    }
}
