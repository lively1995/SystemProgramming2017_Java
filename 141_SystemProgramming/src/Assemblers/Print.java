package Assemblers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

/* -----------------------------------------------------------------------------------
* 설명 : pass1, pass2를 거쳐 만든 어셈블리코드를 출력해주는 함수이다.
*
*/
public class Print {
	/* -----------------------------------------------------------------------------------
	* 설명 : pass1, pass2를 거쳐 만든 immediate Data를 화면으로 출력해주는 함수이다.
	* 매계 : LTORG 연산 후 아직 주소를 배정받지 못한 리터럴들의 첫번재LITTAB 인덱스
	* 반환 : 없음
	* -----------------------------------------------------------------------------------
	*/
	Search srh = new Search();
	public void Immediate_Data(){
		int isop, issym, j = 0, count = 0, lt=0, tab=0;

		for (int i = 0; i < Main.input_data.size(); i++) {
			if (Main.token_table.get(i).label.charAt(0) == '.')		//주석은 개행 후 스킵
				continue;
			
			//Addr 출력
			if (Main.token_table.get(i).Addr != -1) {
				if (Main.token_table.get(i).operator.equals("CSECT")) {
					System.out.print("\t");
				}
				else
					System.out.format("%04X\t", Main.token_table.get(i).Addr);
			}
			else
				System.out.print("\t");

			//label 출력
			if (Main.token_table.get(i).label != null)
				System.out.format("%s	", Main.token_table.get(i).label);
			else
				System.out.print("\t");

			//operator 출력
			if (Main.token_table.get(i).operator.equals(" ") == false) {
				System.out.format("%s	", Main.token_table.get(i).operator);
				if (Main.token_table.get(i).operator.equals("LTORG")) {		//LTORG 인 경우
					for (; lt < Main.litcnt; lt++) {
						if (Main.lit_table.get(lt).addr != -1) {
							System.out.format("\n%04X\t*\t%s\t\t\t", Main.lit_table.get(lt).addr, Main.lit_table.get(lt).literal); //LITTAB에 주소값이 배정된 모든 리터럴 출력
							System.out.format("%s\n", Main.lit_table.get(lt).litdata);
						}
					}
					continue;
				}
			}
			//operand 출력
			if (Main.token_table.get(i).operand[0].charAt(0) != '\0')
				System.out.format("%s", Main.token_table.get(i).operand[0]);
			for (j = 1; j < 3; j++) {
				if (Main.token_table.get(i).operand[j].charAt(0) != '\0') {
					System.out.format(",%s", Main.token_table.get(i).operand[j]);
				}
			}
			
			System.out.print("\t\t");

			//objectcode 코드 출력
			
			if (Main.token_table.get(i).obcode != -1) {
				if(Main.token_table.get(i).obformat>=3){
					System.out.format("%06X", Main.token_table.get(i).obcode);
				}else if(Main.token_table.get(i).obformat ==2){
					System.out.format("%02X", Main.token_table.get(i).obcode);
				}else
					System.out.format("%01X", Main.token_table.get(i).obcode);
			}

			//END 후 literal 출력
			if (Main.token_table.get(i).operator.equals("END")) { //프로그램이 끝났을 경우 literal 출력
				for (j = Main.litcnt; j < Main.token_table.size(); j++) {
					if (Main.lit_table.get(j).literal != null) {
						System.out.format("\n%04X\t*\t%s\t\t\t", Main.lit_table.get(j).addr, Main.lit_table.get(j).literal); //LITTAB에 주소값이 배정된 모든 리터럴 출력
						System.out.format("%s\n", Main.lit_table.get(j).litdata);
					}
				}
			}
			System.out.print("\n");
		}
	}
	
	/* -----------------------------------------------------------------------------------
	* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
	* 매계 : 생성할 오브젝트 파일명
	* 반환 : 없음
	* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
	*        화면에 출력해준다.
	*
	* -----------------------------------------------------------------------------------
	*/
	public void Make_ObjectCode(String output) throws IOException{
		String[] object_code = new String[50];	//obcode
		String[] ref_table = new String[3];
		int i = 0, j = 0;
		int sectnum = 0;	//섹션번호
		int linenum = 0; //object_code의 세로 line, 가로 index
		int length;
		int H_check = 0;
		int T_cnt = 0, T_start = 0;
		String T_code = null;
		String[] M_code = new String[30];
		int M_codeindex = 0, M_find = 0, M_len = 0, M_addr = 0, ref_index = -1, ref_index2 = -1;
		
		for(int m=0; m<10; m++){
			M_code[m] = null;   //M_code 초기화
		}
		for(; i<Main.token_table.size(); i++){
			if(Main.token_table.get(i).label.charAt(0) == '.'){ //주석일 경우
				continue;
			}
			if(Main.token_table.get(i).operator.equals("CSECT")){ //다음 섹터참조
				sectnum++;
				if(T_code != null){ //섹션이 넘어가는 경우 이전에 썻던 T레코드를 작성
					object_code[linenum] = String.format("T%06X%02X%s", T_start, T_cnt / 2, T_code); //T,시작주소,길이,코드 작성
					T_start = 0;
					T_cnt = 0;
					T_code = null;
					linenum++;
				}
				int m=0;
				while(M_code[m] != null){
					object_code[linenum] = null;
					object_code[linenum++] = M_code[m++];
				}

				for (int n = 0; n < 10; n++) {  //M_코드 초기화
					M_code[n] = null;
				}
				M_codeindex = 0;  //M_code 인덱스 초기화

				object_code[linenum] = null;
				if (sectnum == 0) {
					object_code[linenum++] = String.format("E%06X", Main.sectaddr[sectnum][0]);  //첫 섹션은 돌아갈 시작주소
				}
				else
					object_code[linenum++] = "E"; //아니라면 돌아갈 시작주소를 모르기때문에 비움.
				H_check = 0;
				
				continue;
			}
			length = 0;
			
			//H레코드
			if (H_check == 0) {
				object_code[linenum] = "H";
				if (sectnum == 0)
					object_code[linenum] += String.format("%-6s", Main.token_table.get(i).label); //프로그램 이름
				else
					object_code[linenum] += String.format("%-6s", Main.token_table.get(i-4).label); //섹션 이름. 주석크기+EXTREF=3+1=4 총 4만큼떨어져있어 i-5
				object_code[linenum] += String.format("%06X", Main.sectaddr[sectnum][0]);//시작주소
				object_code[linenum] += String.format("%06X", Main.sectaddr[sectnum][1] - Main.sectaddr[sectnum][0]); //길이 = 마지막주소-시작주소
				length = 0;
				linenum++;
				H_check = 1;
			}
			
			//D레코드
			if(Main.token_table.get(i).operator.equals("EXTDEF")){
				object_code[linenum] = "D";
				for(int d=0; d<3; d++){
					if(Main.token_table.get(i).operand[d].charAt(0) != '\0'){
						object_code[linenum] += String.format("%-6s", Main.token_table.get(i).operand[d]);
						object_code[linenum] += String.format("%06X", Main.sym_table.get(srh.search_symbol(Main.token_table.get(i).operand[d])).addr);
					}
				}
				length=0;
				linenum++;
				continue;
			}
			else if (Main.token_table.get(i).operator.equals("EXTREF")){ //R레코드
				object_code[linenum] = "R";
				for(int r=0; r<3; r++){
					ref_table[r] = null;
				}
				for(int d=0; d<3; d++){
					if(Main.token_table.get(i).operand[d].charAt(0) != '\0'){
						ref_table[d] = Main.token_table.get(i).operand[d];
						object_code[linenum] += String.format("%-6s", ref_table[d]);
					}
				}
				length=0;
				linenum++;
				continue;
			}
			
			//T레코드
			int isinst=srh.search_inst(Main.token_table.get(i).operator);
			int ob_len = Main.token_table.get(i).obformat;  //해당 obcode의 길이 파악
			String ref_tmp = null, ref_tmp2 = null;
			
			if(Main.token_table.get(i).obcode != -1){  //obcode가 있는 경우	
				if(T_code != null){
					if(T_cnt+ob_len >=60){
						object_code[linenum] = String.format("T%06X%02X%s", T_start, T_cnt / 2, T_code); //T,시작주소,길이,코드 작성
						T_start = 0;
						T_cnt = 0;
						T_code = null;
						linenum++;
					}
				}
				if(T_cnt==0){ //T레코드 첫 시작주소 판별
					T_start = Main.token_table.get(i).Addr;
				}
				
				if(T_code == null){
					if(Main.token_table.get(i).obformat>=3){
						T_code = String.format("%06X", Main.token_table.get(i).obcode);
					}else if(Main.token_table.get(i).obformat ==2){
						T_code = String.format("%04X", Main.token_table.get(i).obcode);
					}else{
						T_code = String.format("%02X", Main.token_table.get(i).obcode);
					}	
					T_cnt += Main.token_table.get(i).obformat*2;
				}else{
					if(Main.token_table.get(i).obformat>=3){
						T_code += String.format("%06X", Main.token_table.get(i).obcode);
					}else if(Main.token_table.get(i).obformat ==2){
						T_code += String.format("%04X", Main.token_table.get(i).obcode);
					}else{
						T_code += String.format("%02X", Main.token_table.get(i).obcode);
					}
					T_cnt = T_code.length();
				}
				String op_token; // 임시토큰
				op_token = Main.token_table.get(i).operand[0]; //값을 계산해야하는 operand를 버퍼에 복사
				String tmp;
				for(int r=0; r<3; r++){
					if(op_token.equals(ref_table[r])){
						if(ref_table[r] != null){
							M_len = Main.token_table.get(i).Addr+1;
							M_find=1;
							ref_index=r;
							break;
						}
					}
					else if (Main.token_table.get(i).operand[0].indexOf('-') >= 0){  //ref-ref일 경우
						tmp=Main.token_table.get(i).operand[0];
						StringTokenizer tokens = new StringTokenizer(tmp);
						tmp = tokens.nextToken("-");
						for (int re = 0; re < 3; re++) {
							if (tmp.equals(ref_table[re])) {  //레퍼런스 테이블에 있는지 확인
								if (ref_table[r] != null) {
									M_len = Main.token_table.get(i).Addr;
									M_find = 2;
									ref_tmp = ref_table[re];
									ref_index = re;
									break;
								}
							}
						}
						tmp=tokens.nextToken("-");
						for (int re = 0; re < 3; re++) {
							if (tmp.equals(ref_table[re])) {  //레퍼런스 테이블에 있는지 확인
								if (ref_table[r] != null) {
									M_len = Main.token_table.get(i).Addr;
									ref_tmp2 = ref_table[re];
									ref_index2 = re;
									break;
								}
							}
						}
						break;
					}
				}
			}else if(Main.token_table.get(i).operator.equals("RESW") || Main.token_table.get(i).operator.equals("RESB")){//object_code가 연속적이지 않을 경우
				if (T_code != null) {
					object_code[linenum] = String.format("T%06X%02X%s", T_start, T_cnt / 2, T_code); //T,시작주소,길이,코드 작성
					T_start = 0;
					T_cnt = 0;
					T_code = null;
					linenum++;//obcode가 연속적이지 않은 경우
					continue;
				}
			}else if (Main.token_table.get(i).operator.equals("LTORG")) {		//LTORG일경우 추가
				for (; j < Main.litcnt; j++) {
					if (Main.lit_table.get(j).addr != -1) {
						if (T_cnt + Main.lit_table.get(j).size >= 60) {		//60칼럼 이상일 경우
							object_code[linenum] = String.format("T%06X%02X%s", T_start, T_cnt / 2, T_code); //T,시작주소,길이,코드 작성
							T_start = 0;
							T_cnt = 0;
							T_code = null;
							linenum++;
						}
						if (T_cnt == 0) { //T레코드 첫 시작주소 판별
							T_start = Main.lit_table.get(j).addr;
						}
						if(T_code == null)
							T_code = String.format("%s", Main.lit_table.get(j).litdata);
						else
							T_code += String.format("%s", Main.lit_table.get(j).litdata);
						T_cnt=T_code.length();
					}
				}
				object_code[linenum] = String.format("T%06X%02X%s", T_start, T_cnt / 2, T_code); //T,시작주소,길이,코드 작성
				T_start = 0;
				T_cnt = 0;
				T_code = null;
				linenum++;
				continue;
			}
					
			//M레코드
			if (M_find == 1) {  //ref를 4형식으로 참조했을 경우
				if(M_code[M_codeindex] == null)
					M_code[M_codeindex] = String.format("M%06X05+%s", M_len, ref_table[ref_index]); //위치,n번째부터참조인지,+,레퍼런스
				else
					M_code[M_codeindex] += String.format("M%06X05+%s", M_len, ref_table[ref_index]);
				M_codeindex++;
				M_find = 0;
			}
			else if (M_find == 2) { //연산이 있는 m레코드일 경우 ref-ref
				for (int re = 0; re < 2; re++) {
					if (re == 0){
						if(M_code[M_codeindex] == null)
							M_code[M_codeindex]=String.format("M%06X06+%s", M_len, ref_tmp); //위치,n번째부터참조인지,+,레퍼런스
						else
							if(M_code[M_codeindex] == null)
								M_code[M_codeindex]=String.format("M%06X06+%s", M_len, ref_tmp);
							else
								M_code[M_codeindex]+=String.format("M%06X06+%s", M_len, ref_tmp);
					}else{
						if(M_code[M_codeindex] == null)
							M_code[M_codeindex]=String.format("M%06X06-%s", M_len, ref_tmp2); //위치,n번째부터참조인지,-,레퍼런스
						else
							M_code[M_codeindex]+=String.format("M%06X06-%s", M_len, ref_tmp2);
					}
					M_codeindex++;
				}
				M_find = 0;
			}
			
			//END, 프로그램이 끝났을 때
			if (Main.token_table.get(i).operator.equals("END")) {
				for (j = Main.litcnt; j < Main.token_table.size(); j++) {
					if (Main.lit_table.get(j).addr != -1) {
						if (T_cnt + Main.lit_table.get(j).size >= 60) {		//60칼럼 이상일 경우
							object_code[linenum] = String.format("T%06X%02X%s", T_start, T_cnt / 2, T_code); //T,시작주소,길이,코드 작성
							T_start = 0;
							T_cnt = 0;
							T_code = null;
							linenum++;
						}
						if (T_cnt == 0) { //T레코드 첫 시작주소 판별
							T_start = Main.lit_table.get(j).addr;
						}

						T_code += String.format("%s", Main.lit_table.get(j).litdata);
						T_cnt=Main.lit_table.get(j).litdata.length() + T_code.length();
						break;
					}
				}
				//마지막T레코드 작성
				object_code[linenum] = String.format("T%06X%02X%s", T_start, T_cnt / 2, T_code);
				linenum++;
				//마지막M레코드 작성
				int m = 0;
				while (M_code[m] != null) {    //M레코드의 코드 작성
					object_code[linenum] = null;
					object_code[linenum++] = M_code[m++];
				}
				//마지막E레코드 작성
				if (sectnum == 0) {
					object_code[linenum] = String.format("E%06X", Main.sectaddr[sectnum][0]); //첫 섹션은 돌아갈 시작주소
				}
				else
					object_code[linenum++] = "E";  //아니라면 돌아갈 시작주소를 모르기때문에 비움.

				object_code[linenum] = null;
			}			
		}
		//test
		int q=0;
		while(object_code[q] != null){
			System.out.format("%s\n", object_code[q]);
			q++;
		}
		
		BufferedWriter out = new BufferedWriter(new FileWriter(output));
		if(out != null){
			int z=0;
			while(object_code[z] != null){
				out.write(String.format("%s\n", object_code[z++]));
			}
		}
		out.close();
	}
}
