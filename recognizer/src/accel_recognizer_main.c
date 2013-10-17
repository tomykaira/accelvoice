#include <stdio.h>
#include <unistd.h>

#include "pocketsphinx.h"

#include "../include/accel_recognizer.h"

char *raw_query[] = {
  "QUEUE",
  "ARRAY TO CHAR SEQUENCE",
  "REQUIRE",
  "ARGS",
  "GUI",
  NULL
};

char *unknown[] = {
  "ARGS",
  "ARGS",
  "GUI",
  NULL
};

int length_of(void **ptr)
{
  int length = 0;
  while (ptr[length] != NULL)
    length++;
  return length;
}

static int done = FALSE;

void result(int index) {
  printf("%s\n", raw_query[index]);
  done = TRUE;
}

int main(int argc, char *argv[])
{
  char ***query;

  query = ckd_calloc(length_of((void *)raw_query) + 1, sizeof(char **));
  for (int i = 0; raw_query[i]; ++i) {
    char buf[256];
    int ptr = 0;
    int wc = 0;

    query[i] = calloc(16, sizeof(char *));
    for (int j = 0; raw_query[i][j]; ++j) {
      if (raw_query[i][j] == ' ') {
        buf[ptr++] = 0;
        query[i][wc++] = ckd_salloc(buf);
        ptr = 0;
      } else {
        buf[ptr++] = raw_query[i][j];
      }
    }
    buf[ptr] = 0;
    query[i][wc++] = ckd_salloc(buf);
  }

  start(argc, argv);

  start_recognition(query, unknown);

  register_cb_recognized(result);

  while (!done) {
    usleep(100000);
  }

  abort_recognition();

  stop();
  return 0;
}
