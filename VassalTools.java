package memoir44;

public class VassalTools {
	
	public static boolean HasAuditableClass()
	{
		try {
			Class.forName("VASSAL.script.expression.Auditable");
		} catch (ClassNotFoundException e) {
			return false;
		}
		
		return true;
	}
			
	public static void WriteLine(String msgLine) {
		if (msgLine == null)
		{
			msgLine = "_null_";
		}
		
		if (VassalTools.HasAuditableClass()) {
			// VASSAL 3.6+
			VassalTools36.WriteLine(msgLine);
		} else {
			// VASSAL 3.5-
			VassalTools35.WriteLine(msgLine);
		}
	}
	
	public static void WriteLine(String msgLine, boolean logMsg) {
		if (msgLine == null)
		{
			msgLine = "_null_";
		}
		
		if (!logMsg)
		{
			WriteLine(msgLine);
			return;
		}
		
		if (VassalTools.HasAuditableClass()) {
			// VASSAL 3.6+
			VassalTools36.SendLine(msgLine);
		} else {
			// VASSAL 3.5-
			VassalTools35.SendLine(msgLine);
		}
	}
	
}
