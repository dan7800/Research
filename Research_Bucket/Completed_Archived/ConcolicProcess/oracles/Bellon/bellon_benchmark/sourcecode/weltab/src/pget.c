/** pget **/
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
/* query the user for precinct identification */ {
       int j,isplt,len,index;
       int iunit, iward, iprec;
       long int iprnum;
       char date[12],time[8];
       LOGICAL eofsw,lavcb;
       char getunit[3];
       FILE *fopen();
       void shead();
       trace = FALSE;
       flprecid = fopen("thisprec.wtb","w");
       flglobal = fopen("global.tbl","r");
       flunitinfo = fopen("unitinfo.tbl","r");
       flprecinfo = fopen("precinfo.tbl","r");
       /* read global variables */
       offset = 0L;
       fseek(flglobal,offset,0);
       fscanf(flglobal,"%d %d %d %d %d %d %d %50c",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand, electn);
top:   welcom(date,time);
       fprintf(stderr,"\n%50.50s\n\n",electn);
       iunit = 0;
       iward = 0;
       iprec = 0;
       isplt = 0;
       lavcb = FALSE;
       /* query user for legal unit designator */
asku:  fprintf(stderr,"Which Unit (2 letter code or QUIT) ? \n");
       userreply(&len,&eofsw);
       if (eofsw) goto ueof;
       card[0] = toupper(card[0]);
       card[1] = toupper(card[1]);
       if (lscomp(card,0,"QU",0,2)) goto uquit;
       if (lscomp(card,0,"**",0,2)) goto uquit;
       if (len != 2) goto asku;
       /* find unit specified in unit list */
       smove(getunit,0,card,0,2);
       for (j=1;j<(nunit+1);j++) {
             iunit = j;
             offset = (long) ((j - 1) * 40);
             fseek(flunitinfo,offset,0);
             fscanf(flunitinfo,"%2c",uncode);
             if (lscomp(getunit,0,uncode,0,2))
                  goto gotu;
             };
       /* unit not found - print message and try again */
       fprintf(stderr,"* * * Unit '%2.2s' not found. * * *\n\n",card);
       iunit = 0;
       goto asku;
       /* found unit. print unit name and get rest of unit info */
gotu:  fscanf(flunitinfo,"%20c",unitnm);
       fscanf(flunitinfo,"%d %d %d %d %d",
             &untnprec,&untstart,&untwards,&untavcbs,&untsplit);
       fprintf(stderr,"Unit is: %20.20s\n\n",unitnm);
       if (untnprec <= 0 || untstart <= 0) failure(537);
       if (untstart+untnprec-1 > nprec) failure(538);
       /* handle differently if unit has only one precinct */
       if (untnprec == 1) {
             fprintf(stderr,"This unit has only one precinct.\n");
             /* get its info from from precinfo file */
             offset = (long) ((untstart - 1) * 80);
             fseek(flprecinfo,offset,0);
             fscanf(flprecinfo,"%d %ld",&unitno,&precno);
             if (unitno != iunit) failure(533);
             /* decode precinct identity from precno */
             index = untstart;
             iprnum = precno;
             if (precno < 0L) {
                  precno = - precno;
                  lavcb = TRUE;
                  };
             isplt = (short) (precno / 10000L);
             iward = (short) ((precno % 10000L) / 100L);
             iprec = (short) (precno % 100L);
             goto match;
             };
       /* unit has many precincts */
many:  noop();
       /* does the unit have AVCBs? */
       if (untavcbs != NONE) {
             /* check if this is an AVCB */
qavcb:         fprintf(stderr,"Is this an AVCB ? (Y or N):\n");
             userreply(&len,&eofsw);
             if (eofsw) goto ueof;
             card[0] = toupper(card[0]);
             card[1] = toupper(card[1]);
             if (lscomp(card,0,"QU",0,2)) goto uquit;
             if (lscomp(card,0,"**",0,2)) goto uquit;
             if (len < 0 || len > 1) goto qavcb;
             if (len != 0) {
                  if (! (card[0] == 'Y' || card[0] == 'N'
                      || card[0] == '7' || card[0] == '0'))
                      goto qavcb;
                  };
             if (len != 0 && card[0] == 'Y') lavcb = TRUE;
             if (len != 0 && card[0] == '7') lavcb = TRUE;
             fprintf(stderr,"\n");
             };
       /* does the unit have wards? */
       if (untwards) {
             /* skip if AVCB but unit's AVCBs are not by ward */
             if (! (lavcb && (untavcbs != BYWARD))) {
                  /* obtain ward number */
qward:                fprintf(stderr,"Which Ward (1 digit) ?\n");
                  userreply(&len,&eofsw);
                  if (eofsw) goto ueof;
                  card[0] = toupper(card[0]);
                  card[1] = toupper(card[1]);
                  if (lscomp(card,0,"QU",0,2)) goto uquit;
                  if (lscomp(card,0,"**",0,2)) goto uquit;
                  if (len < 1 || len > 1) goto qward;
                  cvci(card,0,len,&iward);
                  if (iward < 1 || iward > 9) goto qward;
                  fprintf(stderr,"\n");
                  };
             };
       /* obtain precinct or AVCB number */
qprec: if (lavcb) {
             fprintf(stderr,
                  "Which AVCB (1 or 2 digits) ?\n"); }
       else {
             fprintf(stderr,
                  "Which Precinct (1 or 2 digits) ?\n");
             };
       userreply(&len,&eofsw);
       if (eofsw) goto ueof;
       card[0] = toupper(card[0]);
       card[1] = toupper(card[1]);
       if (lscomp(card,0,"QU",0,2)) goto uquit;
       if (lscomp(card,0,"**",0,2)) goto uquit;
       if (len < 1 || len > 2) goto qprec;
       cvci(card,0,len,&iprec);
       if (iprec <= 0 || iprec > 99) goto qprec;
       fprintf(stderr,"\n");
       /* assemble partial precinct code (may need split yet) */
       iprnum = ((long) iward * 100L) + (long) iprec;
       if (lavcb) iprnum = - iprnum;
       /* loop through the precincts for this unit */
       for (j=untstart;j<(untstart+untnprec);j++) {
             index = j;
             /* get unit and precinct numbers from precinfo file */
             offset = (long) ((j - 1) * 80);
             fseek(flprecinfo,offset,0);
             fscanf(flprecinfo,"%d %ld",&unitno,&precno);
             /* printf("prec j %d out of %d; vtunit %d; vtprec %ld\n" */
             /*   ,j,nprec,vtunit,vtprec); */
             if (unitno != iunit) failure(535);
             /* does (possibly partial) precinct id match as is? */
             if (precno == iprnum) goto match;
             /* could it be an as-yet-unidentified split precinct? */
             if (! untsplit || lavcb) continue;
             if ((precno % 10000L) != iprnum) continue;
             if (isplt != 0) continue;
             /* identify split and complete partial precinct id */
qsplt:         fprintf(stderr,
                  "Enter Split number (1 or 2 digits) ?\n");
             userreply(&len,&eofsw);
             if (eofsw) goto ueof;
             card[0] = toupper(card[0]);
             card[1] = toupper(card[1]);
             if (lscomp(card,0,"QU",0,2)) goto uquit;
             if (lscomp(card,0,"**",0,2)) goto uquit;
             if (len < 1 || len > 2) goto qsplt;
             cvci(card,0,len,&isplt);
             if (isplt < 1 || isplt > 99) goto qsplt;
             fprintf(stderr,"\n");
             /* complete the precinct id by including split */
             if (lavcb) iprnum = - iprnum;
             iprnum += ((long) isplt * 10000L);
             if (lavcb) iprnum = - iprnum;
             /* try match with the split information added */
             if (precno == iprnum) goto match;
             };
       /* precinct not found in precinfo */
       fprintf(stderr,"\n*** Precinct not found. Start again.\n");
       pause();
       goto top;
       /* found unit.  print verification. */
match: fprintf(stderr,
       "\nPrecinct is: %20.20s ",unitnm);
       if (iward > 0)
             fprintf(stderr,"Ward %d, ",iward);
       if (lavcb) {
             fprintf(stderr,"AVCB %d",iprec); }
       else {
             fprintf(stderr,"Prec %d",iprec);
             };
       if (isplt != 0)
             fprintf(stderr,", Split %d",isplt);
       fprintf(stderr,".\n");
       /* verify precinct information */
qokay: fprintf(stderr,"Correct ?  (Y or N): ");
       userreply(&len,&eofsw);
       if (eofsw) goto ueof;
       card[0] = toupper(card[0]);
       card[1] = toupper(card[1]);
       if (lscomp(card,0,"QU",0,2)) goto uquit;
       if (lscomp(card,0,"**",0,2)) goto uquit;
       if (len != 1) goto qokay;
       if (! (card[0] == 'Y' || card[0] == '7'
           || card[0] == 'N' || card[0] == '0'))
             goto qokay;
       if (card[0] == 'N' || card[0] == '0') {
             /* error - not confirmed by user */
             fprintf(stderr,"\n*** Not confirmed. Start again.\n");
             pause();
             goto top;
             };
       /* write precinct identification to file */
       offset = 0L;
       fseek(flprecid,offset,0);
       fprintf(flprecid,"%d %ld %d ",iunit,iprnum,index);
       fprintf(stderr,
             "\nPrecinct found. (%d,%ld,%d)\n",iunit,iprnum,index);
       goto done;
       /* error - eof */
ueof:  fprintf(stderr,"\n*** EOF on input. Start again.\n");
       pause();
       goto top;
       /* cancelled by user with ** code */
uquit: fprintf(stderr,"\n*** Session cancelled.\n");
       offset = 0L;
       fseek(flprecid,offset,0);
       fprintf(flprecid,"%d %ld %d ",-1,-1L,-1);
       goto done;
done:  fcloseall(); }
/* ===================== */
