package memoir44;

import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;

public class M44CommandFactory extends BasicCommandEncoder {
	public Decorator createDecorator(String type, GamePiece inner) {
		Decorator piece = null;
		
		if (type == null || type.length() < 3)
		{
			VassalTools.WriteLine("Error: CommandEncoder invalid type: " + inner.getType());
		}
		
		if (type.startsWith(M44CardRestriction.ID)) {
			piece = new M44CardRestriction(type,inner);
		}
		else {
			piece = super.createDecorator(type,inner);
		}
		return piece;
	}
}
