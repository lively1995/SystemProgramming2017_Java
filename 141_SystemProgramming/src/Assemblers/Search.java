package Assemblers;

import java.util.StringTokenizer;

public class Search {
	
	//설명 : 입력 문자열 SYMTAB에 들어있는지,현 섹션과 일치하는지 검사하는 함수이다.
	 //매계 : 토큰 단위로 구분된 label, 현섹션번호
	 //반환 : 정상종료 = 해당 symbol의 인덱스, 에러 < 0
	public int search_symbol(String label){
		for(int i=0; i<Main.sym_table.size(); i++){
				if(Main.sym_table.get(i).symbol.equals(label)){
					return i;
				}
		}
		return -1;
	}
	// 설명 : 입력 문자열이 SYMTAB에 들어있는지 검사하는 함수이다.
	 //매계 : 토큰 단위로 구분된 label
	 //반환 : 정상종료 = 해당 symbol의 인덱스, 에러 < 0
	public int search_symbol(String label, int sect){
		for(int i=0; i<Main.sym_table.size(); i++){
			if((Main.sym_table.get(i).section == sect)){
				if(Main.sym_table.get(i).symbol.equals(label)){
					return i;
				}
			}
		}
		return -1;
	}
//	설명 : 입력 문자열이 LITTAB에 들어있는지 검사하는 함수이다.
//	 매계 : 토큰 단위로 구분된 literal
//	 반환 : 정상종료 = 해당 literal의 인덱스, 에러 < 0
	public int search_literal(String str){
		for(int i=0; i <= Main.litcnt; i++){
			if(Main.lit_table.get(i).literal != null){
				if(Main.lit_table.get(i).literal.equals(str) == true)
					return i;
			}
		}
		return -1;
	}
//	 설명 : 입력 문자열이 inst_table에 들어있는지 검사하는 함수이다.
//	 매계 : 토큰 단위로 구분된 토큰테이블의 operator
//	 반환 : 정상종료 = 해당 기계어 코드의 인덱스, 에러 < 0
	public int search_inst(String str){
		for(int i=0; i<Main.inst_table.size(); i++){
			if(Main.inst_table.get(i).operator.equals(str) == true)
				return i;
		}
		return -1;
	}
	//설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다.
	 //매계 : 토큰 단위로 구분된 문자열
	 //반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0
	public int search_opcode(String str){
		for (int i = 0; i < Main.inst_table.size(); i++) {
			if (str.charAt(0) == '+') {									//+가 붙는 extended일 경우
				StringTokenizer tokens = new StringTokenizer(str);
				String buf = tokens.nextToken("+");
				if (Main.inst_table.get(i).operator.equals(buf) == true)	//+ 다음 번지주소를 매개변수로 사용
					return i;
			}
			else if (Main.inst_table.get(i).operator.equals(str) == true)		//일반 operator
				return i;
		}
		return -1;
	}
	
}
