package Assemblers;

import java.util.StringTokenizer;

import Tables.Lit;
import Tables.Sym;
import Tables.Token;
/* -----------------------------------------------------------------------------------
설명 : 어셈블리 코드를 위한 패스1, 패스2과정을 수행하는 클래스이다.
주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기
		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
		   구현하였다.
주의 : try, catch문을 통해 Exception 클래스로 에러를 잡는다.
* -----------------------------------------------------------------------------------
*/
public class Assem_Pass {
	Search srh = new Search();
	
	/* -----------------------------------------------------------------------------------
	* 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
	*		   패스1에서는..
	*		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
	*		   테이블을 생성한다.
	*
	* 매계 : 없음
	* 반환 : 정상 종료 = 0 , 에러 = < 0
	* 주의 : 
	*
	* -----------------------------------------------------------------------------------
	*/
	public int assem_pass1() {
		int indexim = 0; 
		try {
		for (int i = 0; i < Main.input_data.size(); i++) {
			if (token_parsing(i) < 0) {
				return -1;
			}
		}
		
		Main.litcnt=0;
		int startaddr = 0, opnum = 0, symnum=0, j = 0, k = 0, csectnum = 0, locctr=0;
		String litbuf;
		String str;
		String p_token;
		
		for(int i=0; i<Main.token_table.size(); i++){    //리터럴테이블 초기화
			Main.lit_table.add(new Lit());
			Main.sym_table.add(new Sym());                 //심볼테이블 초기화
		}
		int i=0;

		if(Main.token_table.get(i).operator.equals("START")){  //프로그램 시작시
			locctr = Integer.parseInt(Main.token_table.get(i).operand[0]);
			startaddr = locctr;
			Main.token_table.get(i).Addr = locctr;
			i++;
			indexim++;
			Main.sectaddr[0][0] = startaddr;
		}else
			locctr=0;
		
		if(Main.token_table.get(i).operator != null){
			while(Main.token_table.get(i).operator.equals("END") == false){
				if(Main.token_table.get(i).label.charAt(0) != '.'){ //label이 있을 경우
					if(Main.token_table.get(i).operator.equals("CSECT")== true){
						Main.sectaddr[csectnum][1] = locctr;
						locctr=0;   //새 섹션 시작 시 LOCCTR 0으로 초기화
						Main.sym_table.get(srh.search_symbol(Main.token_table.get(i).label)).section = ++csectnum;
						Main.sym_table.get(srh.search_symbol(Main.token_table.get(i).label)).addr = 0;
						Main.token_table.get(i).Addr = locctr;    //인스트럭션 주소 초기화
						Main.sectaddr[csectnum][0] = locctr;
					}
					if(Main.token_table.get(i).label != " "){  //label이 존재
						symnum = srh.search_symbol(Main.token_table.get(i).label); //섹터뺌
						if(symnum<0){  //SYMTAB에 존재하지 않을경우
							Main.sym_table.get(j).symbol = Main.token_table.get(i).label;  //label
							Main.sym_table.get(j).addr = locctr;         //address
							Main.sym_table.get(j).section = csectnum;    //section number
							Main.token_table.get(i).Addr = locctr;       //인스트럭션 주소
							j++;
						}else{     //SYMTAB에 label이 존재할 경우
							if(Main.sym_table.get(symnum).section == csectnum){
								Main.sym_table.get(symnum).addr = locctr;   //주소
							}
							else{
								Main.sym_table.get(j).symbol = Main.token_table.get(i).label;
								Main.sym_table.get(j).addr = locctr;
								Main.sym_table.get(j).section = csectnum;
								j++;
							}
							
							Main.token_table.get(i).Addr = locctr;  //instruction 주소 저장
						}
					}
					else {  //label이 없을 경우
						if(Main.token_table.get(i).operator.equals("EXTDEF") == true || Main.token_table.get(i).operator.equals("EXTREF") == true){ //외부참조 EXTDEF일 경우
							symnum=0;
							for(int idx=0; idx<3; idx++){
								if(Main.token_table.get(i).operand[idx].equals("\0") == false){ //operand 존재할 경우
									symnum = srh.search_symbol(Main.token_table.get(i).operand[idx]);
									if(symnum < 0){ //SYMTAB에 존재하지 않을 경우
										Main.sym_table.get(j).symbol = Main.token_table.get(i).operand[idx];
										Main.sym_table.get(j).section = csectnum;
										j++;
									}else if(Main.sym_table.get(symnum).section == csectnum){ //SYMTAB에 존재할 경우
										Main.sym_table.get(symnum).addr = locctr;
									}
								}
							}
						}
						else if(Main.token_table.get(i).operator.equals("LTORG")==true){ //LTORG 발견 시 LITTAB참조
							while(Main.lit_table.get(Main.litcnt).literal != null){ //LTORG 발견까지 LITTAB에 등록된 리터럴 조회 
								Main.lit_table.get(Main.litcnt).addr = locctr; //해당 literal주소 등록
								locctr += Main.lit_table.get(Main.litcnt).size; //literal의 값
								Main.litcnt++;
							}
						}
						else
							Main.token_table.get(i).Addr = locctr;
					}
					
					opnum = srh.search_opcode(Main.token_table.get(i).operator);
					if(opnum >= 0){
						if(Main.inst_table.get(opnum).format == 34){
							if(Main.token_table.get(i).operator.charAt(0) == '+'){
								Main.token_table.get(i).obformat=4;
								locctr +=4; //+가 붙은 4형식일 경우
							}else{
								Main.token_table.get(i).obformat=3;
								locctr +=3;
							}
						}
						else if(Main.inst_table.get(opnum).format == 2){
							Main.token_table.get(i).obformat=2;
							locctr +=2;
						}
						else{
							Main.token_table.get(i).obformat=1;
							locctr +=1;
						}
					}
					else if (Main.token_table.get(i).operator.equals("WORD") == true){
						Main.token_table.get(i).obformat=3;
						locctr +=3;
					}
					else if (Main.token_table.get(i).operator.equals("RESW") == true){
						locctr +=3 * Integer.parseInt(Main.token_table.get(i).operand[0]);
					}else if (Main.token_table.get(i).operator.equals("RESB") == true){
						locctr += Integer.parseInt(Main.token_table.get(i).operand[0]);;
					}else if (Main.token_table.get(i).operator.equals("BYTE") == true){
						Main.token_table.get(i).obformat=1;
						locctr +=1;
					}else if (Main.token_table.get(i).operator.equals("EQU") == true){
						if(Main.token_table.get(i).operand[0].charAt(0) != '*'){ //*(현재 locctr 값)이 아니라면
							String op_token=Main.token_table.get(i).operand[0];
							
							if(op_token.indexOf('+') >= 0){   //+식일 경우
								StringTokenizer tokens = new StringTokenizer(op_token);
								String op = tokens.nextToken("+");      //연산기호를 기준으로 토큰 분리
								int opaddr1 =  Main.sym_table.get(srh.search_symbol(op, csectnum)).addr; //첫번째 operand의 주소를 찾아 opaddr1에 대입(SYMTAB활용)
								op = tokens.nextToken("+");
								int opaddr2 =  Main.sym_table.get(srh.search_symbol(op, csectnum)).addr;
								
								Main.token_table.get(i).Addr = opaddr1 + opaddr2;  //연산 후의 주소값을 해당 토큰 인스트럭션 주소에 대입
								Main.sym_table.get(srh.search_symbol(op, csectnum)).addr = opaddr1 + opaddr2; //해당 심볼의 주소에 대입
							}
							else if(op_token.indexOf('-') >= 0){  //-식일 경우
								StringTokenizer tokens = new StringTokenizer(op_token);
								String op = tokens.nextToken("-");      //연산기호를 기준으로 토큰 분리
								int opaddr1 =  Main.sym_table.get(srh.search_symbol(op, csectnum)).addr; //첫번째 operand의 주소를 찾아 opaddr1에 대입(SYMTAB활용)
								op = tokens.nextToken("-");
								int opaddr2 =  Main.sym_table.get(srh.search_symbol(op, csectnum)).addr;
								
								Main.token_table.get(i).Addr = opaddr1 - opaddr2;  //연산 후의 주소값을 해당 토큰 인스트럭션 주소에 대입
								Main.sym_table.get(srh.search_symbol(Main.token_table.get(i).label)).addr = opaddr1 - opaddr2; //해당 심볼의 주소에 대입
							}
						}
					}
					if(Main.token_table.get(i).operand[0].charAt(0) == '='){
						
						int litnum = srh.search_literal(Main.token_table.get(i).operand[0]);
						
						if(litnum < 0){ //LITTAB에 없을 경우 (중복 방지)
							Main.lit_table.get(k).literal = Main.token_table.get(i).operand[0]; //LITTAB에 '='로 시작하는 literal 등록	
							//litdata
							int lastindex = Main.lit_table.get(k).literal.lastIndexOf('\'');
							litbuf = Main.lit_table.get(k).literal.substring(3, lastindex); //literal Data를 추출하여 litbuf에 저장
							
							if(Main.lit_table.get(k).literal.charAt(1) == 'C'){ // =C'', 캐릭터일 경우
								int index=0;
								char C_buf;
								for(;index<litbuf.length();index++){   //캐릭터를 아스키코드로 변환하여 data 저장
									C_buf = litbuf.charAt(index);
									Main.lit_table.get(k).litdata += Integer.toHexString(C_buf).toUpperCase();
								}
								Main.lit_table.get(k).size = Main.lit_table.get(k).litdata.length()/2;
							}else{     //=X'' 일경우
								Main.lit_table.get(k).litdata = litbuf; 
								Main.lit_table.get(k).size = Main.lit_table.get(k).litdata.length()-1;//=X일 경우 숫자 2캐릭터당 1바이트
							}
							k++;
						}
						
					}
				}
				i++;
				indexim++;
			}
		}	
		int formatnum;
		if(Main.token_table.get(Main.token_table.size()-1).operator.equals("END") == true){  //프로그램이 끝났을 경우 LITTAB에 리터럴 주소 등록
			int tmp_locctr = 0;		//임시 로케이션 카운터
			for(j=Main.litcnt;j<Main.token_table.size(); j++){
				if(Main.lit_table.get(j).literal != null){
					tmp_locctr += Main.token_table.get(Main.token_table.size()-2).Addr; //END 이전 명령어 주소값
					formatnum=Main.inst_table.get(Main.token_table.size()-2).format; //ENd 이전 명령어의 형식 크기 확인
					if(formatnum == 34){ //3,4형식
						tmp_locctr +=3;
					}
					else if(formatnum ==2){ //2형식
						tmp_locctr +=2;
					}
					Main.lit_table.get(j).addr = tmp_locctr;
					Main.sectaddr[csectnum][1] = Main.lit_table.get(j).addr + Main.lit_table.get(j).size;
				}
			}
		}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("index: "+indexim);
		}
		return 1;
	}
	/* -----------------------------------------------------------------------------------
	* 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
	*		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
	*		   다음과 같은 작업이 수행되어 진다.
	*		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
	* 매계 : 없음
	* 반환 : 정상종료 = 0, 에러발생 = < 0
	* 주의 :
	* -----------------------------------------------------------------------------------
	*/

	public int assem_pass2() {
		int i=0, instnum=-1, sectnum=0;
		try{
		while(i < Main.token_table.size()){
			int extend=0, notpc=0, signcheck=0;
			
			if(Main.token_table.get(i).label.charAt(0) == '.'){ //주석일 경우
				i++;
				continue;
			}
			if(Main.token_table.get(i).operator.equals("CSECT")){ //다음 섹터참조
				sectnum++;
				i++;
				continue;
			}
			
			if(Main.token_table.get(i).operator.charAt(0) == '+'){
				instnum = srh.search_inst(Main.token_table.get(i).operator.substring(1, Main.token_table.get(i).operator.length())); //첫번째문자+을 뺀 해당 operator가 기계어 명령어인지 확인
				extend=1;
				notpc=1;
				Main.token_table.get(i).nixbpe += 2; //초기화(-1 +1 = 0) and +1 //e=1
			}
			else
				instnum = srh.search_inst(Main.token_table.get(i).operator);  //해당 operator가 기계어 명령어인지 확인
			
			if(instnum >=0){ //명령어가 인스트럭션 셋의 명령어일 경우
				if(extend != 1)
					Main.token_table.get(i).nixbpe=0;
				Main.token_table.get(i).obcode=0;
				if(Main.inst_table.get(instnum).format == 2){
					Main.token_table.get(i).obcode = Main.inst_table.get(instnum).opcode << 8; //opcode를 shift한 뒤 저장
					if(Main.inst_table.get(instnum).ops ==1){ //operand 1개
						for (int j = 0; j < 9; j++) {
							if (Main.token_table.get(i).operand[0].equals(Main.reg_table[j])) {
								Main.token_table.get(i).obcode += j << 4; //해당 레지스터번호 저장
								break;
							}
						}
					}
					else{ //레지스터 2개 이상
						for (int j = 0; j < 9; j++) {
							if (Main.token_table.get(i).operand[0].equals(Main.reg_table[j])) {
								Main.token_table.get(i).obcode += j << 4; //해당 레지스터번호 저장
								break;
							}
						}
						for (int j = 0; j < 9; j++) {
							if (Main.token_table.get(i).operand[1].equals(Main.reg_table[j])) {
								Main.token_table.get(i).obcode += j; //해당 레지스터번호 저장
								break;
							}
						}
					}
					i++;
					continue;
				}
				else{   //3,4형식일 경우
					int litnum = -1, litcheck = 0;
					if(Main.token_table.get(i).operand[0].charAt(0) == '#'){
						Main.token_table.get(i).nixbpe += 1 << 4; //ni=01
						if(Character.isDigit(Main.token_table.get(i).operand[0].charAt(1))==true){
							Main.token_table.get(i).obcode += Main.inst_table.get(instnum).opcode << 16 | Main.token_table.get(i).nixbpe << 12 
									| Integer.parseInt(Main.token_table.get(i).operand[0].substring(1, Main.token_table.get(i).operand[0].length()));
							i++;
							continue;
						}else
							signcheck=1;
					}
					else if (Main.token_table.get(i).operand[0].charAt(0) == '@'){
						Main.token_table.get(i).nixbpe += 1 << 5; //ni=10
						signcheck=1;
					}else  //SIC/XE 연산
						Main.token_table.get(i).nixbpe += 3 << 4; //ni=11
					
					for(int j=0; j<=Main.inst_table.get(instnum).ops; j++){  //X레지스터 사용시
						if(Main.token_table.get(i).operand[j].equals("X")){
							Main.token_table.get(i).nixbpe += 1 << 3; //indexing 연산
							break;
						}
					}
					
					if(Main.token_table.get(i).operand[0].equals("\0")){
						;
					}else if(extend == 0){  //extend가 아니면 PC
						Main.token_table.get(i).nixbpe += 1 << 1; //PC Relative, p=1
					}
					
					int symnum = -1;
					if(signcheck == 1){  //#, @ 등의 기호가 있을 경우
						symnum = srh.search_symbol(Main.token_table.get(i).operand[0].substring(1, Main.token_table.get(i).operand[0].length()), sectnum);
					}
					else if(Main.token_table.get(i).operand[0].charAt(0) == '='){  //literal 연산일 경우
							litnum = srh.search_literal(Main.token_table.get(i).operand[0]);
							litcheck=1;
					}
					else{
						symnum = srh.search_symbol(Main.token_table.get(i).operand[0], sectnum);
					}
					
					int reladdr=0;
					if(symnum >= 0){ //operand가 심볼테이블에 존재할 경우
						reladdr = Main.sym_table.get(symnum).addr - Main.token_table.get(i).Addr -3;
						if(reladdr < 0)
							reladdr = 0x0FFF & reladdr;
					}
					else if(litcheck==1){ //literal 연산일 경우
						reladdr = Main.lit_table.get(litnum).addr - Main.token_table.get(i).Addr-3;
					}
					
					if(extend ==1){
						Main.token_table.get(i).obcode += Main.inst_table.get(instnum).opcode << 24 | Main.token_table.get(i).nixbpe << 20 | reladdr;
					}else
						Main.token_table.get(i).obcode += Main.inst_table.get(instnum).opcode << 16 | Main.token_table.get(i).nixbpe << 12 | reladdr;
				}
			}
			else if (Main.token_table.get(i).operator.equals("WORD")){
				Main.token_table.get(i).nixbpe=0;
				Main.token_table.get(i).obcode=0;
				if(Character.isDigit(Main.token_table.get(i).operand[0].charAt(0))==true){
					Main.token_table.get(i).obcode = Integer.parseInt(Main.token_table.get(i).operand[0]);
				}
				else if (Main.token_table.get(i).operand[0].indexOf("+") >= 0){ //+식일 경우
					//
				}
			}
			else if (Main.token_table.get(i).operator.equals("BYTE")){
				Main.token_table.get(i).nixbpe=0;
				Main.token_table.get(i).obcode=0;
				if(Main.token_table.get(i).operand[0].charAt(1) == '\''){ //
					String litbuf;
					litbuf = Main.token_table.get(i).operand[0].substring(2, Main.token_table.get(i).operand[0].length()-1); //' '사이의 Data를 추출하여 litbuf에 저장
				Main.token_table.get(i).obcode= Integer.parseInt(litbuf, 16);
				}	
			}
			else{  //기계어 명령어가 아닐 경우
				i++;
				continue;
			}
			i++;
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return 1;
	}

//	설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다.
//    패스 1로 부터 호출된다.
//매계 : 소스코드의 라인번호
//반환 : 정상종료 = 0 , 에러 < 0
//주의 : my_assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다. 
	public int token_parsing(int index) {
		try {
			String line = null, p_token = null;
			StringTokenizer tokens; // 토큰버퍼

			line = Main.input_data.get(index); // 한 라인 씩 line에 복사

			tokens = new StringTokenizer(line);

			Main.token_table.add(new Token()); // Token테이블 생성

			Main.token_table.get(index).Addr = -1; // 주소 초기화

			// label 파싱
			if (line.charAt(0) == '.') { // 주석처리
				Main.token_table.get(index).label = ".";
				return 0;
			} else if (line.charAt(0) != '\t') { // 첫번째 문자가 탭이 아닐 경우 (탭일 경우엔
													// label이 없음)
				p_token = tokens.nextToken("\t");
				Main.token_table.get(index).label = p_token;
			}

			// operator 파싱
			if (line.charAt(0) == '\t') { // label이 없는 line은 첫번째 토큰이 operator이므로 우선처리
				Main.token_table.get(index).label = " ";
				p_token = tokens.nextToken("\t");
				Main.token_table.get(index).operator = p_token;
			} else {
				p_token = tokens.nextToken("\t");
				Main.token_table.get(index).operator = p_token;
			}

			if (Main.token_table.get(index).operator.equals("RSUB")) { // RSUB일 경우
																	// operand가
																	// 없고
																	// comment만
																	// 있는
																	// line이므로
																	// 예외처리
				p_token = tokens.nextToken("\t");
				Main.token_table.get(index).comment = p_token;
				return 0;
			}
			
			String opbuf = null;
			if (tokens.hasMoreTokens() == true){
				p_token = tokens.nextToken("\t"); // comment부터 처리하기 위해 operand 토큰을 // 따로 저장	
				opbuf = p_token;
			}
											
			// comment 파싱
			if (tokens.hasMoreTokens() == true) { // 다음토큰이 있으면 (코멘트가 있으면)
				p_token = tokens.nextToken("\t");
				Main.token_table.get(index).comment = p_token;
			}

			// operand 파싱
			if (opbuf != null) {

				if (opbuf.indexOf(',') < 0) { // ,가 없는 단일 operand
					Main.token_table.get(index).operand[0] = opbuf;
				} else {
					StringTokenizer optokens = new StringTokenizer(opbuf);
					p_token = optokens.nextToken(",");
					Main.token_table.get(index).operand[0] = p_token;
					p_token = optokens.nextToken(",");
					Main.token_table.get(index).operand[1] = p_token;

					if (optokens.hasMoreTokens() == true) {
						Main.token_table.get(index).operand[2] = optokens.nextToken(",");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("linenum: "+index);
			return -1;
		}
		return 0;
	}
	
	
}
