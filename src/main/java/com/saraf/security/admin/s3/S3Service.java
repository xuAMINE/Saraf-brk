package com.saraf.security.admin.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.saraf.security.exception.TransferNotFoundException;
import com.saraf.service.transfer.Transfer;
import com.saraf.service.transfer.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    private final TransferRepository transferRepository;

    private final String BUCKET_NAME = "saraf-brk";

    public Transfer saveReceipt(Integer id, MultipartFile receipt) throws IOException {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found"));
        Integer userId = transfer.getUser().getId();

        String originalFilename = receipt.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String uniqueId = UUID.randomUUID().toString();
        String fileName = String.format("receipt/%d/receipt_%s_%s.%s", transfer.getUser().getId(), timestamp, uniqueId, extension);

        InputStream inputStream = receipt.getInputStream();
        amazonS3.putObject(new PutObjectRequest(BUCKET_NAME, fileName, inputStream, null));

        transfer.setReceipt(fileName);
        return transferRepository.save(transfer);
    }

    public String generatePreSignedUrl(String fileName, int expirationInMinutes) {
        // Set the URL expiration time
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * expirationInMinutes; // Set expiration time
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(BUCKET_NAME, fileName)
                        .withMethod(com.amazonaws.HttpMethod.GET)
                        .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }
}
