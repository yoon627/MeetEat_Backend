package com.zb.meeteat.domain.restaurant.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.zb.meeteat.exception.UserCustomException;
import com.zb.meeteat.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3ImageUpload {

  @Value("${cloud.aws.bucketName}")
  private String bucket;

  private final AmazonS3 amazonS3;

  public String uploadImage(MultipartFile file) {
    String newFileName = createFileName(file.getOriginalFilename());

    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(file.getContentType());
    objectMetadata.setContentLength(file.getSize());
//    objectMetadata.setCacheControl("no-cache");

    try (InputStream inputStream = file.getInputStream()) {
      amazonS3.putObject(bucket, newFileName, inputStream, objectMetadata);
      return newFileName;
    } catch (IOException e) {
      throw new UserCustomException(ErrorCode.FAIL_FILE_UPLOAD);
    }
  }

  // S3에 저장되어있는 미디어 파일 삭제
  public void deleteFile(String fileName) {
    amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
  }

  private String createFileName(String originalFileName) {
    String extension = validateFileExtension(originalFileName);

    String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    return timestamp + extension;  // 예: 20230213094505000.jpg
  }

  private String validateFileExtension(String fileName) {
    List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "raw", "heic", "heif");

    assert fileName != null;
    int dotIndex = fileName.lastIndexOf(".");
    String extention = "";
    if (dotIndex > 0) {
      extention = fileName.substring(dotIndex + 1);
    } else {
      extention = "";
    }

    String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new UserCustomException(ErrorCode.INVALID_FILE_FORMAT);
    }

    return extention;
  }

}