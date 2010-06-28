package org.example;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import nl.wijsmullerbros.gs.StreamProxy;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.FileCopyUtils;

/**
 * @author bwijsmuller
 *
 */
public class CommandLine {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        System.out.println("NIC_ADDR: "+System.getenv("NIC_ADDR"));
        
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Choose sending client, reading client or server (c/r/s): ");
        String choice = console.readLine();
        CommandLine commandLine = new CommandLine();
        if ("c".equals(choice)) {
            commandLine.startWritingClient(console);
        } else if("s".equals(choice)) {
            commandLine.startServer(console);
        } else if ("r".equals(choice)) {
            commandLine.startReadingClient(console);
        }
    }

    private void startServer(BufferedReader console) throws IOException {
        ClassPathXmlApplicationContext contextA = new ClassPathXmlApplicationContext("classpath:context-a.xml");
        contextA.registerShutdownHook();
        System.out.println("started server. press any key to quit.");
        console.readLine();
    }

    private void startWritingClient(BufferedReader console) throws IOException {
        ClassPathXmlApplicationContext contextB = new ClassPathXmlApplicationContext("classpath:context-b.xml");
        contextB.registerShutdownHook();
        
        RemoteService service = contextB.getBean(RemoteService.class);
        StreamProxy streamProxy = service.createOutputStreamProxy();
        OutputStream outputStream = streamProxy.createRemotingOutputStream();
        
        System.out.println("Type file name to send: ");
        String fileName = console.readLine();
        File file = new File(fileName);
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        FileCopyUtils.copy(inputStream, outputStream);
        
        System.out.println("sent file.");
    }
    
    private void startReadingClient(BufferedReader console) throws IOException {
        ClassPathXmlApplicationContext contextB = new ClassPathXmlApplicationContext("classpath:context-b.xml");
        contextB.registerShutdownHook();
        
        System.out.println("Type file name to read (on server): ");
        String fileName = console.readLine();
        
        RemoteService service = contextB.getBean(RemoteService.class);
        StreamProxy streamProxy = service.createInputStreamProxy(fileName);
        InputStream inputStream = streamProxy.createRemotingInputStream();
        
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream("/tmp/streamOuputTest.dat");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        FileCopyUtils.copy(inputStream, fileOutputStream);
        
        System.out.println("Read file.");
    }

}
