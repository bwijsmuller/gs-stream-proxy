/*
 * Copyright (c) Qiy Intellectual Property B.V. and licensors, 2007-2010. All rights reserved.
 */
package nl.wijsmullerbros;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import nl.wijsmullerbros.gs.ChunkHolder;
import nl.wijsmullerbros.gs.StreamProxy;
import nl.wijsmullerbros.gs.outputstream.RemotingOutputStream;

import org.example.RemoteService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.polling.PollingEventContainerServiceDetails;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileCopyUtils;

/**
 * @author bwijsmuller
 *
 */
public class StreamRemotingTest {

    @Test
    public void testStreaming() throws Exception {
        FileSystemResource testFile = new FileSystemResource("/tmp/streamOuputTest.dat");
        if (testFile.exists()) {
            testFile.getFile().delete();
        }
        
        ClassPathXmlApplicationContext contextA = new ClassPathXmlApplicationContext("classpath:context-a.xml");
        contextA.registerShutdownHook();
        
        RemoteService service = contextA.getBean(RemoteService.class);
        StreamProxy streamProxy = service.createOutputStreamProxy();
        RemotingOutputStream outputStream = streamProxy.createRemotingOutputStream();

        GigaSpace space = (GigaSpace) contextA.getBean("testGigaSpace");
        
        FileSystemResource inputFile = new FileSystemResource("/home/bwijsmuller/Documents/Braam Wijsmuller-ScrumAlliance_CSM_Certificate.pdf");
        assertTrue(inputFile.exists());
        InputStream inputStream = new BufferedInputStream(inputFile.getInputStream());
        ByteArrayOutputStream checkStream = new ByteArrayOutputStream();
        FileCopyUtils.copy(inputStream, checkStream);
        System.out.println("CheckStream contains ["+checkStream.toByteArray().length+"] bytes.");
        
        inputStream = new BufferedInputStream(inputFile.getInputStream());
        FileCopyUtils.copy(inputStream, outputStream);
        for (int i=0; i<5; i++) {
            int count = space.count(new ChunkHolder());
            System.out.println("Found ["+count+"] items, sleeping 1 sec ...");
            Thread.sleep(1000);
        }
        
        FileSystemResource fileSystemResource = new FileSystemResource("/tmp/streamOuputTest.dat");
        assertTrue(fileSystemResource.exists());
        
        checkPollingContainersClosed();
    }

    /**
     * 
     */
    private void checkPollingContainersClosed() {
        Admin admin = new AdminFactory().addGroup("testGroup").createAdmin();
        GridServiceContainer[] containers = admin.getGridServiceContainers().getContainers();
        if (containers.length == 0) {
            System.out.println("no containers left");
            return;
        }
        ProcessingUnitInstance[] processingUnitInstances = containers[0].getProcessingUnitInstances();
        if (processingUnitInstances.length == 0) {
            System.out.println("no processingUnitInstances left");
            return;
        }
        Map<String, PollingEventContainerServiceDetails> details = processingUnitInstances[0].getPollingEventContainerDetails();
        if (details.size() == 0) {
            System.out.println("no details left");
            return;
        }
        Set<String> keySet = details.keySet();
        for (String key : keySet) {
            PollingEventContainerServiceDetails pollingDetails = details.get(key);
            System.out.println("Polling container: "+pollingDetails.getDescription());
        }
        assertEquals(0, keySet.size());
    }

}
