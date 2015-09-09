/** linecount **/
#include <stdio.h>
main() {
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
       fprintf(stdout,stderr,"%d lines read.\n",nread); }
