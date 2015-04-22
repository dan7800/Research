/** sped **/
/* Speed-Entry of precinct numbers from the std. posting sheet */
/* 10-23-88 */
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
       /* query the user for precinct results by speed entry */ {
       int iward,isplt,iprec;
       int indxpr,iunit,ierr,ifound;
       int j,k,n,len;
       int istart,iend;
       long int totoff,iprnum;
       long int iabsl();
       LOGICAL eofsw,lavcb,lallow();
       char date[12],time[8];
       char fname[13];
       FILE *fopen();
       void tellprec(),whoentrer(),getoff();
       trace = FALSE;
       fulltrace = FALSE;
       welcom(date,time);
       blkbuf(80,report);
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
       rewind(flglobal);
       fscanf(flglobal,"%d %d %d %d %d %d %d %50c",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand, electn);
       /* read precinct selected by pfnd */
       flprecid = fopen("thisprec.wtb","r");
       rewind(flprecid);
       fscanf(flprecid,"%d %ld %d ",&iunit,&iprnum,&indxpr);
       if (trace) printf("\nPrecinct (%d,%ld,%d)\n",iunit,iprnum,indxpr);
       /* check if pfnd canceled */
       if (iunit < 0 || indxpr < 0) {
             fprintf(stderr,"*** Skipping SPED. Run canceled.\n");
             goto done;
             };
       /* look up precinfo record for this precinct */
       /* printf("looking up indxpr %d in precinfo file.\n",indxpr); */
       offset = (long) ((indxpr - 1) * 80);
       fseek(flprecinfo,offset,0);
       fscanf(flprecinfo,
             "%d %ld %d %d %d %d %d %d %d %d %d %d %d %d %d %d",
             &unitno,&precno,&regist,&congr,&senate,&distct,
             &college,&vothow,
             &repre[0],&repre[1],&commis[0],&commis[1],&commis[2],
             &commis[3],&commis[4]);
       if (trace) printf("unitno %d.     precno %ld.\n",unitno,precno);
       if (trace) printf("registration %d.\n",regist);
       /* print precinct identification */
       offset = (long) ((unitno - 1) * 40);
       fseek(flunitinfo,offset,0);
       fscanf(flunitinfo,"%2c%20c",uncode,unitnm);
       isplt = (short) (iabsl(precno) / 10000L);
       iward = (short) (iabsl(precno % 10000L) / 100L);
       iprec = (short) (iabsl(precno) % 100L);
       lavcb = FALSE;
       if (precno < 0L) lavcb = TRUE;
       tellprec(iward,iprec,isplt,lavcb);
       /* find <indxpr>.vot filename */
       smove(fname,0,"PRECxxxx.VOT",0,12);
       cvicz(indxpr,fname,4,4);
       fname[12] = '\0';
       /* if it does not exist, cancel */
       ierr = access(fname,0);
       if (ierr != 0) {
             fprintf(stderr,"\n*** File '%12.12s' does not exist. Run canceled.\n",fname);
             goto done;
             };
       /* file exists, proceed */
       flprecvote = fopen(fname,"r+");
       /* read status and record from <indxpr>.vot file */
       rewind(flprecvote);
       fscanf(flprecvote,"%d %ld %d %d ",&iunit,&iprnum,&indxpr,&ifound);
       if (ifound != 1) {
             if (ifound == 2)
                  fprintf(stderr,"*** This precinct has been entered already.\n");
             if (ifound == 3)
                  fprintf(stderr,"*** Precinct was speed-entered already.\n");
             goto x801;
             };
       fscanf(flprecvote,
             ">%d %d %ld %ld %8c %8c %12c %8c ",
             &vtindx,&vtunit,&vtprec,&vtpoll,entrer,verifr,vtdate,vttime);
       for (j=1;j<(ncand+1);j++) {
             fscanf(flprecvote,"%ld ",&vote[j]);
             };
       fprintf(stderr,"Precinct %d read from file %12.12s.\n",indxpr,fname);
       if (iprnum != vtprec | iunit != vtunit) goto x801;
       /* if total vote cast in record was not zero, can't speed enter */
       if (vtpoll > 0) {
             fprintf(stderr,"** Precinct has total vote cast of %d.  Cant speed-enter.\n",vtpoll);
             goto done;
             };
       /* look for precinct number */
       /* invalid unit or precinct */
       /* unit/precinct match - check index */
       if (vtindx != indxpr) goto x801;
       /* get entrer and verifier names */
       /* whoentrer(); */
       /* confirm desire for speed entry */
qsped: fprintf(stderr,"\n Speed Enter this precinct ? (Y or N):\n");
        userreply(&len,&eofsw);
        if (eofsw) goto qsped;
        if (len != 1) goto qsped;
        card[0] = toupper(card[0]);
        if (card[0] == 'Y') goto x400;
        if (card[0] == 'N') goto done;
        if (card[0] == '7') goto x400;
        if (card[0] == '0') goto done;
        goto qsped;
       /* valid unit and prec - start precinct reporting process */
x400:  sclear();
x401:  fprintf(stderr,"\n** Speed-Entry **\n");
       tellprec(iward,iprec,isplt,lavcb);
       /* initialize vote record */
       vtpoll = 0L;
       for (j=1;j<(ncand+1);j++)
             vote[j] = 0L;
       /* loop for each office in election */
       for (j=1;j<(noffic+1);j++) {
             /* printf("office %d.\n",j); */
             /* get table entry for this office */
             getoff(j);
             /* determine if this office is valid for this precinct */
             /* if (vtprec < 0L) goto x525  # 11/3/82 lallow & poll on avcb */
             if (! lallow(j,vtprec,indxpr,vtunit)) {
                  if (trace) {
                      printf("office %d not allowed for precinct %ld.\n",j,vtprec);
                      pause();
                      };
                  sclear();
                  printf("Looking for next office... %d.\r",j);
                  continue;
                  };
             /* print office name */
x525:   if (trace) pause();
             sclear();
x526:   gtoffi(j,report,0);
             len = itrim(50,report);
             report[len] = '\0';
             fprintf(stderr,"%s\n\n",report);
             /* office okay for precinct, proceed with query */
x530:   totoff = 0L;
             if (offinf[CANDCOUNT] <= 0) {
                  /* no candidates */
                  fprintf(stderr,"*** Office has no candidates.\n");
                  /* pause(); */
                  continue;
                  };
             istart = offinf[CANDSTART];
             iend = istart + offinf[CANDCOUNT] - 1;
             /* query for each candidate */
             for (k=istart;k<(iend+1);k++) {
/* x541:             gtcand(k,report,0); */
                  /* len = itrim(25,report); */
                  /* report[len] = '\0'; */
                  /* fprintf(stderr,"%s ? \n",report); */
x541:           fprintf(stderr,"next ? \n");
                  userreply(&len,&eofsw);
                  if (eofsw) goto x541;
                  if (len < 1) goto x541;
                  if (card[0] == 'Y' || card[0] == 'N'
                   || card[0] == 'n' || card[0] == 'n') goto x541;
                  cvcil(card,0,len,&vote[k]);
/*            if (vote[k] >= 0L && vote[k] <= vtpoll)                                   */
/*              {                                                                */
/*              totoff = totoff + vote[k];                                       */
/*              if (totoff <= (vtpoll * (long) offinf[VOTESPERVOTER]))           */
/*               continue;                                                       */
/*              fprintf(stderr,"*** Bad input. ***\n");                          */
/*              fprintf(stderr,"*** Total votes for this office is now %ld.\n",  */
/*               totoff);                                                        */
/*              fprintf(stderr,"    maximum possible is %ld\n",                  */
/*               (vtpoll * (long) offinf[VOTESPERVOTER]));                       */
/*              fprintf(stderr,                                                  */
/*               " given total vote cast at %d votes per voter.\n",              */
/*               offinf[VOTESPERVOTER]);                                         */
/*              fprintf(stderr,"\n*** Re-enter this office from start\n");       */
/*              vote[k] = 0L;                                                    */
/*              pause();                                                         */
/*              goto x525;                                                       */
/*              };                                                               */
/*            fprintf(stderr,"*** Bad input. ***\n");                                   */
/*            if (vote[k] < 0)                                                          */
/*              fprintf(stderr,                                                  */
/*              "    Value non-numeric or less than zero.\n");                   */
/*            if (vote[k] > vtpoll)                                                     */
/*              fprintf(stderr,                                                  */
/*              "    Value greater than total vote cast.\n");                    */
/*            goto x541;                                                                */
                  };
             };
       /* put status and vote record in <indxpr>.vot file */
savedb:        setdate(date,time);
       ifound = 3;  /* flag as speed entered */
       offset = 0L;
       fseek(flprecvote,offset,0);
       fprintf(flprecvote,"%d %ld %d %d ",iunit,iprnum,indxpr,ifound);
       fprintf(flprecvote,
             ">%4d %4d %8ld %8ld %8.8s %8.8s %12.12s %8.8s ",
             vtindx,vtunit,vtprec,vtpoll,entrer,verifr,date,time);
       for (j=1;j<(ncand+1);j++) {
             fprintf(flprecvote,"%8ld ",vote[j]);
             };
       sclear();
       fprintf(stderr,"Precinct %d rewritten to file %12.12s.\n",indxpr,fname);
       goto done;
       /* error - canceling */
x801:  fprintf(stderr,"\n*** canceled ***\n");
done:  if (trace)
             fprintf(stderr,"next program should be SPOL.\n");
       fcloseall(); }
/* ======================= */
void shead(lines,ipage,date,time)
/* print heading of sample cumulative report */
int    *lines,*ipage;
char   date[],time[]; {
       int cprec,pageno;
       float pcnt;
       pageno = *ipage + 1;
       *ipage = pageno;
       /* print heading */
       smove(report,0,"1",0,1);
       smove(report,10,"Weltab III",0,10);
       smove(report,25,"Sample Cumulative Report",0,24);
       smove(report,57,time,0,8);
       smove(report,67,date,0,12);
       cprint(filereport);
       smove(report,10,"Election:",0,9);
       smove(report,25,electn,0,50);
       cprint(filereport);
       smove(report,0,"0",0,1);
       cprec = 0;
       cvicz(cprec,report,10,3);
       smove(report,14,"precincts reporting out of ",0,27);
       cvic(nprec,report,41,3);
       smove(report,47,"(xxxx.xx %)",0,11);
       pcnt = (cprec * 100.0) / nprec;
       cvec(pcnt,report,48,7,2);
       smove(report,67,"Page",0,4);
       cvic(pageno,report,72,4);
       cprint(filereport);
       /* set lines for remaining page */
       *lines = 5;
       return; }
