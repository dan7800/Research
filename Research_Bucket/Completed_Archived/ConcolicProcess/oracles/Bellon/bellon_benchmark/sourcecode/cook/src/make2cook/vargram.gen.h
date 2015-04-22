typedef union {
       string_ty     *lv_string;
       string_list_ty        lv_list;
} vargram_STYPE;
#define        COLON  258
#define        COMMA  259
#define        DOLLAR 260
#define        EQU    261
#define        LB     262
#define        LP     263
#define        PLAIN  264
#define        RB     265
#define        RP     266
#define        SPACE  267
extern vargram_STYPE vargram_lval;
