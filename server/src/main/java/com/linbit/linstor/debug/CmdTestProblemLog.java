package com.linbit.linstor.debug;

import com.linbit.linstor.LinStorException;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.security.AccessContext;

import javax.inject.Inject;

import java.io.PrintStream;
import java.util.Map;

import org.slf4j.event.Level;

/**
 * Throws a test exception to test exception handling and reporting
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public class CmdTestProblemLog extends BaseDebugCmd
{
    private final ErrorReporter errorReporter;

    @Inject
    public CmdTestProblemLog(ErrorReporter errorReporterRef)
    {
        super(
            new String[]
            {
                "TstProbLog"
            },
            "Test Problem log",
            "Throws an exception for test purposes",
            null,
            null
        );
        errorReporter = errorReporterRef;
    }

    @Override
    public void execute(
        PrintStream debugOut,
        PrintStream debugErr,
        AccessContext accCtx,
        Map<String, String> parameters
    ) throws Exception
    {
        debugOut.println("Throwing exception for error logging test");
        errorReporter.reportProblem(Level.ERROR, new LinStorException("test"), accCtx, null, "random context");
    }
}


