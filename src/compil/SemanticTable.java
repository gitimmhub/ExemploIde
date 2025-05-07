/*
 * Template to help verify type compatibility in a Semantic Analyzer.
 * Available to Computer Science course at UNIVALI.
 * Professor Eduardo Alves da Silva.
 */

package compil;

public class SemanticTable {
   
   public static final int ERR = -1;
   public static final int OK_ = 0;
   public static final int WAR = 1;


   public static final int INT = 0;
   public static final int FLO = 1;
   public static final int CHA = 2;
   public static final int STR = 3;
   public static final int BOO = 4;

   public static final int SUM = 0;
   public static final int SUB = 1;
   public static final int MUL = 2;
   public static final int DIV = 3;
   public static final int REL = 4; // qualquer operador relacional

      // TIPO DE RETORNO DAS EXPRESSOES ENTRE TIPOS
   // 5 x 5 x 10 = TIPO x TIPO x OPERADOR
   static int[][][] expTable = {
      { // INT
          {INT, INT, INT, FLO, BOO, INT, INT, FLO, ERR, ERR}, // INT op INT
          {FLO, FLO, FLO, FLO, BOO, ERR, FLO, FLO, ERR, ERR}, // INT op FLO
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // INT op CHA
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // INT op STR
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}  // INT op BOO
      },
      { // FLOAT
          {FLO, FLO, FLO, FLO, BOO, ERR, FLO, FLO, ERR, ERR}, // FLO op INT
          {FLO, FLO, FLO, FLO, BOO, ERR, FLO, FLO, ERR, ERR}, // FLO op FLO
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // FLO op CHA
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // FLO op STR
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}  // FLO op BOO
      },
      { // CHAR
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // CHA op INT
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // CHA op FLO
          {STR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // CHA op CHA
          {STR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // CHA op STR
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}  // CHA op BOO
      },
      { // STRING
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // STR op INT
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // STR op FLO
          {STR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // STR op CHA
          {STR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // STR op STR
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}  // STR op BOO
      },
      { // BOOL
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // BOO op INT
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // BOO op FLO
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // BOO op CHA
          {ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR}, // BOO op STR
          {ERR, ERR, ERR, ERR, BOO, ERR, ERR, ERR, BOO, BOO}  // BOO op BOO
      }
  };

  // Atribuições compatíveis: 5 x 5 = TIPO destino x TIPO origem
  static int[][] atribTable = {
      // INT   FLO   CHA   STR   BOO
      { OK_,  WAR,  ERR,  ERR,  ERR },  // INT
      { ERR,  OK_,  ERR,  ERR,  ERR },  // FLO
      { ERR,  ERR,  OK_,  ERR,  ERR },  // CHA
      { ERR,  ERR,  OK_,  OK_,  ERR },  // STR (permite char para string e string para string)
      { ERR,  ERR,  ERR,  ERR,  OK_ }   // BOO
  };   
   static int resultType (int TP1, int TP2, int OP){
      return (expTable[TP1][TP2][OP]);
   }
   
   static int atribType (int TP1, int TP2){
      return (atribTable[TP1][TP2]);
   }
}
