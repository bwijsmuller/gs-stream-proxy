package org.example;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import nl.wijsmullerbros.RemotingOutputStream;
import nl.wijsmullerbros.StreamProxy;

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
        System.out.println("Choose client or server (c/s): ");
        String choice = console.readLine();
        CommandLine commandLine = new CommandLine();
        if ("c".equals(choice)) {
            commandLine.startClient(console);
        } else {
            commandLine.startServer(console);
        }
    }

    private void startServer(BufferedReader console) throws IOException {
        ClassPathXmlApplicationContext contextA = new ClassPathXmlApplicationContext("classpath:context-a.xml");
        contextA.registerShutdownHook();
        System.out.println("started server. press any key to quit.");
        console.readLine();
    }

    private void startClient(BufferedReader console) throws IOException {
        ClassPathXmlApplicationContext contextB = new ClassPathXmlApplicationContext("classpath:context-b.xml");
        contextB.registerShutdownHook();
        
        RemoteService service = contextB.getBean(RemoteService.class);
        StreamProxy streamProxy = service.createStreamProxy();
        RemotingOutputStream outputStream = streamProxy.createRemotingOutputStream();
        
        System.out.println("Type file name to send: ");
        String fileName = console.readLine();
        File file = new File(fileName);
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        FileCopyUtils.copy(inputStream, outputStream);
        
        System.out.println("sent file.");
    }

}
