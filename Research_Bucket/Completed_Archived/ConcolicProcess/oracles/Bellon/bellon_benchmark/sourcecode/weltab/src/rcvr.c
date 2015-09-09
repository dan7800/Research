/** rcvr **/
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
/* receive and accept/reject VOT files from disk A: */ {
       int j,ierr,xprec;
       int iward,iprec,isplt;
       int iunit,indxpr,ifound;
       int lavcb,exists,same,votesame;       /* logical */
       long int iprnum;
       char date[12],time[8];
       char fname[15];
       FILE *fopen(),*freporn(),*flcvote;
       int ciunit,cindxpr,cifound;
       long ciprnum;
       int cvtindx;
       int cvtunit;
       long int cvtprec;
       long int cvtpoll;
       long int cvote[MAXCAND];
       char centrer[8];
       char cverifr[8];
       char cvtdate[12];
       char cvttime[8];
       int k,nread;
       int npage,nlines;
       char buffer[1000];
       char *result;
       nread = 0;
       npage = 0;
       nlines = 0;
       while ((result = gets(buffer)) != NULL) {
             nread++;
             if (buffer[0] != '1'
              && buffer[0] != ' '
              && buffer[0] != '0'
              && buffer[0] != '+') {
                  nlines++;
                  fprintf(stdout,"     %d Lines\r",nlines);
                  };
             if (buffer[0] == '1') {
                  npage++;
                  fprintf(stdout,"\n\nPage %d\n",npage);
                  nlines = 0;
                  fprintf(stdout,"     %d Lines\r",nlines); }
             if (buffer[0] == ' ') {
                  nlines++;
                  fprintf(stdout,"     %d Lines\r",nlines);
                  };
             if (buffer[0] == '0') {
                  nlines += 2;
                  fprintf(stdout,"     %d Lines\r",nlines);
                  };
             if (buffer[0] == '+')
                  noop();
             };
       fprintf(stdout,"\n\n");
       fprintf(stdout,stderr,"%d lines read.\n",nread);
       trace = FALSE;
       blkbuf(80,report);
       welcom(date,time);
       fprintf(stderr,"Receiving precincts...\n");
       flglobal = fopen("global.tbl","r");
       flunitinfo = fopen("unitinfo.tbl","r");
       flprecinfo = fopen("precinfo.tbl","r");
       /* read global variables */
       rewind(flglobal);
       fscanf(flglobal,"%d %d %d %d %d %d %d %50c",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand, electn);
       smove(fname,0,"x:PRECxxxx.VOT",0,14);
       fname[14] = '\0';
       for (xprec=1;xprec<(nprec+1);xprec++) {
             /* find <indxpr>.vot filename on disk A */
             cvicz(xprec,fname,6,4);
             fname[0] = 'A';
             if (trace) fprintf(stderr,"File name is %14.14s.\n",fname);
             /* if it does not exists, skip */
             ierr = access(fname,0);
             if (ierr != 0) {
                  if (trace)
                      fprintf(stderr,"*** File '%14.14s' does not exist.\n",fname);
                  continue;
                  };
             /* file exists, proceed */
             flprecvote = fopen(fname,"r");
             sclear();
             /* look up precinfo record for this precinct */
             /* printf("looking up indxpr %d in precinfo file.\n",indxpr); */
             offset = (long) ((xprec - 1) * 80);
             fseek(flprecinfo,offset,0);
             fscanf(flprecinfo,
                  "%d %ld %d %d %d %d %d %d %d %d %d %d %d %d",
                  &unitno,&precno,&regist,&congr,&senate,&distct,&vothow,
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
             tellprec(iward,iprec,isplt,lavcb);
             if (ifound <= 1) {
                  fprintf(stderr,"This precinct has not been edited. Skipping.\n");
                  pause();
                  continue;
                  };
             fscanf(flprecvote,
                  ">%d %d %ld %ld %8c %8c %12c %8c ",
                  &vtindx,&vtunit,&vtprec,&vtpoll,entrer,verifr,vtdate,vttime);
             if (vtpoll < 0L) {
                  fprintf(stderr,"This precinct has no total vote cast. Skipping.\n");
                  pause();
                  continue;
                  };
             for (j=1;j<(ncand+1);j++) {
                  fscanf(flprecvote,"%ld ",&vote[j]);
                  };
             fprintf(stderr,"Precinct %d read from file %14.14s.\n",xprec,fname);
             /* find <indxpr>.vot filename on disk C */
             fname[0] = 'C';
             if (trace) fprintf(stderr,"File name is %14.14s.\n",fname);
             /* if it does not exists, write it */
             exists = FALSE;
             ierr = access(fname,0);
             if (ierr == 0) {
                  /* file exists, must compare */
                  flcvote = fopen(fname,"r+");
                  exists = TRUE;
                  /* read status record from C:<indxpr>.vot file */
                  rewind(flcvote);
                  fscanf(flcvote,"%d %ld %d %d ",&ciunit,&ciprnum,&cindxpr,&cifound);
                  fscanf(flcvote,
                      ">%d %d %ld %ld %8c %8c %12c %8c ",
                      &cvtindx,&cvtunit,&cvtprec,&cvtpoll,centrer,cverifr,cvtdate,cvttime);
                  for (j=1;j<(ncand+1);j++) {
                      fscanf(flcvote,"%ld ",&cvote[j]);
                      };
                  fprintf(stderr,"Precinct %d read from file %14.14s.\n",xprec,fname);
                  /* present differing items to user */
                  fprintf(stderr,"\nComparing files for precinct %d...\n",xprec);
                  same = TRUE;
                  if (ciunit != iunit) {
                      fprintf(stderr,"iunit:  A %d, C %d.\n",iunit,ciunit);
                      same = FALSE;
                      };
                  if (ciprnum != iprnum) {
                      fprintf(stderr,"iprnum: A %ld, C %ld.\n",iprnum,ciprnum);
                      same = FALSE;
                      };
                  if (cindxpr != indxpr) {
                      fprintf(stderr,"indxpr: A %d, C %d.\n",indxpr,cindxpr);
                      same = FALSE;
                      };
                  if (cifound != ifound) {
                      fprintf(stderr,"ifound: A %ld, C %ld.\n",ifound,cifound);
                      same = FALSE;
                      };
                  if (cvtindx != vtindx) {
                      fprintf(stderr,"vtindx: A %d, C %d.\n",vtindx,cvtindx);
                      same = FALSE;
                      };
                  if (cvtunit != vtunit) {
                      fprintf(stderr,"vtunit: A %d, C %d.\n",vtunit,cvtunit);
                      same = FALSE;
                      };
                  if (cvtprec != vtprec) {
                      fprintf(stderr,"vtprec: A %ld, C %ld.\n",vtprec,cvtprec);
                      same = FALSE;
                      };
                  if (cvtpoll != vtpoll) {
                      fprintf(stderr,"vtpoll: A %ld, C %ld.\n",vtpoll,cvtpoll);
                      same = FALSE;
                      };
                  if (! lscomp(centrer,0,entrer,0,8)) {
                      fprintf(stderr,"entrer: A '%8.8s', C '%8.8s'.\n",entrer,centrer);
                      same = FALSE;
                      };
                  if (! lscomp(cverifr,0,verifr,0,8)) {
                      fprintf(stderr,"verifr: A '%8.8s', C '%8.8s'.\n",verifr,cverifr);
                      same = FALSE;
                      };
                  if (! lscomp(cvtdate,0,vtdate,0,8)) {
                      fprintf(stderr,"vtdate: A '%12.12s', C '%12.12s'.\n",vtdate,cvtdate);
                      same = FALSE;
                      };
                  if (! lscomp(cvttime,0,vttime,0,8)) {
                      fprintf(stderr,"vttime: A '%8.8s', C '%8.8s'.\n",vttime,cvttime);
                      same = FALSE;
                      };
                  if (same) {
                      fprintf(stderr,"Basic statistics are equal.\n"); }
                  else {
                      pause();
                      };
                  votesame = TRUE;
                  for (k=1;k<(ncand+1);k++) {
                      if (cvote[k] != vote[k]) {
                         if ((k % 4) == 1) fprintf(stderr,"\nvote: ");
                         fprintf(stderr,"%d(A%ld,C%ld) ",k,vote[k],cvote[k]);
                         votesame = FALSE;
                         };
                      };
                      fprintf(stderr,"\n");
                      if (votesame) fprintf(stderr,"Vote counts are equal.\n");
                  if (same && votesame) {
                      fprintf(stderr,"\nFiles are the same. Precinct skipped.\n");
                      pause();
                      continue;
                      };
                  /* files are not the same */
                  fprintf(stderr,"Files are NOT the same.  Changes from A will be accepted.\n");
                  if (confrm()) {
                      fprintf(stderr,"File from A has been accepted.\n"); }
                  else {
                      fprintf(stderr,"File from A rejected.\n");
                      pause();
                      continue;
                      }; }
             else {
                  fprintf(stderr,"*** File '%14.14s' does not exist.\n",fname);
                  };
             /* decided to copy A to C */
             if (exists) {
                  fprintf(stderr,"\nReplacing precinct on disk C\n"); }
             else {
                  flcvote = fopen(fname,"w");
                  fprintf(stderr,"\nAdding precinct to disk C\n");
                  };
             /* copy <indxpr>.vot file from A to C */
             rewind(flcvote);
             fprintf(flcvote,"%d %ld %d %d ",iunit,iprnum,indxpr,ifound);
             fprintf(flcvote,
                  ">%4d %4d %8ld %8ld %8.8s %8.8s %12.12s %8.8s ",
                  vtindx,vtunit,vtprec,vtpoll,entrer,verifr,vtdate,vttime);
             for (j=1;j<(ncand+1);j++) {
                  fprintf(flcvote,"%8ld ",vote[j]);
                  };
             fprintf(stderr,"Precinct %d written to file %14.14s.\n",indxpr,fname);
             pause();
             /* loop back for next precinct */
             };
x900:  fprintf(stderr,"\nReceiving process complete.\n");
       fcloseall(); }
/* ===================== */
