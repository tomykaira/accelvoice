#include <stdio.h>
#include <unistd.h>

#include "pocketsphinx.h"

#include "../include/accel_recognizer.h"
#include "dump_loader.h"

int length_of(void **ptr)
{
  int length = 0;
  while (ptr[length] != NULL)
    length++;
  return length;
}

static int done = FALSE;
char ***loaded_query = NULL;
char **loaded_unknown = NULL;

void result(int index) {
  char **answer = loaded_query[index];
  int ptr = 0;
  while (answer[ptr])
    printf("%s ", answer[ptr++]);
  printf("\n");
  done = TRUE;
}

int main(int argc, char *argv[])
{
  if (argc >= 2) {
    FILE *fp = fopen(argv[1], "r");
    load_from_file(fp);
    fclose(fp);
  } else {
    printf("Loading query from STDIN\n");
    load_from_file(stdin);
  }

  start(argc, argv, NULL);

  start_recognition(loaded_query, loaded_unknown);

  register_cb_recognized(result);

  while (!done) {
    usleep(100000);
  }

  abort_recognition();

  stop();
  return 0;
}
