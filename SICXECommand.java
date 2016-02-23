import java.util.Arrays;

/*
 * A class to hold the Instructions the destinations the NIXBPE values it also holds all the methods for converting items to
 * PC or Base or extended all the modes. Used to hold everything for use in the second pass.
 */
public class SICXECommand {
	String instruction;
	String reg;
	String dest;
	int pc;
	Boolean ni[] = { true, true };
	Boolean xbpe[] = { false, false, true, false };
	int line;

	public SICXECommand(String instruction, String reg, String dest, int pc, Boolean n, Boolean i, Boolean x, Boolean b,
			Boolean p, Boolean e, int line) {
		super();
		this.instruction = instruction;
		this.reg = reg;
		this.dest = dest;
		this.pc = pc;
		this.ni[0] = n;
		this.ni[1] = i;
		this.xbpe[0] = x;
		this.xbpe[1] = b;
		this.xbpe[2] = p;
		this.xbpe[3] = e;
		this.line = line;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public String getReg() {
		return reg;
	}

	public void setReg(String reg) {
		this.reg = reg;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public int getPc() {
		return pc;
	}

	public void setPc(int pc) {
		this.pc = pc;
	}

	public Boolean[] getNi() {
		return ni;
	}

	public void setNi(Boolean[] ni) {
		this.ni = ni;
	}

	public Boolean[] getXbpe() {
		return xbpe;
	}

	public void setXbpe(Boolean[] xbpe) {
		this.xbpe = xbpe;
	}

	public Boolean isImmediate() {
		if (ni[0] == false && ni[1] == true)
			return true;

		return false;
	}

	public Boolean isExtended() {
		return this.xbpe[3];
	}

	public void setToIndirect() {
		this.ni[0] = true;
		this.ni[1] = false;
	}

	public void setToBase() {
		this.xbpe[1] = true;
		this.xbpe[2] = false;
	}

	public void setToIndex() {
		this.xbpe[0] = true;
	}

	public void setToPc() {
		this.xbpe[2] = true;
	}

	public int getNIvalue() {
		if (this.ni[0] == true && this.ni[1] == true) {
			return 0x3;
		} else if (this.ni[0] == true && this.ni[1] == false) {
			return 0x2;
		} else {
			return 0x1;
		}
	}

	public int getXBPEvalue() {

		// check and deal with indexed first
		if (this.xbpe[0] == true) {
			if (this.xbpe[1] == true) {
				return 0xC;
			} else if (this.xbpe[2] == true) {
				return 0xA;
			} else if (this.xbpe[3] == true) {
				return 0x9;
			} else {
				return 0x0;
			}
		} else {
			if (this.xbpe[1] == true) {
				return 0x4;
			} else if (this.xbpe[2] == true) {
				return 0x2;
			} else if (this.xbpe[3] == true) {
				return 0x1;
			} else {
				return 0x0;
			}
		}

	}

	@Override
	public String toString() {
		return "SICXECommand [instruction=" + instruction + ", reg=" + reg + ", dest=" + dest + ", pc=" + pc + ", ni="
				+ Arrays.toString(ni) + ", xbpe=" + Arrays.toString(xbpe) + ", line=" + line + "]";
	}

}
