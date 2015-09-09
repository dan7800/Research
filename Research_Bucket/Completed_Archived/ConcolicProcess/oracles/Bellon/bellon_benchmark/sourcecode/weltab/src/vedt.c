/** vedt **/
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
/* edit precinct results */ {
       int iward,isplt,iprec;
       int indxpr,iunit,ierr,ifound;
       int j,k,n,len;
       int istart,iend,iscat,icstrt;
       long int totoff,iprnum;
       long int iabsl();
       LOGICAL eofsw,lavcb,writsw,confrm(),lallow();
       LOGICAL change,quit;
       char date[12],time[8];
       char fname[13];
       FILE *fopen();
       void tellprec(),askchange(),getoff();
       trace = FALSE;
       welcom(date,time);
       blkbuf(80,report);
       fileoffices = fopen("offices.dat","r");
       flglobal = fopen("global.tbl","r");
       flunitinfo = fopen("unitinfo.tbl","r");
       flprecinfo = fopen("precinfo.tbl","r");
       floffice = fopen("offices.tbl","r+");
       floffnam = fopen("offnam.tbl","r");
       fldualoff = fopen("dualoff.tbl","r");
       flcand = fopen("candidat.tbl","r+");
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
             fprintf(stderr,"*** Skipping VEDT. Run canceled.\n");
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
       /* if it does not exist, cancel */
       ierr = access(fname,0);
       if (ierr != 0) {
             fprintf(stderr,"\n*** File '%12.12s' does not exist. Run canceled.\n",fname);
             goto x900;
             };
       /* file exists, proceed */
       flprecvote = fopen(fname,"r+");
       /* read status and record from <indxpr>.vot file */
       rewind(flprecvote);
       fscanf(flprecvote,"%d %ld %d %d ",&iunit,&iprnum,&indxpr,&ifound);
       fscanf(flprecvote,
             ">%d %d %ld %ld %8c %8c %12c %8c ",
             &vtindx,&vtunit,&vtprec,&vtpoll,entrer,verifr,vtdate,vttime);
       for (j=1;j<(ncand+1);j++) {
             fscanf(flprecvote,"%ld ",&vote[j]);
             };
       fprintf(stderr,"Precinct %d read from file %12.12s.\n",indxpr,fname);
       if (iprnum != vtprec | iunit != vtunit) goto x801;
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
       fprintf(stderr,
       "\nTo skip remaining offices, reply DONE to a change question.\n\n");
       tellprec(iward,iprec,isplt,lavcb);
       /* display total vote cast and verify edit */
       fprintf(stderr,"\nCurrent total vote cast is %ld.\n",vtpoll);
       askchange("entries for this precinct",&change,&quit,TRUE,FALSE);
       if (quit) goto x801;
       if (! change) goto x801;
       /* look for precinct number */
       /* invalid unit or precinct */
       /* unit/precinct match - check index */
       if (vtindx != indxpr) goto x801;
       /* valid unit and prec - start precinct reporting process */
x400:  sclear();
x401:  fprintf(stderr,"\nReady for edit of precinct results.\n");
       tellprec(iward,iprec,isplt,lavcb);
       /* print entrer and verifier names */
       fprintf(stderr,"\nLast name of entry person:  %8.8s\n",entrer);
       fprintf(stderr,"Last name of verify person: %8.8s\n",verifr);
       /* edit total vote cast */
x405:  fprintf(stderr,"\nTotal vote cast = %ld\n",vtpoll);
       askchange("total vote cast",&change,&quit,TRUE,FALSE);
       if (quit) goto x600;
       if (! change) goto x499;
x410:  fprintf(stderr,"\nEnter total vote cast: \n");
       userreply(&len,&eofsw);
       if (eofsw) goto x410;
       if (len < 1) goto x410;
       if (card[0] == 'Y' || card[0] == 'N'
         || card[0] == 'y' || card[0] == 'n') goto x410;
       cvcil(card,0,len,&vtpoll);
       if (vtpoll < 0L) {
             fprintf(stderr,"*** invalid total vote cast\n");
             goto x410;
             };
       if (vtpoll <= (long) regist) goto x415;
       if (regist <= 0) goto x415;
       fprintf(stderr,
             "*** Vote cast more than Precinct's Registration of %d.\n",
             regist);
       goto x410;
x415:  fprintf(stderr,"Total vote cast = %ld\n",vtpoll);
       if (! confrm()) goto x410;
x499:  noop();
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
                  continue;
                  };
             /* print office name */
x525:   sclear();
x526:   gtoffi(j,report,0);
             len = itrim(50,report);
             report[len] = '\0';
             fprintf(stderr,"%s\n\n",report);
             blkbuf(80,report);
             askchange("this office",&change,&quit,TRUE,TRUE);
             if (quit == 999) goto x535;
             if (quit) goto x600;
             if (! change) continue;
             /* office okay for precinct, proceed with query */
x530:   fprintf(stderr,"\nExisting results are:\n");
             totoff = 0L;
             if (offinf[CANDCOUNT] > 0) goto x535;
             if (offinf[WRITEINCOUNT] > 0) goto x535;
             fprintf(stderr,"*** Office has no candidates\n");
             goto x570;
             /* election has ballot candidates */
x535:   istart = offinf[CANDSTART];
             iend = istart + offinf[CANDCOUNT] - 1;
             iscat = iend + 1;
             if (offinf[WRITEINCOUNT] < 1) iscat = 0;
             if (offinf[WRITEINCOUNT] >= 1)
                  iend += offinf[WRITEINCOUNT];
             if (istart <= 0 || iend < istart) failure(512);
             if (quit == 999) goto x238;
             for (k=istart;k<(iend+1);k++) {
                  if (((k-istart+1) > 1) &&
                      (((k-istart+1) % 10) == 1) &&
                     (k < (iend - 1))) {
                      pause();
                      sclear();
                      gtoffi(j,report,0);
                      len = itrim(50,report);
                      report[len] = '\0';
                      fprintf(stderr,"%s (continued)\n\n",report);
                      blkbuf(80,report);
                      };
                  gtcand(k,report,2);
                  cvicl(vote[k],report,28,10);
                  cprint(stderr);
                  };
x536:   fprintf(stderr,
             "\nChange the current results (Y,N,WRITE,DONE) ?\n");
             askchange("",&change,&quit,FALSE,TRUE);
             if (quit == 999) goto x238;
             if (quit) goto x600;
             if (! change) goto x570;
             icstrt = istart;
             goto x239;
             /* only write-in changes */
x238:   icstrt = iscat;
             if (iscat <= 0) icstrt = istart;
             goto x239;
             /* query for each candidate */
x239:   sclear();
             blkbuf(80,report);
             gtoffi(j,report,0);
             cprint(stderr);
             fprintf(stderr,"Enter new value, or = to keep:\n");
             for (k=icstrt;k<(iend+1);k++) {
                  noop();
x541:           gtcand(k,report,0);
                  len = itrim(25,report);
                  report[len] = '\0';
                  fprintf(stderr,"%s (%ld) ?",report,vote[k]);
                  userreply(&len,&eofsw);
                  if (eofsw) goto x541;
                  if (len < 1) goto x541;
                  if (card[0] == '=') goto x543;
                  if (card[0] == 'Y' || card[0] == 'N'
                  || card[0] == 'n' || card[0] == 'n') goto x541;
                  cvcil(card,0,len,&vote[k]);
x543:           if (vote[k] >= 0L && vote[k] <= vtpoll) {
                      totoff = totoff + vote[k];
                      if (totoff <= (vtpoll * (long) offinf[VOTESPERVOTER]))
                         continue;
                      fprintf(stderr,"*** Bad input. ***\n");
                      fprintf(stderr,"*** Total votes for this office is now %ld.\n",
                         totoff);
                      fprintf(stderr,"    maximum possible is %ld\n",
                         (vtpoll * (long) offinf[VOTESPERVOTER]));
                      fprintf(stderr,
                         " given total vote cast at %d votes per voter.\n",
                         offinf[VOTESPERVOTER]);
                      fprintf(stderr,"\n*** Re-edit this office from start\n");
                      if (card[0] != '=') vote[k] = 0L;
                      pause();
                      goto x525;
                      };
                  fprintf(stderr,"*** Bad input. ***\n");
                  if (vote[k] < 0)
                      fprintf(stderr,
                      "    Value non-numeric or less than zero.\n");
                  if (vote[k] > vtpoll)
                      fprintf(stderr,
                      "    Value greater than total vote cast.\n");
                  goto x541;
                  };
             /* check for new write-ins, if write-ins are enabled for this office */
x570:   if (offinf[WRITEINCOUNT] <= 0) goto x580;
             fprintf(stderr,"\nAdd a new named write-in (Y/N) ? \n");
             askchange("",&change,&quit,FALSE,FALSE);
             if (quit) goto x580;
             if (! change) goto x580;
             /* check if write-in is possible to add */
             if (offinf[WRITEINCOUNT] < (nwrite + 1)) goto x572;
             fprintf(stderr,"Cannot add write-in name - limit %d per office.\n",nwrite);
             pause();
             goto x580;
x572:   fprintf(stderr,"Write-in name ?\n");
             userreply(&len,&eofsw);
             if (eofsw) goto x801;
             if (len <= 0 || card[0] == ' ') goto x572;
             smove(report,0,card,0,20);
             len = itrim(50,report);
             report[len] = '\0';
             fprintf(stderr,"\nWrite-in name is '%s'.\n",report);
             if (! confrm()) goto x570;
             /* add the write-in */
             writsw = TRUE;
             offinf[WRITEINCOUNT] += 1;
             k = offinf[CANDSTART] + offinf[CANDCOUNT] - 1 + offinf[WRITEINCOUNT];
             rewind(flcand);
             report[len] = ' ';
             wtcand(k,report,0);
             rewind(flcand);
             report[len] = '\0';
x576:   fprintf(stderr,"Enter votes for %s:\n",report);
             userreply(&len,&eofsw);
             if (eofsw) goto x576;
             if (len < 1) goto x576;
             if (card[0] == 'Y' || card[0] == 'N'
              || card[0] == 'n' || card[0] == 'n') goto x576;
             cvcil(card,0,len,&vote[k]);
             if (vote[k] < 0 || vote[k] > vtpoll) {
                  fprintf(stderr,"*** Bad input. ***\n");
                  if (vote[k] < 0)
                      fprintf(stderr,
                      "    Value non-numeric or less than zero.\n");
                  if (vote[k] > vtpoll)
                      fprintf(stderr,
                      "    Value greater than total vote cast.\n");
                  goto x576; }
             goto x570;
             /* print office name and candidate results */
x580:   sclear();
             istart = offinf[CANDSTART];
             iend = istart + offinf[CANDCOUNT] - 1;
             if (offinf[WRITEINCOUNT] >= 1)
                  iend += offinf[WRITEINCOUNT];
             gtoffi(j,report,0);
             len = itrim(50,report);
             report[len] = '\0';
             fprintf(stderr,"%s\n\n",report);
             totoff = 0L;
             for (k=istart;k<(iend+1);k++) {
                  if ((offinf[CANDCOUNT] > 12) && ((k-istart+1) > 1)
                      && (((k-istart+1) % 15) == 1) && (k < (iend-1))) {
                      /* get confirmation for portion */
                      fprintf(stderr,
       "\n                                         more to come...\n");
                      if (! confrm()) {
                         sclear();
                         fprintf(stderr,
                      "*** Re-edir results for this office:\n");
                         goto x526;
                         };
                      sclear();
                      gtoffi(j,report,0);
                      len = itrim(50,report);
                      report[len] = '\0';
                      fprintf(stderr,"%s\n\n",report);
                      blkbuf(80,report);
                    };
                  gtcand(k,report,0);
                  fprintf(stderr,"%25.25s %ld\n",report,vote[k]);
                  totoff += vote[k];
                  };
             /* check total vote */
             if (totoff <= (vtpoll * (long) offinf[VOTESPERVOTER]))
                  goto x595;
             fprintf(stderr,"*** Bad input. ***\n");
             fprintf(stderr,"*** Total votes for this office is now %ld.\n",
                  totoff);
             fprintf(stderr,"    maximum possible is %ld\n",
                  (vtpoll * (long) offinf[VOTESPERVOTER]));
             fprintf(stderr,
                  " given total vote cast at %d votes per voter.\n",
                  offinf[VOTESPERVOTER]);
             fprintf(stderr,"\n*** Re-edit this office from start\n");
             pause();
             goto x525;
             /* get confirmation from user for this office */
x595:   if (! confrm()) {
                  sclear();
                  fprintf(stderr,"*** Re-edit results for this office:\n");
                  goto x526;
                  };
             if (writsw) {
                  rewind(floffice);
                  putoff(j);
                  rewind(floffice);
                  };
             /* loop back for next office */
             };
       /* get final confirmation from user for this precinct */
x600:  sclear();
       tellprec(iward,iprec,isplt,lavcb);
       fprintf(stderr,
             "\nPrecinct entry complete.  Ready to update database.\n");
       if (! confrm()) goto x401;
       fprintf(stderr,"Thank you.\n");
       /* put status and vote record in <indxpr>.vot file */
       setdate(date,time);
       ifound = 2;
       offset = 0L;
       fseek(flprecvote,offset,0);
       fprintf(flprecvote,"%d %ld %d %d ",iunit,iprnum,indxpr,ifound);
       fprintf(flprecvote,
             ">%4d %4d %8ld %8ld %8.8s %8.8s %12.12s %8.8s ",
             vtindx,vtunit,vtprec,vtpoll,entrer,verifr,date,time);
       for (j=1;j<(ncand+1);j++) {
             fprintf(flprecvote,"%8ld ",vote[j]);
             };
       fprintf(stderr,"Precinct %d rewritten to file %12.12s.\n",indxpr,fname);
       goto x900;
       /* error - canceling */
x801:  fprintf(stderr,"\n*** canceled ***\n");
x900:  fcloseall(); }
/* ======================= */
/** askchange **/
void askchange(what,change,quitflag,prompt,acceptwrite)
/* ask user whether to edit value */
char   what[];
LOGICAL        *change;
LOGICAL        *quitflag;
LOGICAL        prompt;               /* whether to issue prompt */
LOGICAL        acceptwrite;   /* whether to accept writein reply */ {
       int len;
       LOGICAL eofsw;
       if (trace) fprintf(stderr,"> askchange\n");
       *quitflag = FALSE;
       *change = FALSE;
x100:  if (prompt) fprintf(stderr,"Change %s? (Y or N):\n",what);
       userreply(&len,&eofsw);
       if (eofsw) goto x100;
       card[0] = toupper(card[0]);
       if (acceptwrite || len == 4) {
             card[1] = toupper(card[1]);
             card[2] = toupper(card[2]);
             card[3] = toupper(card[3]);
             if (acceptwrite) {
                  if (lscomp(card,0,"WRIT",0,4)
                   || lscomp(card,0,"SCAT",0,4)) {
                      *change = FALSE;
                      *quitflag = 999;
                      return;
                      };
                  };
             if (len == 4) {
                  if (! lscomp(card,0,"STOP",0,4)
                  && ! lscomp(card,0,"DONE",0,4)
                  && ! lscomp(card,0,"QUIT",0,4)) goto x100;
                  *change = FALSE;
                  *quitflag = TRUE;
                  return;
                  };
             };
       if (trace) printf("len=%d, card[0]=%c (dec %d)\n",len,card[0],card[0]);
       if (len != 1) goto x100;
       if (card[0] == 'N') return;
       if (card[0] == '0') return;
       if (card[0] != 'Y'
        && card[0] != '7') goto x100;
       *change = TRUE;
       return; }
/* ==================== */
