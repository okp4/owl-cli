package de.atextor.owlcli;

import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static de.atextor.owlcli.MainClassRunner.run;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DiagramCommandTest {
    private byte[] fileContent( final File file ) {
        try {
            return FileUtils.readFileToByteArray( file );
        } catch ( final IOException exception ) {
            fail( "", exception );
        }
        return null;
    }

    @Test
    public void testWithoutParameters() {
        final Runnable command = () -> App.main( new String[]{ "diagram" } );
        final MainClassRunner.ExecutionResult result = run( command );

        assertThat( result.getExitStatus() ).isEqualTo( 1 );
        assertThat( result.getStdOut() ).isEmpty();
        assertThat( result.getStdErr() ).contains( "Error: " );
    }

    @Test
    public void testWithHelp() {
        final Runnable command = () -> App.main( new String[]{ "diagram", "--help" } );
        final MainClassRunner.ExecutionResult result = run( command );

        assertThat( result.getExitStatus() ).isEqualTo( 0 );
        assertThat( result.getStdOut() ).isNotEmpty();
        assertThat( result.getStdErr() ).isEmpty();
    }

    @Test
    public void testWithInvalidInput() {
        final Runnable command = () -> App.main( new String[]{ "diagram", "definitelynotexistingfile" } );
        final MainClassRunner.ExecutionResult result = run( command );

        assertThat( result.getExitStatus() ).isEqualTo( 1 );
        assertThat( result.getStdOut() ).isEmpty();
        assertThat( result.getStdErr() ).contains( "Error: " );
    }

    protected void testDiagramGeneration( final String testFileName ) throws IOException {
        final File tempDir = Files.newTemporaryFolder();
        assertThat( tempDir ).isEmptyDirectory();

        try {
            final URL input = DiagramCommandTest.class.getResource( "/" + testFileName + ".ttl" );
            final File output = tempDir.toPath().resolve( testFileName + ".ttl" ).toFile();
            FileUtils.copyURLToFile( input, output );
            assertThat( output ).isFile();
            assertThat( fileContent( output ) ).isNotEmpty();

            final Runnable command = () -> App.main( new String[]{ "diagram", output.getAbsolutePath() } );
            final MainClassRunner.ExecutionResult result = run( command );

            System.out.println( result.getStdOut() );
            System.out.println( result.getStdErr() );

            assertThat( result.getExitStatus() ).isEqualTo( 0 );
            assertThat( result.getStdOut() ).isEmpty();
            assertThat( result.getStdErr() ).isEmpty();

            final Path workingDirectory = tempDir.toPath();
            final Path resourceDirectory = workingDirectory.resolve( "static" );
            assertThat( resourceDirectory.toFile().isDirectory() );

            final Path sentinelResource = resourceDirectory.resolve( "owl-class.svg" );
            assertThat( sentinelResource.toFile() ).exists();

            final File writtenFile = workingDirectory.resolve( testFileName + ".svg" ).toFile();
            assertThat( writtenFile ).isFile();
            assertThat( fileContent( writtenFile ) ).contains( "<svg".getBytes() );
        } finally {
            FileUtils.deleteDirectory( tempDir );
        }
    }

    @Test
    public void testClassAssertion() throws IOException {
        testDiagramGeneration( "test-class-assertion" );
    }
}
