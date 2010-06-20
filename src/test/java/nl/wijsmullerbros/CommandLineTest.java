package nl.wijsmullerbros;

import java.io.IOException;

import org.example.CommandLine;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author bwijsmuller
 *
 */
public class CommandLineTest {

    @Test
    @Ignore
    public void test() throws IOException {
        System.setProperty("REMOTE_HOST", "localhost");
        
        CommandLine commandLine = new CommandLine();
        commandLine.main(new String[]{""});
    }
    
}
