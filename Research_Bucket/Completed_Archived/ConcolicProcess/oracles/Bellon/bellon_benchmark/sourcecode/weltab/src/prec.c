/** prec **/
#include <stdio.h>
#include "weltab.h"
#include "filedecl.h"
#include "candnm.h"
#include "cardrd.h"
#include "dcand.h"
#include "office.h"
#include "offnam.h"
#include "longrp.h"
#include "precin.h"
#include "precvt.h"
#include "repout.h"
#include "unitinfo.h"
typedef struct {
  int vect;
  float val;
  char* ptr;
  void* next;
  char flags[16];
  unsigned int err;
} canv1_t;
main()
/* produce the precinct report from the <indxpr>.vot file */ {
       int j,k,ierr;
       int lines,ipage,lincr,lcand;
       int iward,iprec,isplt;
       int iunit,indxpr,ifound;
       int lavcb;    /* logical */
       long int iprnum;
       long int iabsl();
       char date[12],time[8];
       char fname[13];
       float pcnt,total;
       FILE *fopen();
       void phead();
       trace = FALSE;
       blkbuf(80,report);
       lines = 60;
       ipage = 0;
       welcom(date,time);
       fprintf(stderr,"Producing Precinct Report...\n\n");
       fileoffices = fopen("offices.dat","r");
       flglobal = fopen("global.tbl","r");
       flunitinfo = fopen("unitinfo.tbl","r");
       flprecinfo = fopen("precinfo.tbl","r");
       floffice = fopen("offices.tbl","r");
       floffnam = fopen("offnam.tbl","r");
       fldualoff = fopen("dualoff.tbl","r");
       flcand = fopen("candidat.tbl","r");
       fldualcand = fopen("dualcand.tbl","r");
       /* read global variables */
       offset = 0L;
       fseek(flglobal,offset,0);
       fscanf(flglobal,"%d %d %d %d %d %d %d %50c",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand, electn);
       /* read precinct selected by pfnd */
       flprecid = fopen("thisprec.wtb","r");
       offset = 0L;
       fseek(flprecid,offset,0);
       fscanf(flprecid,"%d %ld %d ",&iunit,&iprnum,&indxpr);
       if (trace) printf("\nPrecinct (%d,%ld,%d)\n",iunit,iprnum,indxpr);
       /* check if pfnd canceled */
       if (iunit < 0 || indxpr < 0) {
             fprintf(stderr,"*** Skipping PREC. Run canceled.\n");
             goto x900;
             };
       /* look up precinfo record for this precinct */
       /* printf("looking up indxpr %d in precinfo file.\n",indxpr); */
       offset = (long) ((indxpr - 1) * 80);
       fseek(flprecinfo,offset,0);
       fscanf(flprecinfo,
             "%d %ld %d %d %d %d %d %d %d %d %d %d %d %d %d",
             &unitno,&precno,&regist,&congr,&senate,&distct,
             &college,&vothow,
             &repre[0],&repre[1],&commis[0],&commis[1],&commis[2],
             &commis[3],&commis[4]);
       if (trace) printf("unitno %d.     precno %ld.\n",unitno,precno);
       if (trace) printf("registration %d.\n",regist);
       /* find <indxpr>.vot filename */
       smove(fname,0,"PRECxxxx.VOT",0,12);
       cvicz(indxpr,fname,4,4);
       fname[12] = '\0';
       /* if it does not exists, cancel */
       ierr = access(fname,0);
       if (ierr != 0) {
             fprintf(stderr,"\n*** File '%12.12s' does not exist. Run canceled.\n",fname);
             goto x900;
             };
       /* file exists, proceed */
       flprecvote = fopen(fname,"r");
       /* read status and record from <indxpr>.vot file */
       offset = 0L;
       fseek(flprecvote,offset,0);
       fscanf(flprecvote,"%d %ld %d %d ",&iunit,&iprnum,&indxpr,&ifound);
       fscanf(flprecvote,
             ">%d %d %ld %ld %8c %8c %12c %8c ",
             &vtindx,&vtunit,&vtprec,&vtpoll,entrer,verifr,vtdate,vttime);
       for (j=1;j<(ncand+1);j++) {
             fscanf(flprecvote,"%ld ",&vote[j]);
             };
       fprintf(stderr,"\n\nPrecinct %d read from file %12.12s.\n",indxpr,fname);
       if (iprnum != vtprec | iunit != vtunit) goto x801;
       if (ifound != 1) {
             if (ifound == 2)
                  fprintf(stderr,"This precinct has been edited.\n");
             };
       /* print precinct identification */
       offset = (long) ((vtunit - 1) * 40);
       fseek(flunitinfo,offset,0);
       fscanf(flunitinfo,"%2c%20c",uncode,unitnm);
       isplt = (short) (iabsl(vtprec) / 10000L);
       iward = (short) (iabsl(vtprec % 10000L) / 100L);
       iprec = (short) (iabsl(vtprec) % 100L);
       lavcb = FALSE;
       if (vtprec < 0L) lavcb = TRUE;
       /* sclear(); */
       tellprec(iward,iprec,isplt,lavcb);
       /* open report file <indxpr>.rpt and tell user */
       smove(fname,8,".RPT",0,4);
       filereport = fopen(fname,"w");
       fprintf(stderr,"\nWriting precinct report to file %12.12s.\n\n",fname);
       /* print report of each office */
       for (j=1;j<(noffic+1);j++) {
             /* get table entry for this office or proposal */
             getoff(j);
             /* determine if this office is valid for this precinct */
             if (! lallow(j,vtprec,indxpr,vtunit)) continue;
             /* report office number as status on screen */
             fprintf(stderr,"Office %d out of %d.\r",j,noffic);
             /* determine if page skip necessary */
             k = 1;
             if (offinf[DUALLOCATION] > 0) k = 2;
             lincr = 2 + (offinf[CANDCOUNT] * k);
             if (lincr <= 2) lincr = 3;
             if (lines+lincr >= 60)
                  phead(&lines,&ipage,iward,iprec,isplt,lavcb,vtdate,vttime);
             lines += lincr;
             /* print heading for office */
             smove(report,0,"0",0,1);
             gtoffi(j,report,10);
             cprint(filereport);
             if (offinf[DUALLOCATION] > 0) {
                  gtdual(j,report,12);
/* ? gtdual(offinf[DUALLOCATION],report,12); */
                  cprint(filereport);
                  };
             /* handle each candidate */
             lcand = offinf[CANDCOUNT];
             total = 0.0;
             if (lcand <= 0) {
                  /* no candidates */
                  smove(report,14,"( No Candidates )",0,17);
                  cprint(filereport);
                  continue;
                  };
             if (prtinf[PERCENT]) {
                  for (k=1;k<(lcand+1);k++)
                      total += vote[offinf[CANDSTART]+k-1];
                  };
             for (k=1;k<(lcand+1);k++) {
                  gtcand(offinf[CANDSTART]+k-1,report,15,report,41);
                  cvicl(vote[offinf[CANDSTART]+k-1],report,48,7);
                  if (prtinf[PERCENT]) {
                      smove(report,57,"(xxxx.xx %)",0,11);
                      pcnt = 0.0;
                      if (total > 0.0)
                         pcnt = (vote[offinf[CANDSTART]+k-1] / total) * 100.0;
                      cvec(pcnt,report,58,7,2);
                      };
                  cprint(filereport);
                  if (offinf[DUALLOCATION] > 0) {
                      gtdcnd(offinf[DUALSTART]+k-1,report,17);
                      cprint(filereport);
                      };
                  /* loop back for next candidate */
                  };
             /* loop back for next election */
             };
       /* print cumulative total vote cast */
       if (lines+3 >= 60)
             phead(&lines,&ipage,iward,iprec,isplt,lavcb,vtdate,vttime);
       smove(report,0,"0",0,1);
       smove(report,10,"Precinct vote cast is:",0,24);
       cvicl(vtpoll,report,36,7);
       smove(report,44,"(xxxx.xx % of Registration)",0,27);
       pcnt = 0.0;
       if (regist > 0)
             pcnt = ((vtpoll * 1.0) / regist) * 100.0;
       cvec(pcnt,report,45,7,2);
       cprint(filereport);
       /* page skip */
       smove(report,0,"1",0,1);
       cprint(filereport);
       goto x900;
       /* error - canceling */
x801:  fprintf(stderr,"\n*** canceled ***\n");
       failure(999);
x900:  fprintf(stderr,"Precinct report complete.           \n");
       fcloseall(); }
/* ===================== */
/** phead **/
void phead(lines,ipage,iward,iprec,isplt,lavcb,date,time)
/* print heading for precinct report */
int    *lines,*ipage;
int    iward,iprec,isplt;
int    lavcb;     /* logical */
char   date[],time[]; {
       int pageno;
       pageno = *ipage + 1;
       *ipage = pageno;
       /* print heading */
       smove(report,0,"1",0,1);
       smove(report,10,"Weltab III",0,10);
       smove(report,25,"Unofficial Precinct Report",0,26);
       smove(report,57,time,0,8);
       smove(report,67,date,0,12);
       cprint(filereport);
       smove(report,10,"Election:",0,9);
       smove(report,25,electn,0,50);
       cprint(filereport);
       /* print identity of this precinct */
       smove(report,0,"0",0,1);
       smove(report,10,unitnm,0,20);
       if (iward > 0) {
             smove(report,30,"Ward ",0,5);
             cvic(iward,report,35,2);
             };
       if (lavcb) {
             smove(report,39,"AVCB ",0,5); }
       else {
             smove(report,39,"Prec ",0,5);
             };
       cvic(iprec,report,44,2);
       if (isplt != 0) {
             smove(report,48,"Split ",0,6);
             cvic(isplt,report,54,2);
             };
       smove(report,67,"Page",0,4);
       cvic(pageno,report,72,4);
       cprint(filereport);
       /* set lines for remaining page */
       *lines = 5;
       return; }
/* ======================== */
