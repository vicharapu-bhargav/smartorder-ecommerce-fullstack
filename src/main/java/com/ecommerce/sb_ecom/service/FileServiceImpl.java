package com.ecommerce.sb_ecom.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService{

    @Override
    public String uploadImage(String path, MultipartFile image) {
        //File name of current/original file
        String originalFilename = image.getOriginalFilename();

        //Generate a unique file name
        String uniqueID = UUID.randomUUID().toString();
        String fileName = uniqueID.concat(originalFilename.substring(originalFilename.lastIndexOf('.')));
        String filePath = path + File.separator +fileName;

        //Check if path exist else create it
        File folder =  new File(path);
        if(!folder.exists()){
            folder.mkdir();
        }

        //Upload to server
        try {
            Files.copy(image.getInputStream(), Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileName;
    }
}
