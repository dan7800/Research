/** vtot **/
#include <stdio.h>
#include "weltab.h"
#include "filedecl.h"
#include "candnm.h"
#include "cardrd.h"
#include "cumtvt.h"
#include "dcand.h"
#include "office.h"
#include "offnam.h"
#include "longrp.h"
#include "precin.h"
#include "precvt.h"
#include "repout.h"
#include "unitinfo.h"
main()
/* program to produce cumulative record from vote file */ {
       int j,k,ierr,xprec;
       int lines,ipage,lincr,lcand;
       int iward,iprec,isplt;
       int iunit,indxpr,ifound;
       LOGICAL lavcb;
       long int iprnum;
       char date[12],time[8];
       char fname[13];
       FILE *fopen();
       void phead();
       trace = FALSE;
       blkbuf(80,report);
       welcom(date,time);
       fprintf(stderr,"Totaling precincts...\n\n");
       flglobal = fopen("global.tbl","r");
       flunitinfo = fopen("unitinfo.tbl","r");
       flprecinfo = fopen("precinfo.tbl","r");
       floffice = fopen("offices.tbl","r");
       /* read global variables */
       rewind(flglobal);
       fscanf(flglobal,"%d %d %d %d %d %d %d %50c",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand, electn);
       /* initialize cumulative variables */
       cmprec = 0L;
       cmpoll = 0L;
       cmreg = 0L;
       for (j=1;j<(ncand+1);j++)
             cumt[j] = 0L;
       smove(fname,0,"PRECxxxx.VOT",0,12);
       fname[12] = '\0';
       for (xprec=1;xprec<(nprec+1);xprec++) {
             prfini[xprec] = FALSE;
             /* find <indxpr>.vot filename */
             cvicz(xprec,fname,4,4);
             /* if it does not exists, cancel */
             ierr = access(fname,0);
             if (ierr != 0) {
                  if (trace)
                      fprintf(stderr,"*** File '%12.12s' does not exist.\n",fname);
                  continue;
                  };
             /* file exists, proceed */
             if (xprec != 1) fclose(flprecvote);
             flprecvote = fopen(fname,"r");
             /* look up precinfo record for this precinct */
             /* printf("looking up indxpr %d in precinfo file.\n",indxpr); */
             offset = (long) ((xprec - 1) * 80);
             fseek(flprecinfo,offset,0);
             fscanf(flprecinfo,
                  "%d %ld %d %d %d %d %d %d %d %d %d %d %d %d %d",
                  &unitno,&precno,&regist,&congr,&senate,&distct,
                  &college,&vothow,
                  &repre[0],&repre[1],&commis[0],&commis[1],&commis[2],
                  &commis[3],&commis[4]);
             if (trace) printf("unitno %d.     precno %ld.\n",unitno,precno);
             if (trace) printf("registration %d.\n",regist);
             /* read status and record from <indxpr>.vot file */
             rewind(flprecvote);
             fscanf(flprecvote,"%d %ld %d %d ",&iunit,&iprnum,&indxpr,&ifound);
             /* print precinct identification */
             offset = (long) ((iunit - 1) * 40);
             fseek(flunitinfo,offset,0);
             fscanf(flunitinfo,"%2c%20c",uncode,unitnm);
             isplt = (short) (iabsl(iprnum) / 10000L);
             iward = (short) (iabsl(iprnum % 10000L) / 100L);
             iprec = (short) (iprnum % 100L);
             lavcb = FALSE;
             if (iprnum < 0L) lavcb = TRUE;
             if (ifound <= 1) {
                  fprintf(stderr,"\n");
                  tellprec(iward,iprec,isplt,lavcb);
                  fprintf(stderr,"This precinct has not been edited. Skipping.\n");
                  continue;
                  };
             fscanf(flprecvote,
                  ">%d %d %ld %ld %8c %8c %12c %8c ",
                  &vtindx,&vtunit,&vtprec,&vtpoll,entrer,verifr,vtdate,vttime);
             if (vtpoll < 0L) {
                  fprintf(stderr,"\n");
                  tellprec(iward,iprec,isplt,lavcb);
                  fprintf(stderr,"This precinct has no total vote cast. Skipping.\n");
                  continue;
                  };
             for (j=1;j<(ncand+1);j++) {
                  fscanf(flprecvote,"%ld ",&vote[j]);
                  };
             if (trace)
                  fprintf(stderr,"Precinct %d read from file %12.12s.\n",xprec,fname);
             /* add this precinct to the cumulative totals */
             fprintf(stderr,"\rAdding precinct %d.",xprec);
             prfini[xprec] = TRUE;
             cmprec++;
             cmpoll += vtpoll;
             prpoll[xprec] = vtpoll;
             cmreg += (long) regist;
             for (j=1;j<(ncand+1);j++)
                  cumt[j] += vote[j];
             /* loop back for next precinct */
             };
       /* write out the cumulative counts */
       fltotals = fopen("totals.wtb","w");
       rewind(fltotals);
       fprintf(fltotals,"%12.12s %8.8s %50.50s ",date,time,electn);
       fprintf(fltotals,
             "%ld %ld %ld ",
             cmprec,cmpoll,cmreg);
       for (j=1;j<(ncand+1);j++) {
             fprintf(fltotals,"%ld ",cumt[j]);
             };
       for (j=1;j<(nprec+1);j++) {
             fprintf(fltotals,"%d %ld ",prfini[j],prpoll[j]);
             };
x900:  fprintf(stderr,"\rCumulative totals complete.\n");
       fcloseall(); }
/* ===================== */
