package com.vlee.util;

/*-------------------------------------------------
 * 1 - event and high-level information messages
 * 2 - normal debug messages that help track the flow of 
 command processing
 * 3 - higher level of debug used for inner loops, table 
 traversals, etc.
 * 4 - I/O debug - mover message traces
 * 5 - trace-level debug
 ----------------------------------------------------*/
public final class Log
{
	public static final boolean debuggingOn = true;
	public static final boolean auditTrailOn = true;
	public static final boolean verboseOn = true;
	// public static final boolean verboseOn = false;
	public static final int debugLevel = 5;

	public static void debug(int dbgLvl, String msg)
	{
		if (dbgLvl <= debugLevel)
		{
			System.out.println("Debug[" + dbgLvl + "]: " + msg);
		}
	}

	public static void printDebug(String msg)
	{
		if (debuggingOn)
		{
			System.err.println("Debug: " + msg);
		}
	}

	public static void printAudit(String msg)
	{
		if (auditTrailOn)
		{
			System.out.println("Audit : " + msg);
		}
	}

	public static void printVerbose(String msg)
	{
		if (verboseOn)
		{
			System.out.println("Verbose : " + msg);
		}
	}
} // Log
package com.vlee.util;

/*-------------------------------------------------
 * 1 - event and high-level information messages
 * 2 - normal debug messages that help track the flow of 
 command processing
 * 3 - higher level of debug used for inner loops, table 
 traversals, etc.
 * 4 - I/O debug - mover message traces
 * 5 - trace-level debug
 ----------------------------------------------------*/
public final class Log
{
	public static final boolean debuggingOn = true;
	public static final boolean auditTrailOn = true;
	public static final boolean verboseOn = true;
	// public static final boolean verboseOn = false;
	public static final int debugLevel = 5;

	public static void debug(int dbgLvl, String msg)
	{
		if (dbgLvl <= debugLevel)
		{
			System.out.println("Debug[" + dbgLvl + "]: " + msg);
		}
	}

	public static void printDebug(String msg)
	{
		if (debuggingOn)
		{
			System.err.println("Debug: " + msg);
		}
	}

	public static void printAudit(String msg)
	{
		if (auditTrailOn)
		{
			System.out.println("Audit : " + msg);
		}
	}

	public static void printVerbose(String msg)
	{
		if (verboseOn)
		{
			System.out.println("Verbose : " + msg);
		}
	}
} // Log
package com.vlee.util;

/*-------------------------------------------------
 * 1 - event and high-level information messages
 * 2 - normal debug messages that help track the flow of 
 command processing
 * 3 - higher level of debug used for inner loops, table 
 traversals, etc.
 * 4 - I/O debug - mover message traces
 * 5 - trace-level debug
 ----------------------------------------------------*/
public final class Log
{
	public static final boolean debuggingOn = true;
	public static final boolean auditTrailOn = true;
	public static final boolean verboseOn = true;
	// public static final boolean verboseOn = false;
	public static final int debugLevel = 5;

	public static void debug(int dbgLvl, String msg)
	{
		if (dbgLvl <= debugLevel)
		{
			System.out.println("Debug[" + dbgLvl + "]: " + msg);
		}
	}

	public static void printDebug(String msg)
	{
		if (debuggingOn)
		{
			System.err.println("Debug: " + msg);
		}
	}

	public static void printAudit(String msg)
	{
		if (auditTrailOn)
		{
			System.out.println("Audit : " + msg);
		}
	}

	public static void printVerbose(String msg)
	{
		if (verboseOn)
		{
			System.out.println("Verbose : " + msg);
		}
	}
} // Log
package com.vlee.util;

/*-------------------------------------------------
 * 1 - event and high-level information messages
 * 2 - normal debug messages that help track the flow of 
 command processing
 * 3 - higher level of debug used for inner loops, table 
 traversals, etc.
 * 4 - I/O debug - mover message traces
 * 5 - trace-level debug
 ----------------------------------------------------*/
public final class Log
{
	public static final boolean debuggingOn = true;
	public static final boolean auditTrailOn = true;
	public static final boolean verboseOn = true;
	// public static final boolean verboseOn = false;
	public static final int debugLevel = 5;

	public static void debug(int dbgLvl, String msg)
	{
		if (dbgLvl <= debugLevel)
		{
			System.out.println("Debug[" + dbgLvl + "]: " + msg);
		}
	}

	public static void printDebug(String msg)
	{
		if (debuggingOn)
		{
			System.err.println("Debug: " + msg);
		}
	}

	public static void printAudit(String msg)
	{
		if (auditTrailOn)
		{
			System.out.println("Audit : " + msg);
		}
	}

	public static void printVerbose(String msg)
	{
		if (verboseOn)
		{
			System.out.println("Verbose : " + msg);
		}
	}
} // Log
