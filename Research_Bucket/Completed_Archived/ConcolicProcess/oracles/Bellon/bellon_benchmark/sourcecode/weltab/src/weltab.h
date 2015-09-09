/* weltab global */
#define LOGICAL int
#define TRUE 1
#define FALSE 0
#define max(a,b)       (((a) > (b)) ? (a) : (b))
#define min(a,b)       (((a) < (b)) ? (a) : (b))
#define abs(a)         (((a) < 0) ? -(a) : (a))
LOGICAL trace;
LOGICAL fulltrace;
long int offset;
#define MAXCAND     1050  /* MAX # OF CANDIDATES, INCL. WRITEIN */
                          /* had been 1500 */
#define MAXPRECINCT  240  /* MAX # OF PRECINCTS */
                          /* had been 250 */
#define MAXDCAND      20  /* had been 200 */
#define MAXOFFICE    260  /* had been 500 */
#define MAXDUAL        5  /* had been 50 ? */
#define MAXDAREC     201  /* MAX-PRECINCT PLUS ONE */
int nunit,nprec;
int noffic,ndual,nwrite;
/* noffic - current number of offices */
/* ndual  - current number of dual offices */
/* nwrite - number of write-in name slots/office after scattered */
int ncand,ducand;
/* ncand  - current number of candidates */
/* ducand - current number of dual candidates */
char electn[50];
#define LINECOUNT 58
