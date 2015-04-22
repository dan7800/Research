/** ccibm **/
#include <stdio.h>
main() {
       int k,nread,nwrite;
       char buffer[1000];
       char *result;
       nread = 0;
       nwrite = 0;
       while ((result = gets(buffer)) != NULL) {
             nread++;
             if (buffer[0] != '1'
              && buffer[0] != ' '
              && buffer[0] != '0'
              && buffer[0] != '+') {
                  if (nread != 1) printf("\n");
                  printf("%s",buffer);
                  nwrite++;
                  };
             if (buffer[0] == '1') {
                  if (nread != 1) printf("\f");
                  };
             if (buffer[0] == ' ') {
                  if (nread != 1) printf("\n");
                  };
             if (buffer[0] == '0') {
                  if (nread != 1) printf("\n\n");
                  if (nread == 1) printf("\n");
                  };
             if (buffer[0] == '+')
                  printf("\r");
             k = 1;
             do {
                  buffer[k-1] = buffer[k];
                  k++; }
                  while (buffer[k-1] != '\0');
             printf("%s",buffer);
             nwrite++;
             };
       printf("\f");
       fprintf(stderr,"%d lines read, %d lines written.\n",nread,nwrite); }
