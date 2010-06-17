package org.example;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import nl.wijsmullerbros.StreamProxy;
import nl.wijsmullerbros.StreamProxyFactory;

import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bwijsmuller
 *
 */
@Service
public class DefaultRemoteService implements RemoteService {

    @Autowired
    GigaSpace gigaSpace;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public StreamProxy createStreamProxy() {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream("/tmp/streamOuputTest.dat");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        StreamProxy streamProxy = new StreamProxyFactory(fileOutputStream).createRegisteredProxy();
        //gigaSpace);
        return streamProxy;
    }

}
