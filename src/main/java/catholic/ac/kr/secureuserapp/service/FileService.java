package catholic.ac.kr.secureuserapp.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {
    // Đường dẫn thư mục lưu file (nếu chưa có sẽ tạo mới)
    private final Path rootLocation= Paths.get("uploads");


    public FileService(){
        try {
            Files.createDirectories(rootLocation);
        }catch (IOException e){
            throw new RuntimeException("Không thể tạo thư mục upload!",e);
        }
    }
    // Lưu file được upload vào thư mục
    @PreAuthorize("hasRole('USER')")
    public String storeFile(MultipartFile file){
        try {
            if(file.isEmpty()){
                throw new RuntimeException("File rỗng!");
            }
            // Lấy tên gốc của file
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            // Sao chép dữ liệu file vào thư mục đích
            Files.copy(file.getInputStream(),rootLocation.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        }catch (IOException e){
            throw new RuntimeException("Lỗi khi lưu file!",e);
        }
    }
    // Tải file từ thư mục để trả về client
    @PreAuthorize("hasRole('USER')")
    public Resource loadFile(String fileName){
        try {
            Path filePath = rootLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
//          Kiểm tra file có tồn tại và có thể đọc không
            if(resource.exists() && resource.isReadable()){
                return resource;
            }else {
                throw new RuntimeException("Không tìm thấy hoặc không đọc được file:"+fileName);
            }
        }catch (MalformedURLException e){
            throw new RuntimeException("Lỗi khi tải file",e);
        }
    }

}
