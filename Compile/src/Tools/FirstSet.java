package Tools;

import WordAnalyse.Word;

import static Tools.WordProvider.FindInExpFirst;

public class FirstSet {
    //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    public static boolean FindInConstDeclFirst(Word word) {
        return word.getCategoryCode().equals("CONSTTK");
    }

    //BType → 'int'
    public static boolean FindInBTypeFirst(Word word) {
        return word.getCategoryCode().equals("INTTK");
    }

    // FuncType → 'void' | 'int'
    public static boolean FindInFuncTypeFirst(Word word) {
        return word.getCategoryCode().equals("VOIDTK") || word.getCategoryCode().equals("INTTK");
    }

    //VarDecl → BType VarDef { ',' VarDef } ';'
    public static boolean FindInVarDeclFirst(Word word) {
        return FindInBTypeFirst(word);
    }

    //Decl → ConstDecl | VarDecl
    public static boolean FindInDeclFirst(Word word) {
        return FindInConstDeclFirst(word) || FindInVarDeclFirst(word);
    }

    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    public static boolean FindInConstDefFirst(Word word) {
        return word.getCategoryCode().equals("IDENFR");
    }

    //ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    public static boolean FindInConstInitValFirst(Word word) {
        return word.getCategoryCode().equals("LBRACE") || FindInConstExp(word);
    }

    //VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    public static boolean FindInVarDefFirst(Word word) {
        return word.getCategoryCode().equals("IDENFR");
    }

    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public static boolean FindInInitValFirst(Word word) {
        return word.getCategoryCode().equals("LBRACE") || FindInExpFirst(word);
    }

    //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public static boolean FindInFuncDefFirst(Word word) {
        return FindInFuncTypeFirst(word);
    }

    //FuncFParams → FuncFParam { ',' FuncFParam }
    public static boolean FindInFuncFParamsFirst(Word word) {
        return FindInFuncFParamFirst(word);
    }

    //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    public static boolean FindInFuncFParamFirst(Word word) {
        return FindInBTypeFirst(word);
    }

    //Block → '{' { BlockItem } '}'
    public static boolean FindInBlockFirst(Word word) {
        return word.getCategoryCode().equals("LBRACE");
    }

    //ForStmt → LVal '=' Exp
    public static boolean FindInForStmtFirst(Word word) {
        return FindInLValFirst(word);
    }

    // LVal → Ident
    public static boolean FindInLValFirst(Word word) {
        return word.getCategoryCode().equals("IDENFR");
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number
    public static boolean FindInPrimaryExpFirst(Word word) {
        return word.getCategoryCode().equals("LPARENT") || FindInLValFirst(word) || FindInNumberFirst(word);
    }

    //Number → IntConst
    public static boolean FindInNumberFirst(Word word) {
        return word.getCategoryCode().equals("INTCON");
    }

    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    public static boolean FindInUnaryExpFirst(Word word) {
        return FindInPrimaryExpFirst(word) || FindInUnaryOpFirst(word) || word.getCategoryCode().equals("IDENFR");
    }

    // UnaryOp → '+' | '−' | '!'
    public static boolean FindInUnaryOpFirst(Word word) {
        return word.getCategoryCode().equals("PLUS") || word.getCategoryCode().equals("MINU") || word.getCategoryCode().equals("NOT");
    }

    //FuncRParams → Exp { ',' Exp }
    public static boolean FindInFuncRParamsFirst(Word word) {
        return FindInExpFirst(word);
    }

    //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    public static boolean FindInMulExpFirst(Word word) {
        return FindInUnaryExpFirst(word);
    }

    //AddExp → MulExp | AddExp ('+' | '−') MulExp
    public static boolean FindInAddExpFirst(Word word) {
        return FindInMulExpFirst(word);
    }

    //RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    public static boolean FindInRelExpFirst(Word word) {
        return FindInAddExpFirst(word);
    }

    // EqExp → RelExp | EqExp ('==' | '!=') RelExp
    public static boolean FindInEqExpFirst(Word word) {
        return FindInRelExpFirst(word);
    }

    //LAndExp → EqExp | LAndExp '&&' EqExp
    public static boolean FindInLAndExpFirst(Word word) {
        return FindInEqExpFirst(word);
    }

    // LOrExp → LAndExp | LOrExp '||' LAndExp
    public static boolean FindInLOrExpFirst(Word word) {
        return FindInLAndExpFirst(word);
    }

    //ConstExp → AddExp
    public static boolean FindInConstExp(Word word) {
        return FindInAddExpFirst(word);
    }
}
