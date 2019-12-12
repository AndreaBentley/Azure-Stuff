package com.example.AzureBlobDemo.PageBlobs;

import com.example.AzureBlobDemo.BlobClientProvider;
import com.example.AzureBlobDemo.DataGenerator;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Random;


// C:\Users\bentley\Desktop\Avantgarde\Tracciati e esempi Data Factory SAC FIRECOP\File import per data factory
// \AlberoProccessi righe 20190611.xlsx


public class GivePagesAShot {

    public static void runSamples() throws Throwable {

        CloudBlobClient blobClient;
        blobClient = BlobClientProvider.getBlobClientReference();
        CloudBlobContainer container = createContainer(blobClient, "yabbadabba4");

        try {
            basicPageBlobOperations(container);
        }
        catch (StorageException s) {
            if (s.getErrorCode().equals("BlobTypeNotSupported")) {
                System.out.println(String.format("\t\tError: %s", s.getMessage()));
            }
            else if (s.getErrorCode().equals("FeatureNotSupportedByEmulator")) {
                System.out.println("\t\tError: The append blob feature is currently not supported by the Storage Emulator.");
                System.out.println("\t\tPlease run the sample against your Azure Storage account by updating the config.properties file.");
            }
            else {
                throw s;
            }
        }

    }


    private static void basicPageBlobOperations(CloudBlobContainer container) throws StorageException, IOException, IllegalArgumentException, URISyntaxException {

        // Create sample files for use. We use a file whose size is aligned to 512 bytes since page blobs are expected to be aligned to 512 byte pages.
        System.out.println("\tCreating sample file 128KB in size (aligned to 512 bytes) for upload demonstration.");
        File tempFile = DataGenerator.createTempLocalFile("pageblob-", ".tmp", (128 * 1024));
        System.out.println(String.format("\t\tSuccessfully created the file \"%s\".", tempFile.getAbsolutePath()));

        // Upload the sample file sparsely as a page blob (Only upload certain ranges of the file)
        System.out.println("\n\tUpload the sample file sparsely as a page blob.");
        System.out.println("\t\tCreating an empty page blob of the same size as the sample file.");
        CloudPageBlob pageBlob = container.getPageBlobReference("pageblob.tmp");
        pageBlob.create(tempFile.length()); // This will throw an IllegalArgumentException if the size if not aligned to 512 bytes.

        // Upload selective pages to the blob
        System.out.println("\t\tUploading selective pages to the blob.");
        FileInputStream tempFileInputStream = null;
        try {
            tempFileInputStream = new FileInputStream(tempFile);
            System.out.println("\t\t\tUploading range start: 0, length: 1024.");
            pageBlob.uploadPages(tempFileInputStream, 0, 1024);
            System.out.println("\t\t\tUploading range start: 4096, length: 1536.");
            pageBlob.uploadPages(tempFileInputStream, 4096, 1536);
        }
        catch (Throwable t) {
            throw t;
        }
        finally {
            if (tempFileInputStream != null) {
                tempFileInputStream.close();
            }
        }
        System.out.println("\t\t\tSuccessfully uploaded the blob sparsely.");

        // Create a read-only snapshot of the blob
        System.out.println("\n\tCreate a read-only snapshot of the blob.");
        CloudBlob pageBlobSnapshot = pageBlob.createSnapshot();
        System.out.println("\t\tSuccessfully created a snapshot of the blob.");

        // Upload new pages to the blob, modify and clear existing pages
        System.out.println("\n\tModify the blob by uploading new pages to the blob and clearing existing pages.");
        tempFileInputStream = null;
        try {
            tempFileInputStream = new FileInputStream(tempFile);
            System.out.println("\t\t\tUploading range start: 8192, length: 4096.");
            tempFileInputStream.getChannel().position(8192);
            pageBlob.uploadPages(tempFileInputStream, 8192, 4096);
            System.out.println("\t\t\tClearing range start: 4608, length: 512.");
            pageBlob.clearPages(4608, 512);
        }
        catch (Throwable t) {
            throw t;
        }
        finally {
            if (tempFileInputStream != null) {
                tempFileInputStream.close();
            }
        }
        System.out.println("\t\t\tSuccessfully modified the blob.");

        // Query valid page ranges
        System.out.println("\n\tQuery valid page ranges.");
        for (PageRange pageRange : pageBlob.downloadPageRanges()) {
            System.out.println(String.format("\t\tRange start offset: %d, end offset: %d", pageRange.getStartOffset(), pageRange.getEndOffset()));
        }

        // Query page range diff between snapshots
        System.out.println("\n\tQuery page range diff between the snapshot and the current state.");
        for (PageRange pageRange : pageBlob.downloadPageRangesDiff(pageBlobSnapshot.getSnapshotID())) {
            System.out.println(String.format("\t\tRange start offset: %d, end offset: %d", pageRange.getStartOffset(), pageRange.getEndOffset()));
        }

        // Download the blob and its snapshot
        System.out.println("\n\tDownload the blob and its snapshot.");

        String downloadedPageBlobSnapshotPath = String.format("%ssnapshotof-%s", System.getProperty("java.io.tmpdir"), pageBlobSnapshot.getName());
        System.out.println(String.format("\t\tDownload the blob snapshot from \"%s\" to \"%s\".", pageBlobSnapshot.getUri().toURL(), downloadedPageBlobSnapshotPath));
        pageBlobSnapshot.downloadToFile(downloadedPageBlobSnapshotPath);
        new File(downloadedPageBlobSnapshotPath).deleteOnExit();
        System.out.println("\t\t\tSuccessfully downloaded the blob snapshot.");

        String downloadedPageBlobPath = String.format("%scopyof-%s", System.getProperty("java.io.tmpdir"), pageBlob.getName());
        System.out.println(String.format("\t\tDownload the blob from \"%s\" to \"%s\".", pageBlob.getUri().toURL(), downloadedPageBlobPath));
        pageBlob.downloadToFile(downloadedPageBlobPath);
        new File(downloadedPageBlobPath).deleteOnExit();
        System.out.println("\t\t\tSuccessfully downloaded the blob.");
    }


    private static CloudBlobContainer createContainer(CloudBlobClient blobClient, String containerName) throws StorageException, RuntimeException, IOException, InvalidKeyException, IllegalArgumentException, URISyntaxException, IllegalStateException {

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
