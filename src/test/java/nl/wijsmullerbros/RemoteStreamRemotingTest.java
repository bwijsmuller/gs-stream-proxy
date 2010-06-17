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

import org.example.RemoteService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.notify.NotifyEventContainerServiceDetails;
import org.openspaces.events.polling.PollingEventContainerServiceDetails;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileCopyUtils;

/**
 * @author bwijsmuller
 *
 */
public class RemoteStreamRemotingTest {

    @Before
    public void before() {
        System.setProperty("REMOTE_HOST", "localhost");
    }
    
    @After
    public void after() {
        System.clearProperty("REMOTE_HOST");
    }
    
    @Test
    public void testStreaming() throws Exception {
        //TODO: make the result location platform independent
        FileSystemResource testFile = new FileSystemResource("/tmp/streamOuputTest.dat");
        if (testFile.exists()) {
            testFile.getFile().delete();
        }
        
        ClassPathXmlApplicationContext contextA = new ClassPathXmlApplicationContext("classpath:context-a.xml");
        contextA.registerShutdownHook();
        
        GigaSpace space = (GigaSpace) contextA.getBean("testGigaSpace");
        
        Resource inputFile = new ClassPathResource("test-text.txt");
        assertTrue(inputFile.exists());
        InputStream inputStream = new BufferedInputStream(inputFile.getInputStream());
        ByteArrayOutputStream checkStream = new ByteArrayOutputStream();
        FileCopyUtils.copy(inputStream, checkStream);
        System.out.println("CheckStream contains ["+checkStream.toByteArray().length+"] bytes.");
        
        
        ClassPathXmlApplicationContext contextB = new ClassPathXmlApplicationContext("classpath:context-b.xml");
        contextB.registerShutdownHook();
        
        RemoteService service = contextB.getBean(RemoteService.class);
        StreamProxy streamProxy = service.createStreamProxy();
        RemotingOutputStream outputStream = streamProxy.createRemotingOutputStream();
        
        inputStream = new BufferedInputStream(inputFile.getInputStream());
        FileCopyUtils.copy(inputStream, outputStream);
        for (int i=0; i<5; i++) {
            int count = space.count(new ChunkHolder());
            System.out.println("Found ["+count+"] items, sleeping 1 sec ...");
            Thread.sleep(1000);
        }
        
        FileSystemResource fileSystemResource = new FileSystemResource("/tmp/streamOuputTest.dat");
        assertTrue(fileSystemResource.exists());
        
        checkListenersClosed();
    }

    /**
     * 
     */
    private void checkListenersClosed() {
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
        Map<String, NotifyEventContainerServiceDetails> details = processingUnitInstances[0].getNotifyEventContainerDetails();
        if (details.size() == 0) {
            System.out.println("no details left");
            return;
        }
        Set<String> keySet = details.keySet();
        for (String key : keySet) {
            NotifyEventContainerServiceDetails detail = details.get(key);
            System.out.println("Polling container: "+detail.getDescription());
        }
        assertEquals(0, keySet.size());
    }

}
