package Assemblers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import Tables.*;

public class Main {
	static int MAX_INST = 256;
	public static ArrayList<String> input_data = new ArrayList<>(); // 어셈블리 할 소스코드를 토큰단위로 관리하는  테이블
	static int line_num; // 소스코드 라인넘버
	public static ArrayList<Inst> inst_table = new ArrayList<>();
	public static ArrayList<Token> token_table = new ArrayList<>();
	public static ArrayList<Sym> sym_table = new ArrayList<>();
	public static ArrayList<Lit> lit_table = new ArrayList<>();
	static int litcnt; //LTORG 명령어 처리를 위한 리터럴 카운터
	public static String[] reg_table = { "A", "X", "L", "B", "S", "T", "F", "PC", "SW" }; // 레지스터 테이블															
	public static int[][] sectaddr = new int[3][2];

	public static void main(String[] args) throws IOException{
		Init_My_Assembler init_my_assembler = new Init_My_Assembler();
		Assem_Pass AP = new Assem_Pass();
		Print print = new Print();
		
		 	if (init_my_assembler.instFile("res/inst.data") < 0)
			{
				System.out.println("init_my_assembler: Failed to initialize of program.\n");
				return;
			}
		 	if (init_my_assembler.inputFile("res/input.txt") < 0)
			{
				System.out.println("init_my_assembler: Failed to initialize of program.\n");
				return;
			}
			if (AP.assem_pass1() < 0) {
				System.out.println("assem_pass1: Failed to pass1 process. \n");
				return;
			}
			if (AP.assem_pass2() < 0) {
				System.out.println(" assem_pass2: Failed to pass2 process. \n");
				return;
			}

			print.Immediate_Data();
			print.Make_ObjectCode("output_141");

			return;
	 }
}
