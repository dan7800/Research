/* office information tables */
/* offinf - office race information */
extern int offinf[6];
#define CANDCOUNT 0
#define CANDSTART 1
#define VOTESPERVOTER 2
#define WRITEINCOUNT 3  /* -1 means no writeins allowed */
#define DUALLOCATION 4
#define DUALSTART 5
/* allow  - voter elegibility information */
extern long int allow[3];
extern char allowch[2];
#define TYPE 0
#define VALUE 1
#define PRECID 2
#define ALLOWALL  -1L
#define ALLOWUNIT  2L
#define ALLOWCOMM  3L
#define ALLOWREPR  4L
#define ALLOWSEN   5L
#define ALLOWCONG  6L
#define ALLOWDIST  7L
#define ALLOWPREC  8L
#define ALLOWWARD  9L
#define ALLOWCOLL  1L
/* prtinf - printing information */
extern int prtinf[2];  /* logical */
#define STATE 0 /* whether to include in final state govt. book */
#define PERCENT 1 /* whether to print percentages for this office */
