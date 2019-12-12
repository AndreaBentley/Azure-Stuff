package com.example.AzureBlobDemo.AppendBlobs;

import com.example.AzureBlobDemo.BlobClientProvider;
import com.example.AzureBlobDemo.DataGenerator;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Random;


// C:\Users\bentley\Desktop\Avantgarde\Tracciati e esempi Data Factory SAC FIRECOP\File import per data factory
// \AlberoProccessi righe 20190611.xlsx


public class GiveAppendsAShot {

    public static void runSamples() throws Throwable {

        CloudBlobClient blobClient;
        blobClient = BlobClientProvider.getBlobClientReference();
        CloudBlobContainer container = createContainer(blobClient, "yabbadabba3");

        try {
            basicAppendBlobOperations(container);
        }
        catch (StorageException s) {
            if (s.getErrorCode().equals("BlobTypeNotSupported")) {
                System.out.println(String.format("\t\tError: %s", s.getMessage()));
            }
            else if (s.getErrorCode().equals("FeatureNotSupportedByEmulator")) {
                System.out.println("\t\tError: The append blob feature is currently not supported by the Storage Emulator.");
                System.out.println("\t\tPlease run the sample against your Azure Storage account by updating the config.properties file.\n\n");
            }
            else {
                throw s;
            }
        }

    }


    private static void basicAppendBlobOperations(CloudBlobContainer container) throws StorageException, IOException, IllegalArgumentException, URISyntaxException {
        // Create sample files for use
        Random random = new Random();
        System.out.println("\tCreating sample files between 128KB-256KB in size for upload demonstration.");
        File tempFile1 = DataGenerator.createTempLocalFile("appendblob-", ".tmp", (128 * 1024) + random.nextInt(128 * 1024));
        System.out.println(String.format("\t\tSuccessfully created the file \"%s\".", tempFile1.getAbsolutePath()));
        File tempFile2 = DataGenerator.createTempLocalFile("appendblob-", ".tmp", (128 * 1024) + random.nextInt(128 * 1024));
        System.out.println(String.format("\t\tSuccessfully created the file \"%s\".", tempFile2.getAbsolutePath()));

        // Create an append blob and append data to it from the sample file
        System.out.println("\n\tCreate an empty append blob and append data to it from the sample files.");
        CloudAppendBlob appendBlob = container.getAppendBlobReference("appendblob.tmp");
        appendBlob.createOrReplace();
        appendBlob.appendFromFile(tempFile1.getAbsolutePath());
        appendBlob.appendFromFile(tempFile2.getAbsolutePath());
        System.out.println("\t\tSuccessfully created the append blob and appended data to it.");

        // Write random data blocks to the end of the append blob
        byte[] randomBytes = new byte[4096];
        for (int i = 0; i < 8; i++) {
            random.nextBytes(randomBytes);
            appendBlob.appendFromByteArray(randomBytes, 0, 4096);
        }

        // Download the blob
        if (appendBlob != null) {
            System.out.println("\n\tDownload the blob.");
            String downloadedAppendBlobPath = String.format("%scopyof-%s", System.getProperty("java.io.tmpdir"), appendBlob.getName());
            System.out.println(String.format("\t\tDownload the blob from \"%s\" to \"%s\".", appendBlob.getUri().toURL(), downloadedAppendBlobPath));
            appendBlob.downloadToFile(downloadedAppendBlobPath);
            new File(downloadedAppendBlobPath).deleteOnExit();
            System.out.println("\t\t\tSuccessfully downloaded the blob.");
        }

    }


    private static CloudBlobContainer createContainer(CloudBlobClient blobClient, String containerName) throws com.microsoft.azure.storage.StorageException, RuntimeException, IOException, InvalidKeyException, IllegalArgumentException, URISyntaxException, IllegalStateException {

        // "getContainerReference" returns an address, it
        // does not create the container by itself
        CloudBlobContainer container = blobClient.getContainerReference(containerName);

        try {
            if(container.exists()){
                System.out.println("Oops, container " + containerName + " already exists, it's fine keep going.");
            } else {
                System.out.println("Container " + containerName + " does NOT already exists, giving it life.");
                container.createIfNotExists();
                System.out.println(String.format("\tSuccessfully created the container \"%s\".", container.getName()));
            }
        }
        catch (StorageException s) {
            if (s.getCause() instanceof java.net.ConnectException) {
                System.out.println("Caught connection exception from the client. If running with the default configuration please make sure you have started the storage emulator.");
            }
            throw s;
        }

        return container;

    }

}
