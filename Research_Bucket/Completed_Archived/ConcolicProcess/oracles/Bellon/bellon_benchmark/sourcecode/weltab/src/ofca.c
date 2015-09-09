/** ofca **/
#include <stdio.h>
#include "weltab.h"
#include "filedecl.h"
#include "candnm.h"
#include "cardrd.h"
#include "dcand.h"
#include "longrp.h"
#include "office.h"
#include "offnam.h"
#include "precin.h"
#include "precvt.h"
#include "repout.h"
#include "unitinfo.h"
main()
/* process election, office, and candidate cards */ {
       int j,k,nread;
       int nw;
       LOGICAL propsw,eofsw;
       char date[12],time[8];
       char writein[25];
       FILE *fopen();
       void allowcard();
       trace = FALSE;
       blkbuf(80,report);
       welcom(date,time);
       fileoffices = fopen("offices.dat","r");
       flglobal = fopen("global.tbl","r+");
       flunitinfo = fopen("unitinfo.tbl","r");
       flprecinfo = fopen("precinfo.tbl","r");
       floffice = fopen("offices.tbl","w+");
       floffnam = fopen("offnam.tbl","w+");
       fldualoff = fopen("dualoff.tbl","w+");
       flcand = fopen("candidat.tbl","w+");
       fldualcand = fopen("dualcand.tbl","w+");
       nread = 0;
       /* read global variables */
       rewind(flglobal);
       fscanf(flglobal,"%d %d %d %d %d %d %d ",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand);
       /* reset ofca globals to initial values */
       noffic = 0;
       ndual = 0;
       nwrite = -1;
       ncand = 0;
       ducand = 0;
       /* get election name */
       cread(fileoffices,&eofsw);
       if (eofsw) failure(100);
       nread++;
       while (card[0] == '*') {
             cread(fileoffices,&eofsw);
             if (eofsw) failure(101);
             nread++;
             };
       if (! lscomp(card,0,"ELECTION",0,8)) failure(8);
       smove(electn,0,card,9,50);
       /* get write-ins card */
       cread(fileoffices,&eofsw);
       if (eofsw) failure(150);
       nread++;
       while (card[0] == '*') {
             cread(fileoffices,&eofsw);
             if (eofsw) failure(151);
             nread++;
             };
       if (! lscomp(card,0,"WRITEINS",0,8)) failure(2);
       cvci(card,9,1,&nwrite);
       if (nwrite < 2) nwrite = 2;
       /* next office */
x200:  propsw = FALSE;
       for (k=0;k<6;k++) {
             offinf[k] = -1;
             if (k < 3) allow[k] = -1;
             if (k < 2) prtinf[k] = FALSE;
             };
       cread(fileoffices,&eofsw);
       if (eofsw) goto x800;
       nread++;
       while (card[0] == '*') {
             cread(fileoffices,&eofsw);
             if (eofsw) goto x800;
             nread++;
             };
       if (lscomp(card,0,"OFFICE",0,6)) goto x210;
       if (lscomp(card,0,"PROPOSAL",0,8)) goto x250;
       /* bad card - should have been office or proposal */
       failure(12);
       /* office card */
x210:
       noffic++;
       if (noffic > MAXOFFICE) failure(210);
       wtoffi(noffic,card,9);
       for (j=0;j<6;j++) {
             offinf[j] = 0;
             if (j < 3) allow[j] = 0;
             if (j < 2) prtinf[j] = FALSE;
             };
       /* next card should be second or allow */
       cread(fileoffices,&eofsw);
       if (eofsw) failure(211);
       nread++;
       if (lscomp(card,0,"SECOND",0,6)) goto x220;
       if (lscomp(card,0,"ALLOW",0,5)) goto x300;
       failure(212);
       /* second card */
x220:
       ndual++;
       if (ndual > MAXDUAL) failure(220);
       offinf[DUALLOCATION] = ndual;
       wtdual(ndual,card,9);
       cread(fileoffices,&eofsw);
       if (eofsw) failure(221);
       nread++;
       if (lscomp(card,0,"ALLOW",0,5)) goto x300;
       failure(222);
       /* proposal (proposition, amendment, etc.) */
x250:
       propsw = TRUE;
       noffic++;
       if (noffic > MAXOFFICE) failure(250);
       wtoffi(noffic,card,9);
       ncand++;
       if (ncand > MAXCAND) failure(251);
       wtcand(ncand,"Yes                      ",0,"   ",0);
       offinf[CANDSTART] = ncand;
       offinf[CANDCOUNT] = 2;
       offinf[WRITEINCOUNT] = -1;
       ncand++;
       if (ncand > MAXCAND) failure(252);
       wtcand(ncand,"No                       ",0,"   ",0);
       /* proposal must have allow card */
       cread(fileoffices,&eofsw);
       if (eofsw) failure(253);
       nread++;
       if (! lscomp(card,0,"ALLOW",0,5)) failure(254);
       /* handle allow card */
x300:  allowcard();
/* expect state card (whether to include office in state govt book) */
x400:  cread(fileoffices,&eofsw);
       if (eofsw) failure(400);
       nread++;
       if (! lscomp(card,0,"STATE",0,5)) failure(401);
       if (! (card[9] == 'Y' || card[9] == 'N')) failure(402);
       prtinf[STATE] = TRUE;
       if (card[9] == 'N') prtinf[STATE] = FALSE;
/* expect percent card (whether to print percentages) */
x440:  cread(fileoffices,&eofsw);
       if (eofsw) failure(440);
       nread++;
       if (! lscomp(card,0,"PERCENT",0,7)) failure(441);
       if (! (card[9] == 'Y' || card[9] == 'N')) failure(442);
       prtinf[PERCENT] = TRUE;
       if (card[9] == 'N') prtinf[PERCENT] = FALSE;
       /* expect votesper card (how many votes per voter for office) */
x480:  cread(fileoffices,&eofsw);
       if (eofsw) failure(480);
       nread++;
       if (! lscomp(card,0,"VOTESPER",0,8)) failure(481);
       cvci(card,9,1,&offinf[VOTESPERVOTER]);
       if (offinf[VOTESPERVOTER] < 1 ||
             offinf[VOTESPERVOTER] > 9   ) failure(482);
       /* expect candidates */
x500:  cread(fileoffices,&eofsw);
       if (eofsw) goto x590;
       nread++;
       if (lscomp(card,0,"ENDCAND",0,7)) goto x590;
       if (! lscomp(card,0,"CAND",0,4)) failure(500);
/* cand card */
x550:  offinf[CANDCOUNT]++;
       ncand++;
       if (ncand > MAXCAND) failure(550);
       if (offinf[CANDSTART] == 0)
             offinf[CANDSTART] = ncand;
       wtcand(ncand,card,13,card,9);
       if (offinf[DUALLOCATION] != 0) {
             ducand++;
             if (ducand > MAXDCAND) failure(4);
             if (offinf[DUALSTART] == 0)
                  offinf[DUALSTART] = ducand;
             wtdcnd(ducand,card,40);
             };
       goto x500;
       /* endcand card - provide for writeins in the tables */
x590:  if (propsw) goto x595;
       ncand++;
       if (offinf[CANDSTART] <= 0)
             offinf[CANDSTART] = ncand;
       offinf[WRITEINCOUNT] = 1;
       if (ncand > MAXCAND) failure(590);
       wtcand(ncand,"Scattered Write-ins      ",0,"   ",0);
       smove(writein,0,"Named Write-in xxx       ",0,25);
       for (nw=0;nw<nwrite;nw++) {
             ncand++;
             if (ncand > MAXCAND) failure(595);
             cvic(nw+1,writein,15,3);
             wtcand(ncand,writein,0,"   ",0);
             };
       /* put out table entry for this office or proposal */
x595:  offset = (long) ((noffic - 1) * 70);
       fseek(floffice,offset,0);
       fprintf(floffice,
             "%4d %4d %4d %4d %4d %4d %8ld %8ld %8ld %2.2s %4d %4d ",
             offinf[0], offinf[1], offinf[2], offinf[3], offinf[4],
             offinf[5], allow[0], allow[1], allow[2], allowch,
             prtinf[0], prtinf[1]);
       /* loop back for next office or proposal */
       goto x200;
       /* last steps */
x800:  fprintf(stderr,"\n%d cards read.\n",nread);
       /* rewrite global table */
       rewind(flglobal);
       fprintf(flglobal,"%4d %4d %4d %4d %4d %4d %4d %50.50s",
             nunit,nprec,noffic,ndual,nwrite,ncand,ducand,
             electn);
       fprintf(stderr,"global table file re-written.\n");
       fprintf(stderr,"%d units, totaling %d precincts.\n\n",nunit,nprec);
       fprintf(stderr,"%d races, of which %d have dual races.\n",noffic,ndual);
       fprintf(stderr,"%d candidate slots, %d running mates.\n",ncand,ducand);
       fcloseall(); }
/* ==================== */
int iscancard(lena,a,ptra,lenb,b)
/* look for one string in another */
int    lena;      /* length of a */
char   a[];   /* buffer to look in */
int    ptra;     /* place to start in a */
int    lenb;      /* length of b */
char   b[];   /* string to look for */ {
       int j,k;
       if (ptra+lenb-1 > lena) return -1;
       for (j=ptra;j<(lena-lenb+1);j++) {
             for (k=0;k<lenb;k++) {
                  if (a[j+k] != b[k]) goto NEXT;
                  };
             /* found it */
             return j;
NEXT:   noop(); /* did not find it yet */
             };
       /* did not find it */
       return -1; }
/** allowcard **/
void allowcard() {
       int j,iprec,iward,iunit;
x300:  allow[TYPE] = 0L;
       allow[PRECID] = 0L;
       smove(allowch,0,"XX",0,2);
       if (lscomp(card,9,"ALL",0,3)) allow[TYPE] = ALLOWALL;
       if (lscomp(card,9,"UNIT",0,4)) allow[TYPE] = ALLOWUNIT;
       if (lscomp(card,9,"COMM",0,4)) allow[TYPE] = ALLOWCOMM;
       if (lscomp(card,9,"REPR",0,4)) allow[TYPE] = ALLOWREPR;
       if (lscomp(card,9,"SEN",0,3)) allow[TYPE] = ALLOWSEN;
       if (lscomp(card,9,"CONG",0,4)) allow[TYPE] = ALLOWCONG;
       if (lscomp(card,9,"DIST",0,4)) allow[TYPE] = ALLOWDIST;
       if (lscomp(card,9,"PREC",0,4)) allow[TYPE] = ALLOWPREC;
       if (lscomp(card,9,"WARD",0,4)) allow[TYPE] = ALLOWWARD;
       if (lscomp(card,9,"COLL",0,4)) allow[TYPE] = ALLOWCOLL;
       if (allow[TYPE] == 0L) failure(301);
       /* verify voter allow information against unit/precinct info */
       if (allow[TYPE] == ALLOWALL) return;
       /* allow unit or precinct */
       if (allow[TYPE] != ALLOWUNIT
             && allow[TYPE] != ALLOWPREC
             && allow[TYPE] != ALLOWWARD) goto x320;
       smove(allowch,0,card,14,2);
       for (j=1;j<(nunit+1);j++) {
             iunit = j;
             offset = (long) ((j - 1) * 40);
             fseek(flunitinfo,offset,0);
             fscanf(flunitinfo,"%2c",uncode);
             if (lscomp(allowch,0,uncode,0,2))
                  goto x315;
             };
       failure(312);
x315:  if (allow[TYPE] == ALLOWUNIT) return;
       cvci(card,17,2,&iward);
       iprec = 0;
       if (allow[TYPE] == ALLOWPREC)
             cvci(card,20,2,&iprec);
       allow[PRECID] = (long) ((iward * 100) + iprec);
       for (j=1;j<(nprec+1);j++) {
             if (allow[TYPE] == ALLOWWARD) {
                  /* ward */
                  if (allow[PRECID]/100L == precno/100L
                      &&    iunit == unitno ) return; }
             else {
                  /* precinct */
                  if (allow[PRECID] == precno
                      &&    iunit == unitno ) return;
                  };
             };
       failure(319);
       /* allow other than unit or precinct */
x320:  cvci(card,14,2,&j);
       allow[VALUE] = (long) j;
       /*  a check for legal sen,comm,repr,cong,dist,coll could go here */
       return; }
/* ==================== */
