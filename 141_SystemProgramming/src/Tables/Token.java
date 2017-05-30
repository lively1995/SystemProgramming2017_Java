package Tables;

public class Token {
	public int Addr;
	public String label;
	public String operator;
	public String operand[] = new String[3];
	public String comment;
	public int nixbpe;
	public int obformat;
	public int obcode;
	
	public Token(){
		Addr= -1;
		label = null;
		operator = " ";
		for(int i=0; i<3; i++){
			operand[i]="\0";
		}
		comment=null;
		nixbpe=-1;
		obcode=-1;
		obformat=0;
	}
}
