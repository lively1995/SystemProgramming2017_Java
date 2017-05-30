package Assemblers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import Tables.Inst;
/* -----------------------------------------------------------------------------------
설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 클래스이다.
주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기
		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
		   구현하였다.
주의 : try, catch문을 통해 IOException 클래스를 상속받아 에러를 잡는다.
* -----------------------------------------------------------------------------------
*/
public class Init_My_Assembler {

	/*
	 * -------------------------------------------------------------------------
	 * ---------- 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을 생성하는 함수이다.
	 * 매계 : 기계어 목록 파일 반환 : 정상종료 = 0 , 에러 < 0 주의 : 기계어 목록파일 형식은 다음과 같다.
	 *
	 * =========================================================================
	 * ====== | 이름 | 형식 | 기계어 코드 | 오퍼랜드의 갯수 | NULL|
	 * =========================================================================
	 * ======
	 *
	 * -------------------------------------------------------------------------
	 * ----------
	 */
	public int instFile(String inst_file) {
		String line; // 라인별로 한줄씩 저장할 임시 버퍼
		try {
			FileReader filereader = new FileReader(inst_file);
			BufferedReader reader = new BufferedReader(filereader);
			StringTokenizer tokens;

			while ((line = reader.readLine()) != null) { // txt파일로부터 한 줄 씩 읽기
				// StringTokenizer를 이용한 문자열 분리
				tokens = new StringTokenizer(line);

				Main.inst_table.add(new Inst());
				Main.inst_table.get(Main.inst_table.size() - 1).operator = tokens.nextToken("\t");
				for (int i = 0; i < 3; i++) {
					if (i == 0)
						Main.inst_table.get(Main.inst_table.size() - 1).format = Integer
								.parseInt(tokens.nextToken("\t")); // format
					// 저장
					else if (i == 1) {
						Main.inst_table.get(Main.inst_table.size() - 1).opcode = Integer
								.parseInt(tokens.nextToken("\t"), 16); // opcode
						// 저장
					} else
						Main.inst_table.get(Main.inst_table.size() - 1).ops = Integer.parseInt(tokens.nextToken("\t")); // operand
																														// 갯수
																														// 저장
				}
			}
			filereader.close();
			reader.close();
		} catch (IOException e) {
			System.out.println(e);
			return -1;
		}
		return 0;
	}

	/*
	 * -------------------------------------------------------------------------
	 * ---------- 설명 : 어셈블리 할 소스코드를 읽어오는 함수이다. 매계 : 어셈블리할 소스파일명 
	 * 반환 : 정상종료 = 0 ,
	 * 에러 < 0 
	 * 주의 :
	 * -------------------------------------------------------------------------
	 * ----------
	 */
	public int inputFile(String input_file) {
		String line; // 라인별로 한줄씩 저장할 임시 버퍼
		try {
			FileReader filereader = new FileReader(input_file);
			BufferedReader reader = new BufferedReader(filereader);
			StringTokenizer tokens;

			while ((line = reader.readLine()) != null) { // txt파일로부터 한 줄 씩 읽기
				// StringTokenizer를 이용한 문자열 분리
				Main.input_data.add(line);
			}
			reader.close();
		} catch (IOException e) {
			System.out.println(e);
			return -1;
		}
		return 0;
	}

}