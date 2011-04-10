package org.example;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import nl.wijsmullerbros.gs.StreamProxy;
import nl.wijsmullerbros.gs.StreamProxyFactory;

import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This is an example service that offers a remote stream for clients.
 * @author bwijsmuller
 */
@Service
public class DefaultRemoteService implements RemoteService {

    @Autowired
    GigaSpace gigaSpace;

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamProxy createOutputStreamProxy() {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream("/tmp/streamOuputTest.dat");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        StreamProxy streamProxy = new StreamProxyFactory().createRegisteredProxy(fileOutputStream);
        return streamProxy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamProxy createInputStreamProxy(String filePath) {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        StreamProxy streamProxy = new StreamProxyFactory().createRegisteredProxy(fileInputStream);
        return streamProxy;
    }

}
