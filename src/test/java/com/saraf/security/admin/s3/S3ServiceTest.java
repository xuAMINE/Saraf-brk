package com.saraf.security.admin.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.saraf.security.exception.TransferNotFoundException;
import com.saraf.service.transfer.Transfer;
import com.saraf.service.transfer.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.net.URL;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class S3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveReceipt_Success() throws Exception {
        // Setup mock transfer entity
        Transfer transfer = new Transfer();
        transfer.setId(1);
        transfer.setUser(new com.saraf.security.user.User());
        transfer.getUser().setId(2);

        // Setup mock file
        MockMultipartFile receipt = new MockMultipartFile("receipt", "receipt.jpg", "image/jpeg", "test content".getBytes());

        // Mock repository and S3 interactions
        when(transferRepository.findById(1)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);

        // Call the method
        Transfer result = s3Service.saveReceipt(1, receipt);

        // Verify interactions and assertions
        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));
        assertNotNull(result.getReceipt());
        assertTrue(result.getReceipt().contains("receipt/2/receipt_"));
    }

    @Test
    void saveReceipt_TransferNotFound() {
        // Setup mock file
        MockMultipartFile receipt = new MockMultipartFile("receipt", "receipt.jpg", "image/jpeg", "test content".getBytes());

        // Mock repository to throw TransferNotFoundException
        when(transferRepository.findById(anyInt())).thenThrow(new TransferNotFoundException("Transfer not found"));

        // Test method and expect exception
        assertThrows(TransferNotFoundException.class, () -> s3Service.saveReceipt(1, receipt));

        // Verify that no interaction with S3 occurred
        verify(amazonS3, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void generatePreSignedUrl_Success() {
        // Setup test data
        String fileName = "receipt/test.jpg";
        int expirationInMinutes = 10;
        Date expectedExpiration = new Date(System.currentTimeMillis() + expirationInMinutes * 60 * 1000);

        // Mock URL returned by S3
        URL mockUrl = mock(URL.class);
        when(mockUrl.toString()).thenReturn("http://mockurl.com");

        // Mock S3 generatePresignedUrl call
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(mockUrl);

        // Call the method
        String preSignedUrl = s3Service.generatePreSignedUrl(fileName, expirationInMinutes);

        // Verify interactions
        ArgumentCaptor<GeneratePresignedUrlRequest> captor = ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);
        verify(amazonS3, times(1)).generatePresignedUrl(captor.capture());

        // Assert pre-signed URL and expiration time
        assertEquals("http://mockurl.com", preSignedUrl);
        GeneratePresignedUrlRequest requestCaptured = captor.getValue();
        assertEquals(HttpMethod.GET, requestCaptured.getMethod());
        assertEquals(fileName, requestCaptured.getKey());
        assertTrue(Math.abs(requestCaptured.getExpiration().getTime() - expectedExpiration.getTime()) < 1000);
    }
}
