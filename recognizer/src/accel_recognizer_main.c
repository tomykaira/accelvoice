#include <stdio.h>
#include <unistd.h>

#include "pocketsphinx.h"

#include "../include/accel_recognizer.h"

int length_of(void **ptr)
{
  int length = 0;
  while (ptr[length] != NULL)
    length++;
  return length;
}

int main(int argc, char *argv[])
{
  char *raw_query[] = {
    "QUEUE",
    "ARRAY TO CHAR SEQUENCE",
    "REQUIRE",
    "ARGS",
    NULL
  };
  char *unknown[] = {
    "ARGS",
    "ARGS",
    NULL
  };
  char ***query;

  query = ckd_calloc(length_of((void *)raw_query), sizeof(char **));
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

  printf("%s\n", raw_query[recognize(query, unknown)]);

  stop();
  return 0;
}
