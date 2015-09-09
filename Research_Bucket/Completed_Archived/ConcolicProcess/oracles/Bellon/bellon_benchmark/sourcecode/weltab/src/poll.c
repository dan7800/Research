/** poll **/
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
typedef struct {
  int poll_type;
  float poll_amount;
  char* poll_ptr;
  void* poll_next;
  char pollwd[16];
  unsigned int flags;
} poll_t;
main()
       /* query the user for precinct results */ {
       int iward,isplt,iprec;
       int indxpr,iunit,ierr,ifound;
       int j,k,n,len;
       int istart,iend;
       long int totoff,iprnum;
       long int iabsl();
       LOGICAL eofsw,lavcb,confrm(),lallow();
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
             fprintf(stderr,"*** Skipping POLL. Run canceled.\n");
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
       /* if total vote cast in record was not zero, verify */
       if (vtpoll > 0) {
             fprintf(stderr,"\nCurrent total vote cast is %ld.\n",vtpoll);
             fprintf(stderr,"Should old precinct results be replaced ?\n");
             if (! confrm()) goto x801;
             };
       /* look for precinct number */
       /* invalid unit or precinct */
       /* unit/precinct match - check index */
       if (vtindx != indxpr) goto x801;
       /* get entrer and verifier names */
       whoentrer();
       /* valid unit and prec - start precinct reporting process */
x400:  sclear();
x401:  fprintf(stderr,"\nReady for entry of precinct results.\n");
       tellprec(iward,iprec,isplt,lavcb);
       /* initialize vote record */
       vtpoll = 0L;
       for (j=1;j<(ncand+1);j++)
             vote[j] = 0L;
       /* get total vote cast */
x410:  fprintf(stderr,"\nEnter total vote cast: \n");
       userreply(&len,&eofsw);
       if (eofsw) goto x410;
       if (len < 1) goto x410;
       if (card[0] == 'Y' || card[0] == 'N'
         || card[0] == 'n' || card[0] == 'n') goto x410;
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
                  pause();
                  continue;
                  };
             istart = offinf[CANDSTART];
             iend = istart + offinf[CANDCOUNT] - 1;
             /* query for each candidate */
             for (k=istart;k<(iend+1);k++) {
                  if (((k-istart+1) > 1) &&
                      (((k-istart+1) % 10) == 1) && (k < (iend-1))) {
                    sclear();
                      gtoffi(j,report,0);
                      len = itrim(50,report);
                      report[len] = '\0';
                      fprintf(stderr,"%s (continued)\n\n",report);
                      blkbuf(80,report);
                      };
x541:           gtcand(k,report,0);
                  len = itrim(25,report);
                  report[len] = '\0';
                  fprintf(stderr,"%s ? \n",report);
                  userreply(&len,&eofsw);
                  if (eofsw) goto x541;
                  if (len < 1) goto x541;
                  if (card[0] == 'Y' || card[0] == 'N'
                   || card[0] == 'n' || card[0] == 'n') goto x541;
                  cvcil(card,0,len,&vote[k]);
                  if (vote[k] >= 0L && vote[k] <= vtpoll) {
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
                      fprintf(stderr,"\n*** Re-enter this office from start\n");
                      vote[k] = 0L;
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
             /* print office name and candidate results */
x580:   sclear();
             gtoffi(j,report,0);
             len = itrim(50,report);
             report[len] = '\0';
             fprintf(stderr,"%s\n\n",report);
             for (k=istart;k<(iend+1);k++) {
                  if ((offinf[CANDCOUNT] > 12) && ((k-istart+1) > 1)
                      && (((k-istart+1) % 15) == 1) && (k < (iend-1))) {
                      /* get confirmation for portion */
                      fprintf(stderr,
       "\n                                         more to come...\n");
                      if (! confrm()) {
                         sclear();
                         fprintf(stderr,
                      "*** Re-enter results for this office:\n");
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
                  };
             /* get confirmation from user for this office */
             if (! confrm()) {
                  sclear();
                  fprintf(stderr,"*** Re-enter results for this office:\n");
                  goto x526;
                  };
             };
       /* get final confirmation from user for this precinct */
       sclear();
       tellprec(iward,iprec,isplt,lavcb);
       fprintf(stderr,
             "\nPrecinct entry complete.  Ready to update database.\n");
       if (! confrm()) {
             /* not confirmed. determine how bad it is. */
qhowbad:       fprintf(stderr,
"\nNot confirmed.\n\nAre there Many or Few changes needed (M or F) ?\n");
             userreply(&len,&eofsw);
             if (eofsw) goto qhowbad;
             if (len < 1) goto qhowbad;
             card[0] = toupper(card[0]);
             if (card[0] != 'M' && card[0] != 'F') goto qhowbad;
             /* if many changes needed, have precinct re-entered */
             if (card[0] == 'M') {
                  fprintf(stderr,
             "\nPlease re-enter this precinct from the beginning.\n");
                  pause();
                  goto x401; }
             else {
                  fprintf(stderr,
             "\nThe results so far will be put in the database.\n");
                  fprintf(stderr,
             "Please tell your supervisor what changes are needed.\n\n");
                  goto savedb;
                  }; }
       else {
             fprintf(stderr,"Thank you.\n");
             };
       /* put status and vote record in <indxpr>.vot file */
savedb:        setdate(date,time);
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
       goto done;
       /* error - canceling */
x801:  fprintf(stderr,"\n*** canceled ***\n");
done:  fcloseall(); }
/* ======================= */
typedef struct {
  int poll_type;
  float poll_amount;
  char* poll_ptr;
  void* poll_next;
  char pollwd[16];
  unsigned int flags;
  double factor;
  double real;
  double imag;
} polling_t;
/** whoentrer **/
void whoentrer() {
       int len, eofsw;
x407:  fprintf(stderr,"\nEnter last name of entry person: \n");
       userreply(&len,&eofsw);
       if (eofsw) goto x407;
       if (len < 1) goto x407;
       if (lscomp(card,0," ",0,1)) goto x407;
       smove(entrer,0,card,0,8);
x408:  fprintf(stderr,"\nEnter last name of verify person, or <CR>\n");
       userreply(&len,&eofsw);
       if (eofsw) goto x408;
       if (len < 1) {
             smove(verifr,0,"NONE    ",0,8); }
       else {
             if (lscomp(card,0," ",0,1)) goto x408;
             smove(verifr,0,card,0,8);
             };
       return; }
/* ======================= */
