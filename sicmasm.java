import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class sicmasm {

	public static void main(String[] args) throws IOException {
		String base = null; //holds the variable that the Base location is set to.
		String title = null; //holds the title of the program
		String fileName; //name of the program
		HashMap<String, Integer> directives = new HashMap<String, Integer>();
		HashMap<String, Integer> symbolTable = new HashMap<String, Integer>();
		HashMap<String, Integer> instructionsSize = new HashMap<String, Integer>(); //holds all the Instructions and the size needed
		HashMap<String, Integer> registers = new HashMap<String, Integer>();
		HashMap<Integer, Integer> memoryLocsline = new HashMap<Integer, Integer>();//Holds the memory address of each line
		HashMap<Integer, String> opCodesLines = new HashMap<Integer, String>();//store the final OP code by line
		HashMap<String, Integer> opCodes = new HashMap<String, Integer>(); //holds all the instructions and their Hex op code
		ArrayList<String[]> lines = new ArrayList<String[]>();
		ArrayList<SICXECommand> table = new ArrayList<SICXECommand>();//Holds all the SICXECommands for further processing
		ArrayList<String> linesPrinting = new ArrayList<String>();//unbroken lines of the assembly code

		// Registers
		registers.put("A", 0);
		registers.put("X", 1);
		registers.put("B", 3);
		registers.put("L", 2);
		registers.put("S", 4);
		registers.put("T", 5);

		// add in all the used directives
		directives.put("START", null);
		directives.put("BYTE", 0x1);
		directives.put("WORD", 0x3);
		directives.put("RESW", 0x3);
		directives.put("RESB", 0x1);
		directives.put("END", null);
		directives.put("BASE", null);
		directives.put("NOBASE", null);

		// Add in the instructions and their sizes for use in pass 1
		instructionsSize.put("ADDR", 0x2);
		instructionsSize.put("COMPR", 0x2);
		instructionsSize.put("SUBR", 0x2);
		instructionsSize.put("ADD", 0x3);
		instructionsSize.put("SUB", 0x3);
		instructionsSize.put("MUL", 0x3);
		instructionsSize.put("DIV", 0x3);
		instructionsSize.put("COMP", 0x3);
		instructionsSize.put("J", 0x3);
		instructionsSize.put("JEQ", 0x3);
		instructionsSize.put("JGT", 0x3);
		instructionsSize.put("JLT", 0x3);
		instructionsSize.put("JSUB", 0x3);
		instructionsSize.put("LDCH", 0x3);
		instructionsSize.put("RSUB", 0X3);
		instructionsSize.put("TIX", 0x3);
		instructionsSize.put("TIXR", 0x2);
		instructionsSize.put("RD", 0x3);
		instructionsSize.put("TD", 0x3);
		instructionsSize.put("WD", 0x3);
		instructionsSize.put("STCH", 0x3);
		instructionsSize.put("CLEAR", 0x2);
		instructionsSize.put("MOV", 0x3);

		// Add in all the opcodes for instructions handled by program
		opCodes.put("ADDR", 0x90);
		opCodes.put("COMPR", 0xA0);
		opCodes.put("SUBR", 0x94);
		opCodes.put("ADD", 0x18);
		opCodes.put("SUB", 0x1C);
		opCodes.put("MUL", 0x20);
		opCodes.put("DIV", 0x24);
		opCodes.put("COMP", 0x28);
		opCodes.put("J", 0x3C);
		opCodes.put("JEQ", 0x30);
		opCodes.put("JGT", 0x34);
		opCodes.put("JLT", 0x38);
		opCodes.put("JSUB", 0x48);
		opCodes.put("LDCH", 0x50);
		opCodes.put("RSUB", 0X4C);
		opCodes.put("TIX", 0x2C);
		opCodes.put("TIXR", 0xB8);
		opCodes.put("RD", 0xD8);
		opCodes.put("TD", 0xE0);
		opCodes.put("WD", 0xDC);
		opCodes.put("STCH", 0x54);
		opCodes.put("CLEAR", 0xB4);
		opCodes.put("LDA", 0x00);
		opCodes.put("LDB", 0x68);
		opCodes.put("LDL", 0x08);
		opCodes.put("LDS", 0x6C);
		opCodes.put("LDT", 0x74);
		opCodes.put("LDX", 0x04);
		opCodes.put("STA", 0x0C);
		opCodes.put("STB", 0x78);
		opCodes.put("STL", 0x14);
		opCodes.put("STS", 0x7C);
		opCodes.put("STT", 0x84);
		opCodes.put("STX", 0x10);

		String fp;
		// Get the title of the file
		if (args.length > 0) {
			fp = args[0];
			String[] result = args[0].split("[/.]");
			fileName = result[result.length - 2]; // the file name should be the
													// second to last element in
													// the array
		} else {
			fileName = "main";
			fp = "main.asm";
		}

		// Read the asm file
		BufferedReader in = new BufferedReader(new FileReader(fp));
		// set up the lst file for writing
		PrintWriter lst = new PrintWriter(fileName + ".lst", "UTF-8");
		// set up the obj file for writing
		PrintWriter obj = new PrintWriter(new BufferedWriter(new FileWriter(fileName + ".obj")));

		// read in each line and set it up for pass 1

		//read each line and split it to get instructions on their own.
		String line;
		while ((line = in.readLine()) != null) {
			linesPrinting.add(line);
			String[] ar = line.replaceAll("^[,\\s]+", "").split("[,\\s]+");
			lines.add(ar);
		}
		in.close();

		
		/*============================================================================*
		 * =============================FIRST PASS====================================*
		 *============================================================================*/
		// first pass find all the memory locations and set up for pass 2
		int memoryLoc = 0x0;
		for (int i = 0; i < lines.size(); i++) {

			String[] p1ar = lines.get(i);

			// Store the memory locations by line for later
			memoryLocsline.put(i, memoryLoc);

			// looks for the start of the program it is always set up this way
			// so we are able to do it
			if (p1ar.length > 2 && p1ar[1].equals("START")) {
				title = p1ar[0];
				memoryLoc = Integer.parseInt(p1ar[2], 16);
				directives.put("START", memoryLoc);
				memoryLocsline.put(i, memoryLoc);
				continue;
			}
			
			//Cycle through checking each line for either instructions or directives if it finds an instruction it 
			//creates a SICXECommand obj and stores each one for the PASS2. IF it finds a directive it does what needs
			//to be done with each one. If it is a new variable it gets added to the symbol table.
			for (int j = 0; j < p1ar.length; j++) {
				// ignore once a comment is reached
				if (p1ar[j].startsWith(";")) {
					break;
					// Look for any variables or symbols
				} else if (!directives.containsKey(p1ar[j]) && !instructionsSize.containsKey(p1ar[j]) && j == 0
						&& !p1ar[j].startsWith("+")) {

					if (!symbolTable.containsKey(p1ar[j]))
						symbolTable.put(p1ar[j], memoryLoc);
					// symbolTable.putIfAbsent(p1ar[j], memoryLoc);
					// Looks for where the base is set
				} else if (p1ar[j].equals("BASE")) {
					base = p1ar[j + 1];

					// Dealing with reserve words and Bytes
				} else if (p1ar[j].equals("RESW") || p1ar[j].equals("RESB")) {
					memoryLoc += (directives.get(p1ar[j]) * Integer.parseInt(p1ar[j + 1]));
					// Deal with bytes
				} else if (p1ar[j].equals("BYTE")) {
					if (p1ar[j + 1].startsWith("C")) {
						String s2 = p1ar[j + 1].substring(2, p1ar[j + 1].length() - 1);
						s2 = converCharToHex(s2);
						memoryLoc += (directives.get(p1ar[j]) * (p1ar[j + 1].length() - 3));
						opCodesLines.put(i, s2);
					} else {
						String s2 = p1ar[j + 1].substring(2, p1ar[j + 1].length() - 1);

						memoryLoc += ((p1ar[j + 1].length() - 3) / 2);
						opCodesLines.put(i, s2);
					}
					// Deal with Words
				} else if (p1ar[j].equals("WORD")) {
					memoryLoc += directives.get(p1ar);
					// Deal with the end
				} else if (p1ar[j].equals("END")) {
					directives.put("END", memoryLoc);
					break;
					//Deal with all the instructions
				} else if (instructionsSize.containsKey(p1ar[j])) {
					memoryLoc += instructionsSize.get(p1ar[j]);
					
					//finds and processes the MOV function and if it is Immediate or not
					if (p1ar[j].equals("MOV") && (p1ar[j + 1].startsWith("#") || p1ar[j + 2].startsWith("#"))) {
						String ar1[] = convertmov(p1ar[j + 1], p1ar[j + 2]);
						SICXECommand com = new SICXECommand(ar1[0], null, ar1[1], memoryLoc, false, true, false, false,
								true, false, i);
						table.add(com);
					} else if (p1ar[j].equals("MOV")) {
						String ar1[] = convertmov(p1ar[j + 1], p1ar[j + 2]);
						SICXECommand com = new SICXECommand(ar1[0], null, ar1[1], memoryLoc, true, true, false, false,
								true, false, i);
						table.add(com);
						//Deals with all the Register Values that take two registers
					} else if (p1ar[j].equals("ADDR") || p1ar[j].equals("COMPR") || p1ar[j].equals("SUBR")) {
						SICXECommand com = new SICXECommand(p1ar[j], convertRegister(p1ar[j + 2]),
								convertRegister(p1ar[j + 1]), memoryLoc, false, false, false, false, false, false, i);
						table.add(com);
						//Deal with Single register instructions
					} else if (p1ar[j].equals("CLEAR") || p1ar[j].equals("TIXR")) {
						SICXECommand com = new SICXECommand(p1ar[j], convertRegister(p1ar[j + 1]), (String) null,
								memoryLoc, false, false, false, false, false, false, i);
						table.add(com);
						//Deal with the no argument instruction
					} else if (p1ar[j].equals("RSUB")) {
						SICXECommand com = new SICXECommand(p1ar[j], (String) null, (String) null, memoryLoc, true,
								true, false, false, false, false, i);
						table.add(com);
						//All other commands
					} else {
						if (p1ar[j + 1].startsWith("#")) {
							SICXECommand com = new SICXECommand(p1ar[j], (String) null, p1ar[j + 1], memoryLoc, false,
									true, false, false, false, false, i);
							table.add(com);
						} else {

							SICXECommand com = new SICXECommand(p1ar[j], (String) null, p1ar[j + 1], memoryLoc, true,
									true, false, false, true, false, i);
							table.add(com);
						}
					}

					break;
					// Looks for any extended values
				} else if (p1ar[j].startsWith("+")) {
					String s = p1ar[j].substring(1);
					memoryLoc += (instructionsSize.get(s) + 1);

					if (s.equals("MOV") && (p1ar[j + 1].startsWith("#") || p1ar[j + 2].startsWith("#"))) {
						String ar1[] = convertmov(p1ar[j + 1], p1ar[j + 2]);
						SICXECommand com = new SICXECommand(ar1[0], null, ar1[1], memoryLoc, false, true, false, false,
								false, true, i);
						table.add(com);
					} else if (s.equals("MOV")) {
						String ar1[] = convertmov(p1ar[j + 1], p1ar[j + 2]);
						SICXECommand com = new SICXECommand(ar1[0], null, ar1[1], memoryLoc, true, true, false, false,
								false, true, i);
						table.add(com);

					} else {

						SICXECommand com = new SICXECommand(s, null, p1ar[j + 1], memoryLoc, true, true, false, false,
								false, true, i);
						table.add(com);
					}

					break;
				}
			}

		}

		// setup the base for the second pass
		directives.remove("BASE");
		directives.put("BASE", symbolTable.get(base));

		/*
		 * =====================================================================
		 * ======================PASS 2========================================
		 * ===================================================================
		 */

		for (int i = 0; i < table.size(); i++) {

			SICXECommand command = table.get(i);
			String destValue = "";
			int op = 0;
			int memory = 0;

			// Do all the finding of commands that are immediate since they were
			// discoverable in the first pass
			if (command.isImmediate()) {

				destValue = command.getDest().substring(1, command.getDest().length());

				// If it is in the symbol table we have to use the PC or Base
				// Counter as well
				if (symbolTable.containsKey(destValue)) {
					command.setToPc();
					op = opCodes.get(command.getInstruction()) + command.getNIvalue();
					memory = symbolTable.get(destValue) - command.getPc();
					
					//If Check if it is base greater than 2047 or less than -2048
					if (memory > (Math.pow(2, 11) - 1) || memory < (Math.pow(2, 11) * -1)) {
						command.setToBase();
						memory = symbolTable.get(destValue) - directives.get("BASE");
						opCodesLines.put(command.getLine(), String.format("%02X", op)
								+ String.format("%01X", command.getXBPEvalue()) + String.format("%03X", memory));
					} else {
						opCodesLines.put(command.getLine(), String.format("%02X", op)
								+ String.format("%01X", command.getXBPEvalue()) + String.format("%03X", memory));
					}
					//now look for all extended values
				} else if (command.isExtended()) {
					op = opCodes.get(command.getInstruction()) + command.getNIvalue();
					memory = Integer.parseInt(destValue);
					opCodesLines.put(command.getLine(), String.format("%02X", op)
							+ String.format("%01X", command.getXBPEvalue()) + String.format("%05X", memory));
				} else {
					op = opCodes.get(command.getInstruction()) + command.getNIvalue();
					memory = Integer.parseInt(destValue);
					opCodesLines.put(command.getLine(), String.format("%02X", op) + String.format("%04X", memory));
				}
				// Check for all Extedned values as those are also discoverable
				// in the first pass.
			} else if (command.isExtended()) {

				// if(symbolTable.get(command.getDest())-command.getPc()>(Math.pow(2,
				// 11)-1) ||
				// symbolTable.get(command.getDest())-command.getPc()<(Math.pow(2,
				// 11)*-1)){
				// op=opCodes.get(command.getInstruction())+command.getNIvalue();
				// memory = command.getPc()-directives.get("BASE");
				// opCodesLines.put(command.getLine(),
				// String.format("%02X",op)+String.format("%01X",
				// command.getXBPEvalue())+String.format("%05X",memory));
				// }else{
				op = opCodes.get(command.getInstruction()) + command.getNIvalue();
				memory = symbolTable.get(command.getDest());
				opCodesLines.put(command.getLine(), String.format("%02X", op)
						+ String.format("%01X", command.getXBPEvalue()) + String.format("%05X", memory));
				// }
				// All the other commands not dealt with in the first pass
			} else {
				//Start by finding all indirect commands denoted with the @ in the src
				if (command.getDest() != null && command.getDest().startsWith("@")) {
					command.setToIndirect();
					String s2 = command.getDest().substring(1, command.getDest().length());
					//check if Base memory addressing
					if (symbolTable.get(s2) - command.getPc() > (Math.pow(2, 11) - 1)
							&& symbolTable.get(s2) - command.getPc() < Math.pow(2, 11)) {
						command.setToBase();
						op = opCodes.get(command.getInstruction()) + command.getNIvalue();
						memory = directives.get("BASE") - symbolTable.get(s2);
						opCodesLines.put(command.getLine(), String.format("%02X", op)
								+ String.format("%01X", command.getXBPEvalue()) + String.format("%03X", memory));
						//PC memory adresing by default
					} else {
						op = opCodes.get(command.getInstruction()) + command.getNIvalue();
						memory = symbolTable.get(s2) - command.getPc();
						opCodesLines.put(command.getLine(), String.format("%02X", op)
								+ String.format("%01X", command.getXBPEvalue()) + String.format("%03X", memory));
					}

					// now look for indexed items as they will contain the unstranslated %EXX register
				} else if (command.getDest() != null && command.getDest().contains("%EXX")) {
					command.setToIndex();
					String indexAr = command.getDest().substring(0, command.getDest().indexOf('['));
					//Check if Base or not
					if (symbolTable.get(indexAr) - command.getPc() > (Math.pow(2, 11) - 1)
							|| symbolTable.get(indexAr) - command.getPc() < Math.pow(2, 11) * -1) {
						command.setToBase();

						op = opCodes.get(command.getInstruction()) + command.getNIvalue();
						memory = symbolTable.get(indexAr) - directives.get("BASE");
						opCodesLines.put(command.getLine(), String.format("%02X", op)
								+ String.format("%01X", command.getXBPEvalue()) + String.format("%03X", memory));
					} else {
						op = opCodes.get(command.getInstruction()) + command.getNIvalue();
						memory = symbolTable.get(indexAr) - command.getPc();
						opCodesLines.put(command.getLine(), String.format("%02X", op)
								+ String.format("%01X", command.getXBPEvalue()) + String.format("%03X", memory));

					}

					// Deal with all the register commands 
				} else if (command.getInstruction().endsWith("R")) {
					op = opCodes.get(command.getInstruction());

					if (command.getDest() == null) {
						opCodesLines.put(command.getLine(), Integer.toHexString(op)
								+ Integer.toHexString(registers.get(command.getReg())) + Integer.toHexString(0));
					} else {
						opCodesLines.put(command.getLine(),
								Integer.toHexString(op) + Integer.toHexString(registers.get(command.getReg()))
										+ Integer.toHexString(registers.get(command.getDest())));
					}
				} else if (command.getDest() == null) {
					op = opCodes.get(command.getInstruction()) + command.getNIvalue();
					opCodesLines.put(command.getLine(), String.format("%02X", op) + String.format("%04x", 0));
					//All other standard instructions
				} else {

					// Check to see if the addressing mode should switch to base
					if ((symbolTable.get(command.getDest()) - command.getPc()) > (Math.pow(2, 11) - 1)
							|| (symbolTable.get(command.getDest()) - command.getPc()) < (Math.pow(2, 11) * (-1))) {
						command.setToBase();
						op = opCodes.get(command.getInstruction()) + command.getNIvalue();
						memory = symbolTable.get(command.getDest()) - directives.get("BASE");
						opCodesLines.put(command.getLine(), String.format("%02X", op)
								+ String.format("%01X", command.getXBPEvalue()) + String.format("%03X", memory));
					} else {

						op = opCodes.get(command.getInstruction()) + command.getNIvalue();
						memory = symbolTable.get(command.getDest()) - command.getPc();

						String s1 = Integer.toHexString(memory);
						if (s1.startsWith("f")) {
							s1 = shortenMem(s1);
							opCodesLines.put(command.getLine(), String.format("%02X", op)
									+ String.format("%01X", command.getXBPEvalue()) + s1.toUpperCase());
						} else {
							opCodesLines.put(command.getLine(), String.format("%02X", op)
									+ String.format("%01X", command.getXBPEvalue()) + String.format("%03X", memory));
						}

					}
				}

			}

		}

		// MAKE THE LST FILE
		for (int i = 0; i < linesPrinting.size() - 1; i++) {
			String line1 = linesPrinting.get(i);
			//If it is a line with just comment or a Base Directives no need to print OP or Memory Location
			if (line1.startsWith(";") || line1.contains("BASE")) {
				lst.write("\t\t" + String.format("%-60s", line1) + "\n");
				continue;
			} else if (i == linesPrinting.size() - 2) {
				lst.write("\t\t" + String.format("%-60s", line1) + "\n");
				continue;
			}

			if (memoryLocsline.containsKey(i))
				lst.write(String.format("%04X", memoryLocsline.get(i)).toUpperCase());

			lst.write("\t" + String.format("%-60s", line1));

			if (opCodesLines.containsKey(i))
				lst.write("\t" + opCodesLines.get(i).toUpperCase());

			lst.write("\n");
		}

		lst.close();

		// MAKE THE OBJ FILE
		int start = directives.get("START");
		int size = 0;
		String line3 = "";
		int loc = start;
		//print the Header
		obj.write("H" + title + "\t" + String.format("%06X", directives.get("START"))
				+ String.format("%06X", directives.get("END")) + "\n");
		
		//Now go through all the opCodes needed adding them to a long string once it is 60 HEX characters or the next one would make it more than 60 it appends a T the starting memory address and the size of instructions included 
		for (Integer key : opCodesLines.keySet()) {
			if (size + opCodesLines.get(key).length() <= 60) {
				size += opCodesLines.get(key).length();
				line3 = line3 + opCodesLines.get(key).toUpperCase();
			} else {
				line3 = String.format("T%06X%02X", memoryLocsline.get(loc), size / 2) + line3;
				obj.write(line3 + "\n");
				start += size / 2;
				size = opCodesLines.get(key).length();
				line3 = opCodesLines.get(key);
				loc = key;
			}
		}

		// PICKUP THE LAST LINE THAT WAS NOT COPIED
		line3 = String.format("T%06X%02X", memoryLocsline.get(loc), size / 2) + line3;
		obj.write(line3.toUpperCase() + "\n");
		obj.write(String.format("E%06X", directives.get("START")));

		obj.close();

	}
	
	public static String convertRegister(String reg) {
		String register;

		if (reg.equals("%EAX")) {
			return "A";
		} else if (reg.equals("%EBX")) {
			return "B";
		} else if (reg.equals("%ELX")) {
			return "L";
		} else if (reg.equals("%ETX")) {
			return "T";
		} else if (reg.equals("%EXX")) {
			return "X";
		} else if (reg.equals("%ESX")) {
			return "S";
		} else {
			return null;
		}

	}

	public static String[] convertmov(String src, String dest) {
		String[] result = new String[2];

		if (src.startsWith("%")) {
			result[0] = "ST" + convertRegister(src);
			result[1] = dest;
		} else {
			result[1] = src;
			result[0] = "LD" + convertRegister(dest);
		}

		return result;
	}

	public static String shortenMem(String s) {
		s = s.substring(s.length() - 3, s.length());
		return s;
	}

	public static String converCharToHex(String s) {
		int hex;
		String str = "";

		for (int i = 0; i < s.length(); i++) {
			hex = (int) s.charAt(i);
			str += Integer.toHexString(hex);
		}

		return str;
	}
}
