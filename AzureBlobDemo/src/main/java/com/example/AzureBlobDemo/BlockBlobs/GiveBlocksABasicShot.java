package com.example.AzureBlobDemo.BlockBlobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Base64;

import com.example.AzureBlobDemo.BlobClientProvider;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlockEntry;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.CopyStatus;
import com.microsoft.azure.storage.blob.DeleteSnapshotsOption;


// C:\Users\bentley\Desktop\Avantgarde\Tracciati e esempi Data Factory SAC FIRECOP\File import per data factory
// \AlberoProccessi righe 20190611.xlsx


public class GiveBlocksABasicShot {

    public static void runSamples() throws Throwable {

        // BlobContainerClient: The BlobContainerClient class allows
        // you to manipulate Azure Storage containers and their blobs.

        // CloudBlobContainer: Represents a container in
        // the Microsoft Azure Blob service.

        // CloudBlobClient: Provides a client-side logical representation
        // of Microsoft Azure Blob storage.

        CloudBlobClient blobClient;
        CloudBlobContainer container1 = null;
        CloudBlobContainer container2 = null;

        // Create a blob client for interacting with the blob service
        blobClient = BlobClientProvider.getBlobClientReference();

        // Create new containers with randomized names
        System.out.println("\nCreate container for the sample demonstration");
        container1 = createContainer(blobClient, "yabbadabba");
        container2 = createContainer(blobClient, "yabbadabba2");

        if( !container1.exists() && !container2.exists()){
            System.out.println("THE END");
            System.exit(0);
        }

        String filePath = "C:\\Users\\bentley\\Desktop\\Avantgarde\\Tracciati e esempi Data Factory SAC FIRECOP\\File import per data factory\\AlberoProccessi righe 20190611.xlsx";
        File file = new File(String.valueOf(new FileInputStream(filePath)));
        System.out.println(file.getName());
        System.out.println(file.getAbsolutePath());
        System.out.println(filePath);
        // FileInputStream input = new FileInputStream(filePath);



        System.out.println("\n\tUpload a sample file as a block blob.");
        CloudBlockBlob blockBlob1 = container1.getBlockBlobReference("Avantgarde Trial1");
        blockBlob1.uploadFromFile(filePath);
        System.out.println("\t\tSuccessfully uploaded the blob.");

        // Create a read-only snapshot of the blob
        System.out.println("\n\tCreate a read-only snapshot of the blob.");
        CloudBlob blockBlob1Snapshot = blockBlob1.createSnapshot();
        System.out.println("\t\tSuccessfully created a snapshot of the blob.");




        // Upload a sample file as a block blob using a block list
        System.out.println("\n\tUpload the third sample file as a block blob using a block list.");
        CloudBlockBlob blockBlob2 = container2.getBlockBlobReference("Avantgarde Trial2");
        uploadFileBlocksAsBlockBlob(blockBlob2, filePath);
        System.out.println("\t\tSuccessfully uploaded the blob using a block list.");



        // Download the blobs and its snapshot
        System.out.println("\n\tDownload the blobs and its snapshots.");

        String downloadedBlobPath = String.format("%ssnapshotof-%s", System.getProperty("java.io.tmpdir"), blockBlob1Snapshot.getName());
        System.out.println(String.format("\t\tDownload the blob snapshot from \"%s\" to \"%s\".", blockBlob1Snapshot.getUri().toURL(), downloadedBlobPath));
        blockBlob1Snapshot.downloadToFile(downloadedBlobPath);
        new File(downloadedBlobPath).deleteOnExit();
        System.out.println("\t\t\tSuccessfully downloaded the blob snapshot.");

        downloadedBlobPath = String.format("%scopyof-%s", System.getProperty("java.io.tmpdir"), blockBlob2.getName());
        System.out.println(String.format("\t\tDownload the blob from \"%s\" to \"%s\".", blockBlob2.getUri().toURL(), downloadedBlobPath));
        blockBlob2.downloadToFile(downloadedBlobPath);
        new File(downloadedBlobPath).deleteOnExit();
        System.out.println("\t\t\tSuccessfully downloaded the blob.");




        // Download the block list for the block blob
        System.out.println("\n\tDownload the block list.");
        for (BlockEntry blockEntry : blockBlob2.downloadBlockList()) {
            System.out.println(String.format("\t\tBlock id: %s (%s), size: %s", new String(Base64.getDecoder().decode(blockEntry.getId())), blockEntry.getId(), blockEntry.getSize()));
        }




        // Copy the blob
        System.out.println(String.format("\n\tCopying blob \"%s\".", blockBlob1.getUri().toURL()));
        CloudBlockBlob blockBlob1Copy = container1.getBlockBlobReference(blockBlob1.getName() + ".copy");
        blockBlob1Copy.startCopy(blockBlob1);
        waitForCopyToComplete(blockBlob1Copy);
        System.out.println("\t\tSuccessfully copied the blob.");




        // Delete a blob and its snapshots
        System.out.println(String.format("\n\tDelete the blob \"%s\" and its snapshots.", blockBlob1.getName()));
        blockBlob1.delete(DeleteSnapshotsOption.INCLUDE_SNAPSHOTS, null, null, null);
        System.out.println("\t\tSuccessfully deleted the blob and its snapshots.");

    }


    private static CloudBlobContainer createContainer(CloudBlobClient blobClient, String containerName) throws StorageException, RuntimeException, IOException, InvalidKeyException, IllegalArgumentException, URISyntaxException, IllegalStateException {

        // "getContainerReference" returns an address, it
        // does not create the container by itself
        CloudBlobContainer container = blobClient.getContainerReference(containerName);

        try {
            if(container.exists()){
                System.out.println("Container " + containerName + " already exists, killing it with fire.");
                container.deleteIfExists();
                System.out.println(String.format("\tSuccessfully destroyed the container \"%s\".", container.getName()));
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


    private static void uploadFileBlocksAsBlockBlob(CloudBlockBlob blockBlob, String filePath) throws Throwable {

        FileInputStream fileInputStream = null;
        try {
            // Open the file
            fileInputStream = new FileInputStream(filePath);

            // Split the file into 32K blocks (block size deliberately kept small for the demo) and upload all the blocks
            int blockNum = 0;
            String blockId = null;
            String blockIdEncoded = null;
            ArrayList<BlockEntry> blockList = new ArrayList<BlockEntry>();
            while (fileInputStream.available() > (32 * 1024)) {
                blockId = String.format("%05d", blockNum);
                blockIdEncoded = Base64.getEncoder().encodeToString(blockId.getBytes());
                blockBlob.uploadBlock(blockIdEncoded, fileInputStream, (32 * 1024));
                blockList.add(new BlockEntry(blockIdEncoded));
                blockNum++;
            }
            blockId = String.format("%05d", blockNum);
            blockIdEncoded = Base64.getEncoder().encodeToString(blockId.getBytes());
            blockBlob.uploadBlock(blockIdEncoded, fileInputStream, fileInputStream.available());
            blockList.add(new BlockEntry(blockIdEncoded));

            // Commit the blocks
            blockBlob.commitBlockList(blockList);
        }
        catch (Throwable t) {
            throw t;
        }
        finally {
            // Close the file output stream writer
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }


    private static void waitForCopyToComplete(CloudBlob blob) throws InterruptedException, StorageException {
        CopyStatus copyStatus = CopyStatus.PENDING;
        while (copyStatus == CopyStatus.PENDING) {
            Thread.sleep(1000);
            blob.downloadAttributes();
            copyStatus = blob.getCopyState().getStatus();
        }
    }

}
