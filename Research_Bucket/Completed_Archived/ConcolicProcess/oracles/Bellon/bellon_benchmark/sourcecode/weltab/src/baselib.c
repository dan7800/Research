#include <stdio.h>
#include <time.h>
#define TRUE 1
#define FALSE 0
/** smove **/
void smove(bufa,ptra,bufb,ptrb,len)
/* move a string from bufb to bufa */
char   bufa[];
int    ptra;
char   bufb[];
int    ptrb;
int    len; {
       int j,endfound;
       endfound = FALSE;
       for (j=0;j<len;j++) {
             if (endfound) {
                  bufa[ptra+j] = ' '; }
             else {
                  if (bufb[ptrb+j] == '\0') {
                      bufa[ptra+j] = ' ';
                      endfound = TRUE; }
                  else {
                      bufa[ptra+j] = bufb[ptrb+j]; }; }; };
       return; }
/* ==================== */
/** noop **/
void noop()
/* filler: do nothing */ {
       int l;
       l = 0;
       return; }
/* ==================== */
/** iabs **/
int iabs(ivalue)
/* return the absolute value of ivalue */
int ivalue; {
       return (ivalue<0 ? -ivalue : ivalue); }
/* ==================== */
/** iabsl **/
long int iabsl(lvalue)
/* return the absolute value of long lvalue */
long int lvalue; {
       return (lvalue < 0L ? -lvalue : lvalue); }
/* ==================== */
/** failure **/
void failure(code)
/* terminate processing with the indicated code */
int code; {
       fprintf(stderr,"\n *** Stopping with code %d.\n",code);
       fcloseall();
       exit(code); }
/* ==================== */
/** lscomp **/
int lscomp(bufa,ptra,bufb,ptrb,len)
/* compare strings in two buffers */
char   bufa[];
int    ptra;
char   bufb[];
int    ptrb;
int    len; {
       int j;
       for (j=0;j<len;j++) {
             if (bufa[ptra+j] != bufb[ptrb+j]) return FALSE;
             };
       return TRUE; }
/* ==================== */
/** scnbuf **/
void scnbuf(lbuf,buf,num,start,len,irc)
/* find first char and length of series of tokens in buffer */
int    lbuf;      /* length of buffer */
char   buf[];    /* buffer */
int    num;       /* number of tokens expected */
int    start[];   /* index of first char of each token */
int    len[];     /* length of each token */
int    *irc;      /* return code:  -1 = less than num items found
                                    (len[i])=0 for 1st not found)
                                  0 = all is okay
                                  1 = more than num items found */ {
       int i,j,begin,first,blank;
       /* loop through for each item that is expected */
       begin = 0;
       for (i=0;i<num;i++) {
             first = iunsc1(lbuf,buf,begin," ");
             if (first < 0) {
                  /* not enough items on the line */
                  *irc = -1;
                  for (j=i;j<num;j++)
                      len[j] = 0;
                  return;
                  };
             blank = iscan1(lbuf,buf,first," ");
             if (blank < 0) blank = lbuf + 1;
             start[i] = first;
             len[i] = blank - first;
             begin = blank;
             };
       /* look for tokens on rest of line */
       first = iunsc1(lbuf,buf,begin," ");
       if (first < 0) {
             /* finished the line */
             *irc = 0; }
       else {
             /* items remain on the line */
             *irc = 1;
             };
       return; }
/* ==================== */
/** cvci **/
void cvci(bufpar,ptrpar,lenpar,inum)
/* convert character form of integer to binary */
char   bufpar[]; /* buffer */
int    ptrpar;    /* place in bufpar where number starts */
int    lenpar;    /* length of the number */
int    *inum;     /* resulting number */ {
       int atoi();
       char tempbuf[36];
       int j;
       for (j=0;j<lenpar;j++) {
             tempbuf[j] = bufpar[ptrpar+j];
             };
       tempbuf[lenpar] = '\0';
       *inum = atoi(tempbuf);
       return; }
/* ==================== */
/** cvcil **/
void cvcil(bufpar,ptrpar,lenpar,inum)
/* convert character form of integer to binary */
char   bufpar[]; /* buffer */
int    ptrpar;    /* place in bufpar where number starts */
int    lenpar;    /* length of the number */
long int       *inum;        /* resulting number */ {
       long atol();
       char tempbuf[36];
       int j;
       for (j=0;j<lenpar;j++) {
             tempbuf[j] = bufpar[ptrpar+j];
             };
       tempbuf[lenpar] = '\0';
       *inum = atol(tempbuf);
       return; }
/* ==================== */
/** iunsc1 **/
int iunsc1(len,buf,bptr,ch)
/* look for other than one character in a string */
int    len;       /* length of buf */
char   buf[];    /* buffer */
int    bptr;      /* where to start in buf */
char   ch[];     /* character to look for */ {
       int j;
       if (bptr > len) return -1;
       for (j=bptr;j<bptr+len;j++) {
             if (buf[j] != ch[0]) return j;
             };
       /* did not find it */
       return -1; }
/* ==================== */
/** iscan1 **/
int iscan1(len,buf,bptr,ch)
/* look one character in a string */
int    len;       /* length of buf */
char   buf[];    /* buffer */
int    bptr;      /* where to start in buf */
char   ch[];     /* character to look for */ {
       int j;
       if (bptr > len) return -1;
       for (j=bptr;j<bptr+len;j++) {
             if (buf[j] == ch[0]) return j;
             };
       /* did not find it */
       return -1; }
/* ==================== */
/** iscan **/
int iscan(lena,bufa,bptra,lenb,bufb)
/* look for one string in another */
int    lena;      /* length of bufa */
char   bufa[];   /* buffer to look in */
int    bptra;     /* place to start in bufa */
int    lenb;      /* length of bufb */
char   bufb[];   /* string to look for */ {
       int j,k;
       if (bptra+lenb-1 > lena) return -1;
       for (j=bptra;j<(lena-lenb+1);j++) {
             for (k=0;k<lenb;k++) {
                  if (bufa[j+k] != bufb[k]) goto NEXT;
                  };
             /* found it */
             return j;
NEXT:   noop(); /* did not find it yet */
             };
       /* did not find it */
       return -1; }
/* ==================== */
/** setdate **/
void setdate(chdate,chtime)
/* set up char buffers with the date and time */
char chdate[];
char chtime[]; {
       long ltime;
       char *timech;
       char *ctime();
       time(&ltime);
       timech = ctime(&ltime);
       smove(chdate,0,"xxx xx, xxxx",0,12);
       smove(chdate,0,timech,4,6);
       smove(chdate,8,timech,20,4);
       smove(chtime,0,"xx:xx:xx",0,8);
       smove(chtime,0,timech,11,8);
       return; }
/* ==================== */
/** blkbuf **/
void blkbuf(lenpar,bufpar)
/* blank the buffer */
int    lenpar;    /* length of bufpar */
char   bufpar[]; /* buffer */ {
       int j;
       for (j=0;j<lenpar;j++)
             bufpar[j] = ' ';
       return; }
/* ==================== */
/** sclear **/
void sclear()
/* clear the ibm pc screen */ {
       fprintf(stderr,"\033[2J");
       return; }
/* ==================== */
/** cvic **/
void cvic(inum,bufpar,ptrpar,lenpar)
/* convert integer to character form */
int    inum;      /* integer to convert */
char   bufpar[]; /* buffer */
int    ptrpar;    /* where to start in bufpar */
int    lenpar;    /* length to fill with integer */ {
       int len,j,k;
       char tempbuf[20];
       itoa(inum,tempbuf,10);
       len = strlen(tempbuf);
       if (len > lenpar) {
             for (j=ptrpar;j<ptrpar+lenpar;j++)
                  bufpar[j] = '*';
             return;
             };
       /* if leading blanks are needed, blank the buffer */
       if (len < lenpar) {
             for (j=ptrpar;j<ptrpar+lenpar;j++)
                  bufpar[j] = ' ';
             };
       /* copy the string into the buffer */
       for (j=0;j<len;j++) {
             bufpar[(ptrpar+lenpar-len)+j] = tempbuf[j];
             };
       return; }
/* ==================== */
/** cvicl **/
void cvicl(inum,bufpar,ptrpar,lenpar)
/* convert long integer to character form */
long int       inum; /* integer to convert */
char   bufpar[]; /* buffer */
int    ptrpar;    /* where to start in bufpar */
int    lenpar;    /* length to fill with integer */ {
       int len,j,k;
       char tempbuf[20];
       ltoa(inum,tempbuf,10);
       len = strlen(tempbuf);
       if (len > lenpar) {
             for (j=ptrpar;j<ptrpar+lenpar;j++)
                  bufpar[j] = '*';
             return;
             };
       /* if leading blanks are needed, blank the buffer */
       if (len < lenpar) {
             for (j=ptrpar;j<ptrpar+lenpar;j++)
                  bufpar[j] = ' ';
             };
       /* copy the string into the buffer */
       for (j=0;j<len;j++) {
             bufpar[(ptrpar+lenpar-len)+j] = tempbuf[j];
             };
       return; }
/* ==================== */
/** cvicz **/
void cvicz(inum,bufpar,ptrpar,lenpar)
/* convert a number to character with leading zeros */
int    inum;      /* number to be converted */
char   bufpar[]; /* buffer */
int    ptrpar;    /* place to start in bufpar */
int    lenpar;    /* length to use in bufpar */ {
       int j;
       cvic(inum,bufpar,ptrpar,lenpar);
       for (j=ptrpar;j<ptrpar+lenpar;j++) {
             if (bufpar[j] != ' ') return;
             bufpar[j] = '0';
             };
       return; }
/* ==================== */
/** cviczl **/
void cviczl(inum,bufpar,ptrpar,lenpar)
/* convert a number to character with leading zeros */
long int       inum; /* number to be converted */
char   bufpar[]; /* buffer */
int    ptrpar;    /* place to start in bufpar */
int    lenpar;    /* length to use in bufpar */ {
       int j;
       cvicl(inum,bufpar,ptrpar,lenpar);
       for (j=ptrpar;j<ptrpar+lenpar;j++) {
             if (bufpar[j] != ' ') return;
             bufpar[j] = '0';
             };
       return; }
/* ==================== */
/** itrim **/
int itrim(lenpar,bufpar)
/* return length of buffer trimmed for trailing blanks and \0 */
int    lenpar;
char   bufpar[]; {
       int l;
       l = lenpar-1;
       while (l >= 0) {
             if (bufpar[l] != ' ' && bufpar[l] != '\0'
              && bufpar[l] != '\r' && bufpar[l] != '\n'
              && bufpar[l] != '\t'
                  ) break;
             l--;
             };
       if (l < 0) l = lenpar;
       return l+1; }
/* ==================== */
/** pause **/
void pause()
/* pause and wait for user */ {
       int len,eofsw;
       char buf[1];
       fprintf(stderr,"Press <CR> to continue...\n");
       scanf("%c",buf);
       return; }
/* ==================== */
/** cvec **/
void cvec (realnum,bufpar,ptrpar,lenpar,decplaces)
/* convert floating point number to character */
float  realnum;
char   bufpar[];
int    ptrpar;
int    lenpar;
int    decplaces; {
       int decimal,sign,l;
       int decptr;
       char *fcvt(),*digits;
       char   bufa[];
       int    ptra;
       char   bufb[];
       int    ptrb;
       int    len;
       int j, endfound;
       endfound = FALSE;
       for (j=0;j<len;j++) {
             if (endfound) {
                  bufa[ptra+j] = ' '; }
             else {
                  if (bufb[ptrb+j] == '\0') {
                      bufa[ptra+j] = ' ';
                      endfound = TRUE; }
                  else {
                      bufa[ptra+j] = bufb[ptrb+j]; }; }; };
       l = lenpar - decplaces - 1;
       for (j=0;j<lenpar;j++) {
             if (j <= l) {
                  bufpar[ptrpar+j] = ' '; }
             else {
                  bufpar[ptrpar+j] = '0';
                  };
             };
       bufpar[ptrpar+l] = '.';
       digits = fcvt(realnum,decplaces,&decimal,&sign);
       /* printf("cvec: realnum=%.4f, decimal=%d, sign=%d, digits=%s\n", */
             /* realnum,decimal,sign,digits); */
       if (decimal > 0) {
             smove(bufpar,ptrpar+l-decimal,digits,0,decimal); }
       else {
             bufpar[ptrpar+l-1] = '0';
             };
       decptr = decimal;
       if (decptr < 0) decptr = 0;
       for (j=0;j<decplaces;j++) {
             if (decimal < 0) {
                  decimal++;
                  continue; }
             else {
                  bufpar[ptrpar+l+1+j] = digits[decptr];
                  decptr++;
                  };
             };
       /* printf("cvec: %80.80s\n",bufpar); */
       return; }
/* ==================== */
