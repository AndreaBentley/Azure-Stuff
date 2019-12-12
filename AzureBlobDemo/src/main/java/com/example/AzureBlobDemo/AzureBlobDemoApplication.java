package com.example.AzureBlobDemo;

import com.example.AzureBlobDemo.AppendBlobs.GiveAppendsAShot;
import com.example.AzureBlobDemo.BlockBlobs.GiveBlocksABasicShot;
import com.example.AzureBlobDemo.BlockBlobs.GiveBlocksAnAdvancedShot;
import com.example.AzureBlobDemo.PageBlobs.GivePagesAShot;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AzureBlobDemoApplication {

	public static void main(String[] args) throws Throwable {
		//SpringApplication.run(AzureBlobDemoApplication.class, args);
		GiveBlocksABasicShot shot1 = new GiveBlocksABasicShot();
		GiveBlocksAnAdvancedShot shot2 = new GiveBlocksAnAdvancedShot();
		shot1.runSamples();
		shot2.runSamples();

		GiveAppendsAShot shot3 = new GiveAppendsAShot();
		shot3.runSamples();

		GivePagesAShot shot4 = new GivePagesAShot();
		shot4.runSamples();
	}

}
